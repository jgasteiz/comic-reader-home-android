package com.jgasteiz.readcomicsandroid.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnComicDetailsFetched
import com.jgasteiz.readcomicsandroid.models.Item
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


class ReadingActivity : Activity() {

    private val LOG_TAG = ReadingActivity::class.java.simpleName

    private var mPageIndex = 0

    // The actual comic item will be passed through the intent extras.
    private lateinit var mComic: Item
    // Image view of the page.
    private lateinit var mPageImageView: ImageView
    // Scroll view of the page.
    private lateinit var mPageScrollView: ScrollView
    // Progress bar
    private lateinit var mProgressBar: ProgressBar
    // List of page Uris of the current comic
    private lateinit var mPageUriList: ArrayList<Uri>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_reading)

        // Initialize the page image view and progress bar.
        mPageImageView = findViewById<ImageView>(R.id.pageImageView)
        mPageScrollView = findViewById<ScrollView>(R.id.pageScrollView)
        mProgressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Setup the gesture detector.
        mPageImageView.setOnTouchListener({ _, event ->
            GestureDetector(this, ReaderGestureListener()).onTouchEvent(event)
        })


        mComic = intent.getSerializableExtra("comic") as Item

        // If the comic is offline, get its number of pages straight away.
        if (mComic.isComicOffline) {
            mComic.numPages = Utils.getOfflineComicNumPages(this, mComic)
            startReading()
        }
        // Otherwise, get the number of pages the hard way.
        else {
            Utils.fetchComicDetails(mComic.path, object : OnComicDetailsFetched {
                override fun callback(numPages: Int) {
                    mComic.numPages = numPages
                    startReading()
                }
            })
        }
    }

    /**
     * Get the comic list of pages and load the first page.
     */
    private fun startReading() {
        mPageUriList = if (mComic.isComicOffline) {
            Utils.getOfflineComicPageUriList(this, mComic)
        } else {
            Utils.getOnlineComicPageUriList(mComic)
        }
        loadPage()
        setImmersiveMode()
    }

    /**
     * Load the page in the mPageIndex index.
     */
    private fun loadPage() {
        val pageUri = mPageUriList[mPageIndex]
        // If the comic is offline, load the Uri straight away.
        if (mComic.isComicOffline) {
            mPageImageView.setImageURI(pageUri)
            mPageScrollView.scrollTo(0, 0)
        }
        // Otherwise, load it using Picasso
        else {
            mPageImageView.visibility = View.GONE
            mProgressBar.visibility = View.VISIBLE
            Picasso
                .with(this)
                .load(pageUri)
                .into(mPageImageView, object : Callback {
                    override fun onSuccess() {
                        mProgressBar.visibility = View.GONE
                        mPageImageView.visibility = View.VISIBLE
                        mPageScrollView.scrollTo(0, 0)
                    }
                    override fun onError() {
                        mProgressBar.visibility = View.GONE
                        mPageImageView.visibility = View.VISIBLE
                        noMorePagesToLoad()
                    }
                })

        }
    }

    private fun setImmersiveMode() {
        // Set the immersive mode.
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or // hide nav bar
                View.SYSTEM_UI_FLAG_FULLSCREEN or // hide status bar
                View.SYSTEM_UI_FLAG_IMMERSIVE
    }

    private fun noMorePagesToLoad() {
        Toast.makeText(this, "No more pages to load", Toast.LENGTH_SHORT).show()
    }

        /**
     * Class for detecting gestures on the mSimpleDraweeView.
     */
    internal inner class ReaderGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            Log.d("TAG", "onDown: ")
            val touchRightPosition = (100 * event.x / mPageImageView.width).toInt()
            when {
                touchRightPosition > 75 -> loadNextPage()
                touchRightPosition < 25 -> loadPreviousPage()
                else -> setImmersiveMode()
            }
            return true
        }
    }

    /**
     * Navigate to the next page of the comic.
     */
    private fun loadNextPage() {
        mPageIndex++
        if (mPageIndex > mPageUriList.size - 1) {
            noMorePagesToLoad()
            mPageIndex = mPageUriList.size - 1
        } else {
            loadPage()
        }
    }

    /**
     * Navigate to the previous page of the comic.
     */
    private fun loadPreviousPage() {
        mPageIndex--
        if (mPageIndex < 0) {
            mPageIndex = 0
        } else {
            loadPage()
        }
    }


}
