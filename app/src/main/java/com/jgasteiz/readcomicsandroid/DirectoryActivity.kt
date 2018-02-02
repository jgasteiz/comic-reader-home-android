package com.jgasteiz.readcomicsandroid

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnDirectoryContentFetched
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType

class DirectoryActivity : AppCompatActivity() {

    private var mCurrentDirectory: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
