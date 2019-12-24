package com.jgasteiz.readcomicsandroid.helpers

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.jgasteiz.readcomicsandroid.interfaces.OnComicDownloaded
import com.jgasteiz.readcomicsandroid.interfaces.OnPageDownloaded
import com.jgasteiz.readcomicsandroid.models.Item
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DownloadComicAsyncTask internal constructor(
        context: Context,
        private val mComic: Item,
        private val mOnPageDownloaded: OnPageDownloaded,
        private val mOnComicDownloaded: OnComicDownloaded
) : AsyncTask<Void, Int, Boolean>() {

    private val LOG_TAG = DownloadComicAsyncTask::class.java.simpleName
    private val mFilesDir: File = context.filesDir

    /**
     * Download the pages of the given comic in the internal storage.
     */
    override fun doInBackground(vararg params: Void): Boolean {
        // Create a directory for the comic
        val comicDirectoryPath = "$mFilesDir${java.io.File.separator}${mComic.path}"
        val comicDirectory = File(comicDirectoryPath)
        val directoryCreated = comicDirectory.mkdirs()
        if (directoryCreated) {
            Log.d(LOG_TAG, "Comic directory ${comicDirectory.absolutePath} created")
        }

        if (mComic.numPages == null) {
            Log.d(LOG_TAG, "The comic doesn't have any pages, nothing to download")
            return false
        }

        for (i in 0 until mComic.numPages!!) {
            try {
                val pageUrl = Utils.getComicPageUrl(mComic, i)

                // Download a single page.
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(pageUrl).build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw IOException("Failed to download file: $response")
                    }
                    val fileNameParts = pageUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val fileName = fileNameParts[fileNameParts.size - 1]
                    val fos = FileOutputStream("$comicDirectoryPath${java.io.File.separator}$fileName")
                    fos.write(response.body!!.bytes())
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Update the progress.
                publishProgress(i * 100 / mComic.numPages!!)
            } catch (e: Exception) {
                Log.d(LOG_TAG, "Ooops, something happened")
                return false
            }
        }
        return true
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val percentage: Int = when {
            values[0] == null -> 100
            values[0]!! > 100 -> 100
            else -> values[0]!!
        }
        mOnPageDownloaded.callback(percentage)
    }

    override fun onPostExecute(success: Boolean) {
        mOnComicDownloaded.callback()
    }

}
