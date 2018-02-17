package com.jgasteiz.readcomicsandroid.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.adapters.DirectoryAdapter
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType

import kotlinx.android.synthetic.main.activity_downloads.*

class DownloadsActivity : AppCompatActivity() {

    private val LOG_TAG = DownloadsActivity::class.java.simpleName

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

        populateListView(downloadedComicList)
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

            val intent = Intent(this, ReadingActivity::class.java)
            intent.putExtra("comic", item)
            startActivity(intent)
        }
    }
}
