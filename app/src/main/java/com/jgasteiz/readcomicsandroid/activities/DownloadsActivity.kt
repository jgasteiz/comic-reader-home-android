package com.jgasteiz.readcomicsandroid.activities

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.adapters.ItemListAdapter
import com.jgasteiz.readcomicsandroid.helpers.Constants
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.models.Item
import kotlinx.android.synthetic.main.activity_downloads.*


class DownloadsActivity : BaseActivity() {

    private val LOG_TAG = DownloadsActivity::class.java.simpleName

    override val hasRemovableItems = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)
        setSupportActionBar(toolbar)

        // Go to root
        val downloadButtonView = findViewById<FloatingActionButton>(R.id.goto_root)
        downloadButtonView.setOnClickListener { _ ->
            val intent = Intent(this, DirectoryActivity::class.java)
            startActivity(intent)
        }

        // Get offline comics
        val downloadedComicList = Utils.getDownloadedComics(this)
        Log.d(LOG_TAG, downloadedComicList.toString())

        populateRecyclerView(downloadedComicList)
    }

    /**
     * Populate the activity list view with comics/directories.
     */
    private fun populateRecyclerView(itemList: ArrayList<Item>) {
        val recyclerView = findViewById<RecyclerView>(R.id.itemList)
        val linearLayoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = linearLayoutManager

        val adapter = ItemListAdapter(
                this,
                itemList,
                onItemClick = ::onItemClick,
                onDownloadClick = null,
                onRemoveClick = {
                    removeDownload(it)
                    itemList.remove(it);
                    true
                }
        )
        recyclerView.adapter = adapter
    }

    /**
     * Comic click listener - go to ReadingActivity.
     */
    private fun onItemClick(comic: Item) {
        val intent = Intent(this, ReadingActivity::class.java)
        intent.putExtra("comic", comic)
        startActivity(intent)
    }
}
