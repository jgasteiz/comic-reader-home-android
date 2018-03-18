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
import android.util.Base64
import java.nio.charset.Charset


object Utils {

    private val LOG_TAG = Utils::class.java.simpleName

    // TODO: move this to some settings.
    val SERVER_ADDRESS = "192.168.0.28"
    val DIRECTORY_API_URL = "http://${SERVER_ADDRESS}/api/directory/"
    val COMIC_DETAIL_API_URL = "http://${SERVER_ADDRESS}/api/comic/"
    val PAGE_API_URL = "http://${SERVER_ADDRESS}/api/page/"

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
                            .mapTo(itemList) {
                                val comic = Item(it, ItemType.COMIC)
                                comic.isComicOffline = Utils.isComicOffline(context, comic)
                                comic
                            }
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
    fun getOfflineComicPage(context: Context, pageNumber: Int, comic: Item): Bitmap? {
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
     * Get the number of pages
     */
    fun getOfflineComicNumPages(context: Context, comic: Item): Int {
        return getComicDirectory(context, comic).listFiles().count()
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

    /**
     * Get the list of offline/downloaded comics.
     */
    fun getDownloadedComics(context: Context): ArrayList<Item> {
        val comicList = ArrayList<Item>()

        val downloadedDirectories = context.filesDir.listFiles()
        (0 until downloadedDirectories.count())
                .map { downloadedDirectories.get(it) as File }
                .mapTo(comicList) {
                    val name = Base64.decode(it.name, android.util.Base64.DEFAULT)
                    val decodedPath = String(name, Charset.defaultCharset())
                    val decodedName = decodedPath.split("/").last()
                    val comic = Item(decodedName, it.name, ItemType.COMIC)
                    comic.isComicOffline = Utils.isComicOffline(context, comic)
                    comic
                }

        // Sort them by name.
        comicList.sortBy { it.name }
        return comicList
    }
}
