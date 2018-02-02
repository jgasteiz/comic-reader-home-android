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
import com.jgasteiz.readcomicsandroid.models.Item
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import uk.co.senab.photoview.PhotoViewAttacher


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

        // Initialize the progress bar.
        mProgressBar = findViewById<ProgressBar>(R.id.progress_bar)
        mProgressBar!!.visibility = View.GONE
        // Initialize the main image view.
        mPageImageView = findViewById<ImageView>(R.id.active_page)
        // Attach a PhotoView attacher to it.
        mAttacher = PhotoViewAttacher(mPageImageView!!)
        // Set a custom single tap listener for navigating through the comic.
        mAttacher!!.onViewTapListener = onTapListener

        // Load the first page.
        loadPageWithIndex(mCurrentPageIndex)

        // Start immersive mode for better reading.
        setImmersiveMode()
    }

    /**
     * Navigate to the next page of the comic.
     */
    private fun loadNextPage() {
        mCurrentPageIndex++
        // TODO: handle end of comic!
        loadPageWithIndex(mCurrentPageIndex)
    }

    /**
     * Navigate to the previous page of the comic.
     */
    private fun loadPreviousPage() {
        mCurrentPageIndex--
        if (mCurrentPageIndex < 0) {
            mCurrentPageIndex = 0
        }
        loadPageWithIndex(mCurrentPageIndex)
    }

    /**
     * Load the comic page with the given `pageIndex` index.
     * @param pageNumber Integer, index of the page to load.
     */
    private fun loadPageWithIndex(pageNumber: Int) {
        mPageImageView!!.visibility = View.GONE
        mProgressBar!!.visibility = View.VISIBLE

        if (mComic == null) {
            Log.e(LOG_TAG, "No comic to load!")
            return
        }

        // If the comic is offline, load the page from the local storage.
        if (Utils.isComicOffline(this, mComic!!)) {
            val pageBitmap = Utils.getComicOfflinePage(this, pageNumber, mComic!!)
            if (pageBitmap != null) {
                mPageImageView!!.setImageBitmap(pageBitmap)
            } else {
                Toast.makeText(this, "No more pages to load", Toast.LENGTH_SHORT).show()
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
                        Log.e(LOG_TAG, "An error occurred loading the page.")
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