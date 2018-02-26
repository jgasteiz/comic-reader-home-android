package com.jgasteiz.readcomicsandroid.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.adapters.DirectoryAdapter
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnDirectoryContentFetched
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType


class DirectoryActivity : BaseActivity() {

    private var mCurrentDirectory: Item? = null

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
                            populateListView(itemList)
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
    private fun populateListView(itemList: ArrayList<Item>) {
        val listView = findViewById<ListView>(R.id.itemList)
        val adapter = DirectoryAdapter(this, itemList)
        listView.adapter = adapter

        listView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = itemList[position]

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
}
