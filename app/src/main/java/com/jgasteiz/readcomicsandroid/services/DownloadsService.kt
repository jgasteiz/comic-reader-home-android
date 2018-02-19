package com.jgasteiz.readcomicsandroid.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.models.Item
import java.util.*


class DownloadsService: Service() {

    private val mBinder = LocalBinder()
    private val mGenerator = Random()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal// Return this instance of LocalService so clients can call public methods
        val getService: DownloadsService
            get() = this@DownloadsService
    }

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }

    fun downloadComic(comic: Item) {
        // TODO: move all the download logic to the Service.
        Utils.downloadComic(this, comic)
    }
}