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
import android.app.Activity
import com.jgasteiz.readcomicsandroid.helpers.Constants


class DownloadsService: IntentService("DownloadsService") {

    companion object {
        val LOG_TAG = DownloadsService::class.java.simpleName
    }

    override fun onHandleIntent(intent: Intent?) {
        val bundle = intent?.getExtras()
        val comic: Item = bundle?.getSerializable("comic") as Item
        this.downloadComic(comic)

        // Construct an Intent tying it to the ACTION_DOWNLOAD_START
        val broadcastIntent = Intent(Constants.ACTION_DOWNLOAD_START)
        broadcastIntent.putExtra("resultCode", Activity.RESULT_OK)
        broadcastIntent.putExtra("resultValue", "Download started")
        broadcastIntent.putExtra("comic", comic)
        sendBroadcast(broadcastIntent)
    }

    /**
     * Download a comic.
     */
    fun downloadComic (comic: Item) {
        val context = this

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
                                Log.d(LOG_TAG, "Download progress: ${percentage}%")
                            }
                        }, object : OnComicDownloaded {
                    override fun callback() {
                        Log.d(LOG_TAG, "Download progress: 100%")
                        val broadcastIntent = Intent(Constants.ACTION_DOWNLOAD_END)
                        broadcastIntent.putExtra("resultCode", Activity.RESULT_OK)
                        broadcastIntent.putExtra("resultValue", "Download finished")
                        broadcastIntent.putExtra("comic", comic)
                        sendBroadcast(broadcastIntent)
                    }
                }
                )
                task.execute()
            }
        })
    }
}