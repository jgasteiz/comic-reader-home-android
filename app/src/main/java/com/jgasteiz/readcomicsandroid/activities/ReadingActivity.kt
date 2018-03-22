package com.jgasteiz.readcomicsandroid.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnComicDetailsFetched
import com.jgasteiz.readcomicsandroid.models.Item
import com.stfalcon.frescoimageviewer.ImageViewer


class ReadingActivity : Activity() {

    private val LOG_TAG = ReadingActivity::class.java.simpleName
    private var mComic: Item? = null
    private var mImageViewer: ImageViewer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_reading)

        // Get the comic from the intent.
        mComic = intent.getSerializableExtra("comic") as Item

        if (mComic == null) {
            Log.e(LOG_TAG, "There was an error loading the comic.")
            Toast.makeText(this, "There was an error loading the comic, try a different one!", Toast.LENGTH_SHORT).show()
            return
        }

        // If the comic is offline, get its number of pages straight away.
        if (mComic!!.isComicOffline) {
            mComic!!.numPages = Utils.getOfflineComicNumPages(this, mComic!!)
            startReading()
        }
        // Otherwise, get the number of pages the hard way.
        else {
            Utils.fetchComicDetails(mComic!!.path, object : OnComicDetailsFetched {
                override fun callback(numPages: Int) {
                    mComic!!.numPages = numPages
                    startReading()
                }
            })
        }
    }

    /**
     * Load the first page of the comic and enter immersive mode.
     */
    private fun startReading() {
        loadPages()
        setImmersiveMode()
    }

    /**
     * Initialize the ImageViewer and load all the comic page urls on it.
     * If the comic is offline, a list of File Uris will be loaded, otherwise
     * the url of the actual remote page urls will be loaded.
     */
    private fun loadPages() {
        val pageUriList: ArrayList<String>
        if (mComic!!.isComicOffline) {
            pageUriList = Utils.getOfflineComicPageUriList(this, mComic!!)
        } else {
            pageUriList = Utils.getOnlineComicPageUriList(mComic!!)
        }
        // TODO: enable immersive mode on page touch.
        mImageViewer = ImageViewer
                .Builder(this, pageUriList)
                .setStartPosition(0)
                .show()
    }

    /**
     * Enable inmersive mode - hide both status and navigation bars.
     */
    private fun setImmersiveMode() {
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or // hide nav bar
                        View.SYSTEM_UI_FLAG_FULLSCREEN or // hide status bar
                        View.SYSTEM_UI_FLAG_IMMERSIVE
    }
}