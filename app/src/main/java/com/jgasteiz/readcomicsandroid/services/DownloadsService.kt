package com.jgasteiz.readcomicsandroid.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.jgasteiz.readcomicsandroid.helpers.DownloadComicAsyncTask
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.interfaces.OnComicDetailsFetched
import com.jgasteiz.readcomicsandroid.interfaces.OnComicDownloaded
import com.jgasteiz.readcomicsandroid.interfaces.OnPageDownloaded
import com.jgasteiz.readcomicsandroid.models.Item
import java.util.*


class DownloadsService: IntentService("DownloadsService") {

    companion object {
        val DOWNLOAD_NOTIFICATION_ID = 1
        val LOG_TAG = DownloadsService::class.java.simpleName
    }

    var downloads: HashMap<String, Int> = HashMap()

    override fun onHandleIntent(intent: Intent?) {
        val bundle = intent?.getExtras()
        val comic: Item = bundle?.getSerializable("comic") as Item
        this.downloadComic(comic)
    }

    // TODO: Make this work so we can broadcast download progress.
    // Defines and instantiates an object for handling status updates.
//    private val mBroadcaster = BroadcastNotifier(this)

    /**
     * Download a comic.
     */
    fun downloadComic (comic: Item) {
        downloads[comic.path] = 0
        val context = this

        // TODO: Do this properly
//        @SuppressLint("NewApi")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationHelper = NotificationHelper(context)
//            val notification = notificationHelper.getNotification("Downloading ${comic.name}", "Downloading ${comic.name}")
//            notificationHelper.notify(DOWNLOAD_NOTIFICATION_ID, notification)
//        }

        // First, get the number of pages of the comic.
        Utils.fetchComicDetails(comic.path, object : OnComicDetailsFetched {
            override fun callback(numPages: Int) {

                comic.numPages = numPages

                // When done, download the actual comic.
                val task = DownloadComicAsyncTask(
                        context,
                        comic,
                        object : OnPageDownloaded {
                            override fun callback(percentage: Int) {
                                downloads[comic.path] = percentage
                                Log.d(LOG_TAG, "Download progress: ${percentage}%")
                            }
                        }, object : OnComicDownloaded {
                    override fun callback() {
                        downloads[comic.path] = 100
                        Log.d(LOG_TAG, "Download progress: 100%")
                    }
                }
                )
                task.execute()
            }
        })
    }
}