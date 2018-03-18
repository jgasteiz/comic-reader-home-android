package com.jgasteiz.readcomicsandroid.activities

import android.support.v7.app.AppCompatActivity
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.helpers.Utils


abstract class BaseActivity : AppCompatActivity() {

    abstract val hasRemovableItems: Boolean

    /**
     * Remove the given comic from the downloads.
     */
    fun removeDownload(comic: Item) {
        Utils.removeComicDownload(this, comic)
    }
}