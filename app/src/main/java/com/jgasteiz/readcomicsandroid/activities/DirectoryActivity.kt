package com.jgasteiz.readcomicsandroid.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.adapters.ItemListAdapter
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnDirectoryContentFetched
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
import android.support.v7.widget.DividerItemDecoration


class DirectoryActivity() : BaseActivity() {

    private var mCurrentDirectory: Item? = null

    override val hasRemovableItems = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Go to downloads
        val downloadButtonView = findViewById<FloatingActionButton>(R.id.goto_downloads)
        downloadButtonView.setOnClickListener { _ ->
            val intent = Intent(this, DownloadsActivity::class.java)
            startActivity(intent)
        }

        loadCurrentDirectory()
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

        val recyclerView = findViewById<RecyclerView>(R.id.itemList)
        val linearLayoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = linearLayoutManager

        val adapter = ItemListAdapter(
                this,
                itemList,
                onItemClick = ::onItemClick,
                onDownloadClick = ::startDownload,
                onRemoveClick = {
                    removeDownload(it)
                    false
                }
        )
        recyclerView.adapter = adapter
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
}
