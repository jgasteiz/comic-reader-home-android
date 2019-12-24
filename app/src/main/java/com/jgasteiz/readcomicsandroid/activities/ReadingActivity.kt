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
import com.jgasteiz.readcomicsandroid.views.PageOverlayView
import com.stfalcon.frescoimageviewer.ImageViewer


class ReadingActivity : Activity() {

    private val LOG_TAG = ReadingActivity::class.java.simpleName

    // The actual comic item will be passed through the intent extras.
    private var mComic: Item? = null

    // Image viewer used for navigating through the pages
    private var mImageViewer: ImageViewer? = null

    // The page overlay view will display the comic title and the page number
    private var mPageOverlayView: PageOverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_reading)

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
            Utils.fetchComicDetails(mComic!!.pk, object : OnComicDetailsFetched {
                override fun callback(numPages: Int) {
                    mComic!!.numPages = numPages
                    startReading()
                }
            })
        }
    }

    /**
     * Initialize the ImageViewer with the comic pages.
     */
    private fun startReading() {
        // Get the list of pages
        val pageUriList = if (mComic!!.isComicOffline) {
            Utils.getOfflineComicPageUriList(this, mComic!!)
        } else {
            Utils.getOnlineComicPageUriList(mComic!!)
        }

        // Initialize the page overlay view with the comic title.
        mPageOverlayView = PageOverlayView(this)
        mPageOverlayView!!.setOverlayText(mComic!!.name)

        // Initialize the ImageViewer.
        mImageViewer = ImageViewer
                .Builder(this, pageUriList)
                .setStartPosition(0)
                .hideStatusBar(true)
                // We want to close the activity when the image viewer is dismissed.
                .setOnDismissListener({
                    this.finish()
                })
                // Set the page overlay and update the page overlay view with the page number
                // on page changes.
                .setOverlayView(mPageOverlayView)
                .setImageChangeListener({
                    val overlayText = "${mComic!!.name} - ${it + 1}/${mComic!!.numPages!!}"
                    mPageOverlayView!!.setOverlayText(overlayText)
                })
                .show()

        // Set the immersive mode.
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or // hide nav bar
                View.SYSTEM_UI_FLAG_FULLSCREEN or // hide status bar
                View.SYSTEM_UI_FLAG_IMMERSIVE
    }
}
