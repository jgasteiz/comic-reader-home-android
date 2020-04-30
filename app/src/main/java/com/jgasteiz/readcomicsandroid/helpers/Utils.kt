package com.jgasteiz.readcomicsandroid.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import com.jgasteiz.readcomicsandroid.interfaces.*
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import android.util.Base64
import java.nio.charset.Charset
import kotlin.collections.ArrayList


object Utils {

    private val LOG_TAG = Utils::class.java.simpleName

    // TODO: move this to some settings.
    val SERVER_ADDRESS = "192.168.0.42"
    val FILE_API_URL = "http://${SERVER_ADDRESS}/api/fileitems/"
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
    fun fetchDirectoryDetails(context: Context, directoryPk: Int?, onDirectoryContentFetched: OnDirectoryContentFetched) {

        val itemList = ArrayList<Item>()

        val task = GetStringResponseAsyncTask(object : OnResponseFetched {
            override fun callback(response: String) {
                Log.d(LOG_TAG, response)
                try {
                    var responseJson: JSONObject?
                    try {
                        responseJson = JSONObject(response)
                    } catch (e: Exception) {
                        val responseJsonArray = JSONArray(response)
                        responseJson = responseJsonArray[0] as JSONObject
                    }
                    if (responseJson == null) {
                        Log.e(LOG_TAG, "There's no response to parse")
                        onDirectoryContentFetched.callback(itemList)
                        return
                    }

                    val childrenJson = responseJson.get("children") as JSONArray

                    (0 until childrenJson.length())
                            .map { childrenJson.get(it) as JSONObject }
                            .mapTo(itemList) {
                                if (it.get("file_type") == "directory") {
                                    Item(it, ItemType.DIRECTORY)
                                } else {
                                    val comic = Item(it, ItemType.COMIC)
                                    comic.isComicOffline = Utils.isComicOffline(context, comic)
                                    comic
                                }
                            }

                } catch (e: JSONException) {
                    Log.e(LOG_TAG, e.message)
                }

                onDirectoryContentFetched.callback(itemList)
            }
        })

        // If a directory path is given, fetch its details.
        if (directoryPk != null) {
            task.execute(String.format("%s%s/?format=json", FILE_API_URL, directoryPk))
        }
        // Otherwise just fetch the root directory.
        else {
            task.execute(FILE_API_URL)
        }
    }

    /**
     * Fetch the details of a comic.
     * @param onComicDetailsFetched callback
     */
    fun fetchComicDetails(comicPk: Int, onComicDetailsFetched: OnComicDetailsFetched) {
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

        task.execute(String.format("%s%s/?format=json", FILE_API_URL, comicPk))
    }

    /**
     * Return the url for downloading the given comic on the given page number.
     */
    fun getComicPageUrl(comic: Item, pageNumber: Int): String {
        return String.format("%s%s/%s/?format=json", PAGE_API_URL, comic.pk, pageNumber)
    }

    /**
     * Get a list of page urls for an online comic.
     */
    fun getOnlineComicPageUriList(comic: Item): ArrayList<String> {
        val pageList = ArrayList<String>()
        for (i in 0 until comic.numPages!!) {
            pageList.add(getComicPageUrl(comic, i))
        }
        return pageList
    }

    /**
     * Get a list of page urls for a downloaded comic.
     */
    fun getOfflineComicPageUriList(context: Context, comic: Item): ArrayList<String> {
        val comicDirectory = getComicDirectory(context, comic)

        val pageList = ArrayList<String>()
        for (i in 0 until comic.numPages!!) {
            val pageFile = comicDirectory.listFiles()[i]
            if (pageFile.exists()) {
                pageList.add(Uri.fromFile(pageFile).toString())
            }
        }
        return pageList
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
                .map { downloadedDirectories[it] as File }
                .mapTo(comicList) {
                    // TODO: fix this
                    val name = Base64.decode(it.name, android.util.Base64.DEFAULT)
                    val decodedPath = String(name, Charset.defaultCharset())
                    val decodedName = decodedPath.split("/").last()
                    val comic = Item(0, decodedName, it.name, ItemType.COMIC)
                    comic.isComicOffline = Utils.isComicOffline(context, comic)
                    comic
                }

        // Sort them by name.
        comicList.sortBy { it.name }
        return comicList
    }
}
