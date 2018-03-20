package com.jgasteiz.readcomicsandroid.activities

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.drawee.view.SimpleDraweeView
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnComicDetailsFetched
import com.jgasteiz.readcomicsandroid.models.Item
import android.graphics.PointF
import com.facebook.drawee.drawable.ScalingUtils
import android.view.GestureDetector
import android.view.MotionEvent


class ReadingActivity : Activity() {

    private val LOG_TAG = ReadingActivity::class.java.simpleName
    private var mCurrentPageIndex = 0
    private var mDraweeViewGestureDetector: GestureDetector? = null
    private var mSimpleDraweeView: SimpleDraweeView? = null
    private var mComic: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_reading)

        // Get the comic from the intent.
        mComic = intent.getSerializableExtra("comic") as Item

        if (mComic == null) {
            Log.e(LOG_TAG, "There was an error loading the comic.")
            Toast.makeText(this, "There was an error loading the comic, go back!", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize the main image view.
        mSimpleDraweeView = findViewById(R.id.active_page)

        // Initialize the gesture detector.
        mDraweeViewGestureDetector = GestureDetector(this, ReaderGestureListener())

        // Set the touch listener and let the gesture detector deal with it.
        // TODO: remove the performClick warning
        mSimpleDraweeView?.setOnTouchListener({ v, event ->
            mDraweeViewGestureDetector!!.onTouchEvent(event)
        })

        // Set the initial scale type.
        setScaleType(resources.configuration.orientation)

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
     * On device rotation, reset the scale type.
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig != null) {
            setScaleType(newConfig.orientation)
        }
    }

    /**
     * Set the scale type depending on the device orientation.
     * Portrait: fit the page.
     * Landscape: fit width, focus on top.
     */
    private fun setScaleType(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mSimpleDraweeView?.hierarchy?.actualImageScaleType = ScalingUtils.ScaleType.CENTER_INSIDE
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mSimpleDraweeView?.hierarchy?.actualImageScaleType = ScalingUtils.ScaleType.FOCUS_CROP
            mSimpleDraweeView?.hierarchy?.setActualImageFocusPoint(PointF(0.5f, 0f))
        }
    }

    /**
     * Load the first page of the comic and enter immersive mode.
     */
    private fun startReading() {
        mCurrentPageIndex = 0
        loadPageWithIndex(mCurrentPageIndex)
        setImmersiveMode()
    }

    /**
     * Navigate to the next page of the comic.
     */
    private fun loadNextPage() {
        mCurrentPageIndex++
        if (mCurrentPageIndex > mComic!!.numPages!! - 1) {
            noMorePagesToLoad()
        } else {
            loadPageWithIndex(mCurrentPageIndex)
        }
    }

    /**
     * Navigate to the previous page of the comic.
     */
    private fun loadPreviousPage() {
        mCurrentPageIndex--
        if (mCurrentPageIndex < 0) {
            mCurrentPageIndex = 0
        } else {
            loadPageWithIndex(mCurrentPageIndex)
        }
    }

    /**
     * Decrease the current page index to the minimum between "1 page less" and "total num pages in
     * the comic" and show a toast saying there are no more pages. Also, adjust the num pages of
     * the comic to the new maximum.
     * This can happen in 3 places:
     * 1. by going forward page after page and going over the max number of pages.
     * 2. by not having downloaded the right amount of files.
     * 3. api not returning the right count of pages in the comic.
     */
    private fun noMorePagesToLoad() {
        mCurrentPageIndex = minOf(mCurrentPageIndex - 1, mComic!!.numPages!! - 1)
        mComic!!.numPages = mCurrentPageIndex + 1
        Toast.makeText(this, "No more pages to load", Toast.LENGTH_SHORT).show()
    }

    /**
     * Load the comic page with the given `pageIndex` index.
     * @param pageNumber Integer, index of the page to load.
     */
    private fun loadPageWithIndex(pageNumber: Int) {
        // If the comic is offline, load the page from the local storage.
        if (mComic!!.isComicOffline) {
            val pageUri = Utils.getOfflineComicPageUri(this, pageNumber, mComic!!)
            if (pageUri != null) {
                mSimpleDraweeView!!.setImageURI(pageUri)
            } else {
                noMorePagesToLoad()
            }
        } else {
            mSimpleDraweeView?.setImageURI(Utils.getComicPageUrl(mComic!!, pageNumber))
        }
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

    /**
     * Class for detecting gestures on the mSimpleDraweeView.
     */
    internal inner class ReaderGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            Log.d("TAG", "onDown: ")
            val touchRightPosition = (100 * event.x / mSimpleDraweeView!!.width).toInt()
            when {
                touchRightPosition > 85 -> loadNextPage()
                touchRightPosition < 15 -> loadPreviousPage()
                else -> setImmersiveMode()
            }
            return true
        }
    }
}