package com.jgasteiz.readcomicsandroid.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnComicDetailsFetched
import com.jgasteiz.readcomicsandroid.models.Item
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import uk.co.senab.photoview.PhotoViewAttacher
import android.content.res.Configuration


class ReadingActivity : Activity() {

    private val LOG_TAG = ReadingActivity::class.java.simpleName

    private var mCurrentPageIndex = 0

    private var mPageImageView: ImageView? = null
    private var mAttacher: PhotoViewAttacher? = null
    private var mProgressBar: ProgressBar? = null
    private var mComic: Item? = null

    private var mPicassoCallback: Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reading_activity)

        // Get the comic from the intent.
        mComic = intent.getSerializableExtra("comic") as Item

        if (mComic == null) {
            Log.e(LOG_TAG, "There was an error loading the comic.")
            Toast.makeText(this, "There was an error loading the comic, go back!", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize the progress bar.
        mProgressBar = findViewById<ProgressBar>(R.id.progress_bar)
        mProgressBar!!.visibility = View.GONE
        // Initialize the main image view.
        mPageImageView = findViewById<ImageView>(R.id.active_page)
        // Attach a PhotoView attacher to it.
        mAttacher = PhotoViewAttacher(mPageImageView!!)
        // Set a custom single tap listener for navigating through the comic.
        mAttacher!!.onViewTapListener = onTapListener

        // If the comic is offline, get its number of pages straight away.
        if (Utils.isComicOffline(this, mComic!!)) {
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

    // TODO
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Fit width on landscape
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
        // Fit page on portrait
        else {

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
        mPageImageView!!.visibility = View.GONE
        mProgressBar!!.visibility = View.VISIBLE

        // If the comic is offline, load the page from the local storage.
        if (Utils.isComicOffline(this, mComic!!)) {
            val pageBitmap = Utils.getOfflineComicPage(this, pageNumber, mComic!!)
            if (pageBitmap != null) {
                mPageImageView!!.setImageBitmap(pageBitmap)
            } else {
                noMorePagesToLoad()
            }
            mProgressBar!!.visibility = View.GONE
            mPageImageView!!.visibility = View.VISIBLE
        }
        // Otherwise load it using Picasso
        else {
            Picasso
                .with(this)
                .load(Utils.getComicPageUrl(mComic!!, pageNumber))
                .into(mPageImageView!!, object : Callback {
                    override fun onSuccess() {
                        mProgressBar!!.visibility = View.GONE
                        mPageImageView!!.visibility = View.VISIBLE
                        mAttacher!!.update()
                    }
                    override fun onError() {
                        noMorePagesToLoad()
                        loadPageWithIndex(mCurrentPageIndex)
                    }
                })
        }
    }

    /**
     * Custom tap listener for loading the next or the previous comic page,
     * depending on where the user taps on the screen:
     * 15% left side of the screen -> previous page.
     * 15% right side of the screen -> next page.

     * @return PhotoViewAttacher.OnViewTapListener, custom tap listener.
     */
    private val onTapListener: PhotoViewAttacher.OnViewTapListener
        get() = PhotoViewAttacher.OnViewTapListener { view, x, y ->
            val touchRightPosition = (100 * x / view.width).toInt()

            if (touchRightPosition > 85) {
                loadNextPage()
            } else if (touchRightPosition < 15) {
                loadPreviousPage()
            } else {
                setImmersiveMode()
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
}