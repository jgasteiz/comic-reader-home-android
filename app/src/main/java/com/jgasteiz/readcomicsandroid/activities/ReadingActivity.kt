package com.jgasteiz.readcomicsandroid.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.MotionEvent
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
import kotlin.math.max


class ReadingActivity : Activity() {

    private val LOG_TAG = ReadingActivity::class.java.simpleName

    private var mCurrentPageIndex = 0

    private var mPageImageView: ImageView? = null
    private var mAttacher: PhotoViewAttacher? = null
    private var mProgressBar: ProgressBar? = null
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

        // Initialize the progress bar.
        mProgressBar = findViewById<ProgressBar>(R.id.progress_bar)
        mProgressBar!!.visibility = View.GONE
        // Initialize the main image view.
        mPageImageView = findViewById<ImageView>(R.id.active_page)
        // Attach a PhotoView attacher to it.
        mAttacher = PhotoViewAttacher(mPageImageView!!)
        // Set a custom single tap listener for navigating through the comic.
        mAttacher!!.setOnDoubleTapListener(OnDoubleTapListener())

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

        // DEBUG Scale buttons
        // val fitWidthButton = findViewById<Button>(R.id.fit_width)
        // fitWidthButton.setOnClickListener { fitWidth() }
        // val fitHeightButton = findViewById<Button>(R.id.fit_height)
        // fitHeightButton.setOnClickListener { fitHeight() }
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
                mAttacher!!.update()
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
     * Custom double tap listener for:
     * 1 - onSingleTapConfirmed loading the next or the previous comic page,
     * depending on where the user taps on the screen:
     * 15% left side of the screen -> previous page.
     * 15% right side of the screen -> next page.
     * 2 - fit to width/height or back to scale 1.
     * @return GestureDetector.OnDoubleTapListener, custom tap listener.
     */
    private inner class OnDoubleTapListener : GestureDetector.OnDoubleTapListener {
        override fun onDoubleTap(p0: MotionEvent?): Boolean {
            val fitWidthScale = getFitWidthScale()
            val fitHeightScale = getFitHeightScale()

            if (fitWidthScale != null && fitHeightScale != null) {
                val biggestScale = max(fitWidthScale, fitHeightScale)
                if (biggestScale > mAttacher!!.scale) {
                    fitToScale(biggestScale)
                } else {
                    fitToScale(1F)
                }
            }
            return true
        }

        override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
            val touchRightPosition = (100 * p0!!.x / mPageImageView!!.width).toInt()
            when {
                touchRightPosition > 85 -> loadNextPage()
                touchRightPosition < 15 -> loadPreviousPage()
                else -> setImmersiveMode()
            }
            return true
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

    // Aspect ratio related functions

    private fun getImageViewValues(): ArrayList<Float>? {
        val imageViewWidth = mAttacher?.imageView?.width?.toFloat()
        val imageViewHeight = mAttacher?.imageView?.height?.toFloat()
        Log.d(LOG_TAG, imageViewWidth.toString())
        Log.d(LOG_TAG, imageViewHeight.toString())

        val pageWidth = mAttacher?.imageView?.drawable?.intrinsicWidth?.toFloat()
        val pageHeight = mAttacher?.imageView?.drawable?.intrinsicHeight?.toFloat()
        Log.d(LOG_TAG, pageWidth.toString())
        Log.d(LOG_TAG, pageHeight.toString())

        if (imageViewWidth == null || imageViewHeight == null || pageWidth == null || pageHeight == null) {
            return null
        }
        val result = ArrayList<Float>()
        result.add(imageViewWidth)
        result.add(imageViewHeight)
        result.add(pageWidth)
        result.add(pageHeight)
        return result
    }

    private fun getFitWidthScale(): Float? {
        val imageViewValues = getImageViewValues() ?: return null
        val imageViewWidth = imageViewValues[0]
        val imageViewHeight = imageViewValues[1]
        val pageWidth = imageViewValues[2]
        val pageHeight = imageViewValues[3]

        val imageViewAspectRatio = imageViewWidth / imageViewHeight
        val pageProportions = pageWidth / pageHeight

        // If the image view aspect ratio is lower than the page, we
        // know scale 1 will be fit width.
        return if (imageViewAspectRatio <= pageProportions) {
            1F
        } else {
            // We know the image is fit to height.
            val pageScale = pageHeight / imageViewHeight
            // Calculate how much width of the image view is being used by the page.
            val pageWidthOnImageView = pageWidth / pageScale
            // Calculate the new scale we need to set in order to cover the
            // image view width with the page.
            imageViewWidth / pageWidthOnImageView
        }
    }

    private fun getFitHeightScale(): Float? {
        val imageViewValues = getImageViewValues() ?: return null
        val imageViewWidth = imageViewValues[0]
        val imageViewHeight = imageViewValues[1]
        val pageWidth = imageViewValues[2]
        val pageHeight = imageViewValues[3]

        val imageViewAspectRatio = imageViewWidth / imageViewHeight
        val pageProportions = pageWidth / pageHeight

        // If the image view aspect ratio is higher than the page, we
        // know scale 1 will be fit width.
        return if (imageViewAspectRatio >= pageProportions) {
            1F
        } else {
            // We know the image is fit to width.
            val pageScale = pageWidth / imageViewWidth
            // Calculate how much height of the image view is being used by the page.
            val pageHeightOnImageView = pageHeight / pageScale
            // Calculate the new scale we need to set in order to cover the
            // image view height with the page.
            imageViewHeight / pageHeightOnImageView
        }
    }

    private fun fitWidth() {
        val newScale = getFitWidthScale()?: return
        fitToScale(newScale)
    }

    private fun fitHeight() {
        val newScale = getFitHeightScale()?: return
        fitToScale(newScale)
    }

    private fun fitToScale(scale:Float) {
        if (mAttacher?.maximumScale!! < scale) {
            mAttacher?.maximumScale = scale * 2
        }
        mAttacher?.setScale(scale, true)
    }
}