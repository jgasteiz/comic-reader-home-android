package com.jgasteiz.readcomicsandroid.views

import android.content.Context
import android.widget.TextView
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.jgasteiz.readcomicsandroid.R


class PageOverlayView : RelativeLayout {

    private var title: TextView? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun setOverlayText(overlayText: String) {
        title!!.text = overlayText
    }

    private fun init() {
        val view = View.inflate(context, R.layout.view_image_overlay, this)
        title = view.findViewById(R.id.comicTitle)
    }
}