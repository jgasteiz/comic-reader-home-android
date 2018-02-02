package com.jgasteiz.readcomicsandroid.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.util.Log
import com.jgasteiz.readcomicsandroid.interfaces.*
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*


object Utils {

    private val LOG_TAG = Utils::class.java.simpleName

    var downloads: HashMap<String, Int> = HashMap()

    // TODO: move this to some settings.
    private val DIRECTORY_API_URL = "http://192.168.0.28/api/directory/"
    private val COMIC_DETAIL_API_URL = "http://192.168.0.28/api/comic/"
    private val PAGE_API_URL = "http://192.168.0.28/api/page/"

    /**
     * Returns whether there's a network available.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    /**
     * Fetch all the directories and comics inside a directory.
     * @param onDirectoryContentFetched callback
     */
    fun fetchDirectoryDetails(context: Context, directoryPath: String?, onDirectoryContentFetched: OnDirectoryContentFetched) {

        val itemList = ArrayList<Item>()

        val task = GetStringResponseAsyncTask(object : OnResponseFetched {
            override fun callback(response: String) {
                Log.d(LOG_TAG, response)
                try {
                    val responseJson = JSONObject(response)
                    val pathContentsJson = responseJson.get("path_contents") as JSONObject
                    val comicsJson = pathContentsJson.get("comics") as JSONArray
                    val directoriesJson = pathContentsJson.get("directories") as JSONArray

                    (0 until comicsJson.length())
                            .map { comicsJson.get(it) as JSONObject }
                            .mapTo(itemList) { Item(it, ItemType.COMIC) }
                    (0 until directoriesJson.length())
                            .map { directoriesJson.get(it) as JSONObject }
                            .mapTo(itemList) { Item(it, ItemType.DIRECTORY) }

                } catch (e: JSONException) {
                    Log.e(LOG_TAG, e.message)
                }

                onDirectoryContentFetched.callback(itemList)
            }
        })

        // If a directory path is given, fetch its details.
        if (directoryPath != null) {
            task.execute(String.format("%s%s/", DIRECTORY_API_URL, directoryPath))
        }
        // Otherwise just fetch the root directory.
        else {
            task.execute(DIRECTORY_API_URL)
        }
    }

    /**
     * Fetch all the directories and comics inside a directory.
     * @param onComicDetailsFetched callback
     */
    fun fetchComicDetails(comicPath: String, onComicDetailsFetched: OnComicDetailsFetched) {
        val task = GetStringResponseAsyncTask(object : OnResponseFetched {
            override fun callback(response: String) {
                var numPages = -1

                Log.d(LOG_TAG, response)
                try {
                    val responseJson = JSONObject(response)
                    numPages = responseJson.get("num_pages") as Int
                } catch (e: JSONException) {
                    Log.e(LOG_TAG, e.message)
                }

                onComicDetailsFetched.callback(numPages)
            }
        })

        task.execute(String.format("%s%s/", COMIC_DETAIL_API_URL, comicPath))
    }

    /**
     * Return the url for downloading the given comic on the given page number.
     */
    fun getComicPageUrl(comic: Item, pageNumber: Int): String {
        return String.format("%s%s/%s", PAGE_API_URL, comic.path, pageNumber)
    }

    /**
     * Return the downloaded page of the given comic with the given page number.
     */
    fun getComicOfflinePage(context: Context, pageNumber: Int, comic: Item): Bitmap? {
        // Return the offline page
        val comicDirectory = getComicDirectory(context, comic)

        if (pageNumber < comicDirectory.listFiles().count() && pageNumber > -1) {
            val pageFile = comicDirectory.listFiles()[pageNumber]
            if (pageFile.exists()) {
                return BitmapFactory.decodeFile(pageFile.absolutePath)
            }
        }
        return null
    }

    /**
     * Download the given comic.
     */
    fun downloadComic (context: Context, comic: Item) {
        downloads[comic.path] = 0

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
                        }
                    }, object : OnComicDownloaded {
                        override fun callback() {
                            downloads[comic.path] = 100
                        }
                    }
                )
                task.execute()
            }
        })
    }

    /**
     * Checks if a given comic has been downloaded and return true if it has.
     * @param context Context instance
     * *
     * @return true if the comic is offline, false if not.
     */
    fun isComicOffline(context: Context, comic: Item): Boolean {
        // A comic is offline if its directory exists and either it's download is complete or
        // there's no download record of it.
        return getComicDirectory(context, comic).exists()
                && (!downloads.containsKey(comic.path)
                    || downloads[comic.path] == 100)
    }

    /**
     * Return the comic directory.
     * @param context Context instance
     * *
     * @return File instance for the comic directory
     */
    fun getComicDirectory(context: Context, comic: Item): File {
        val comicDirectoryPath = String.format("%s%s%s", context.filesDir, File.separator, comic.path)
        return File(comicDirectoryPath)
    }

    /**
     * Delete the downloaded comic pages.
     * @param comic Comic instance
     */
    fun removeComicDownload(context: Context, comic: Item) {
        val comicDirectory = getComicDirectory(context, comic)
        if (comicDirectory.isDirectory) {
            comicDirectory.deleteRecursively()
            Log.d(LOG_TAG, "Directory `%s` deleted".format(comicDirectory.absolutePath))
        }
    }
}
