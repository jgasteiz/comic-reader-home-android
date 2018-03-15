package com.jgasteiz.readcomicsandroid.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.services.DownloadsService
import android.os.Bundle
import com.jgasteiz.readcomicsandroid.helpers.Utils


abstract class BaseActivity : AppCompatActivity() {

    private val LOG_TAG = BaseActivity::class.java.simpleName

    abstract val hasRemovableItems: Boolean

    private var mServiceIntent: Intent? = null

    /**
     * Start a download for the given comic.
     */
    fun startDownload(comic: Item) {
        val bundle = Bundle()
        bundle.putSerializable("comic", comic)
        mServiceIntent = Intent(this, DownloadsService::class.java)
        mServiceIntent!!.putExtras(bundle)
        this.startService(mServiceIntent)
    }

    /**
     * Remove the given comic from the downloads.
     */
    fun removeDownload(comic: Item) {
        Utils.removeComicDownload(this, comic)
    }
}