package com.jgasteiz.readcomicsandroid.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.adapters.ItemListAdapterKotlin
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnDirectoryContentFetched
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
import android.support.v7.widget.DividerItemDecoration
import com.jgasteiz.readcomicsandroid.helpers.Constants
import com.jgasteiz.readcomicsandroid.services.DownloadsService


class DirectoryActivity() : BaseActivity() {

    private var mCurrentDirectory: Item? = null

    private var mAdapterKotlin: ItemListAdapterKotlin? = null
    private var mItemList: ArrayList<Item>? = null

    override val hasRemovableItems = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Go to downloads
        val downloadButtonView = findViewById<FloatingActionButton>(R.id.goto_downloads)
        downloadButtonView.setOnClickListener { _ ->
            val intent = Intent(this, DownloadsActivity::class.java)
            startActivity(intent)
            finish()
        }

        loadCurrentDirectory()
    }

    /**
     * Create an intent filter with actions we'll listen for and register
     * the broadcast receiver.
     */
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_DOWNLOAD_START)
        filter.addAction(Constants.ACTION_DOWNLOAD_PROGRESS)
        filter.addAction(Constants.ACTION_DOWNLOAD_END)
        registerReceiver(broadcastReceiver, filter)
    }

    /**
     * Unregister the broadcast receiver.
     */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    /**
     * Hack alert: when pressing "back", navigate to the parent
     * directory if the current directory has a parent.
     * Otherwise, do what the back button does.
     */
    override fun onBackPressed() {
        if (mCurrentDirectory == null) {
            super.onBackPressed()
        } else {
            mCurrentDirectory = mCurrentDirectory!!.parentDirectory
            loadCurrentDirectory()
        }
    }

    /**
     * Load the path of the current directory in the list.
     */
    private fun loadCurrentDirectory() {
        if (Utils.isNetworkAvailable(this)) {
            Utils.fetchDirectoryDetails(
                    this,
                    mCurrentDirectory?.path,
                    object : OnDirectoryContentFetched {
                        override fun callback(itemList: ArrayList<Item>) {
                            populateRecyclerView(itemList)
                        }
                    }
            )
        } else {
            Toast.makeText(this, "There's no internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Populate the activity list view with comics/directories.
     */
    private fun populateRecyclerView(itemList: ArrayList<Item>) {
        if (itemList.size == 0) {
            Toast.makeText(this, "There are no items to load. Make sure the server is working well.", Toast.LENGTH_LONG).show()
            return
        }
        mItemList = itemList

        val recyclerView = findViewById<RecyclerView>(R.id.itemList)
        val linearLayoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = linearLayoutManager

        mAdapterKotlin = ItemListAdapterKotlin(
                this,
                mItemList!!,
                onItemClick = ::onItemClick,
                onDownloadClick = ::startDownload,
                onRemoveClick = {
                    removeDownload(it)
                    false
                }
        )
        recyclerView.adapter = mAdapterKotlin
    }

    /**
     * Handle a click event on a comic or a directory.
     */
    private fun onItemClick(item: Item) {
        // If the item is a comic, go read it.
        if (item.type == ItemType.COMIC) {
            val intent = Intent(this, ReadingActivity::class.java)
            intent.putExtra("comic", item)
            startActivity(intent)
        }
        // Otherwise, navigate!
        else if (item.type == ItemType.DIRECTORY) {
            if (mCurrentDirectory != null) {
                item.parentDirectory = Item(
                        mCurrentDirectory!!.name,
                        mCurrentDirectory!!.path,
                        mCurrentDirectory!!.parentDirectory,
                        mCurrentDirectory!!.type
                )
            } else {
                item.parentDirectory = null
            }

            mCurrentDirectory = item
            loadCurrentDirectory()
        }
    }

    /**
     * Start a download for the given comic.
     */
    fun startDownload(comic: Item) {
        val bundle = Bundle()
        bundle.putSerializable("comic", comic)
        val downloadIntent = Intent(this, DownloadsService::class.java)
        downloadIntent.putExtras(bundle)
        this.startService(downloadIntent)
    }


    /**
     * Receive broadcasts about items being downloaded. When an item starts
     * or finishes being downloaded, update the item on the list and notify
     * the adapter so it can reload the recyclerview.
     */
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.ACTION_DOWNLOAD_START) {
                val resultValue = intent.getStringExtra("resultValue")
                Toast.makeText(context, resultValue, Toast.LENGTH_SHORT).show()
                val comic = intent.getSerializableExtra("comic") as Item
                mItemList?.find { s -> s.path == comic.path }?.isComicDownloading = true
                mAdapterKotlin?.notifyDataSetChanged()
            } else if (intent.action == Constants.ACTION_DOWNLOAD_PROGRESS) {
                val resultValue = intent.getStringExtra("resultValue")
                val comic = intent.getSerializableExtra("comic") as Item
                mItemList?.find { s -> s.path == comic.path }?.downloadProgress = resultValue
                mAdapterKotlin?.notifyDataSetChanged()
            } else if (intent.action == Constants.ACTION_DOWNLOAD_END) {
                val resultValue = intent.getStringExtra("resultValue")
                Toast.makeText(context, resultValue, Toast.LENGTH_SHORT).show()
                val comic = intent.getSerializableExtra("comic") as Item
                mItemList?.find { s -> s.path == comic.path }?.isComicDownloading = false
                mItemList?.find { s -> s.path == comic.path }?.isComicOffline = true
                mAdapterKotlin?.notifyDataSetChanged()
            }
        }
    }
}
