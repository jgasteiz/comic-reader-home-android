package com.jgasteiz.readcomicsandroid.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.jgasteiz.readcomicsandroid.interfaces.OnDirectoryContentFetched
import com.jgasteiz.readcomicsandroid.interfaces.OnResponseFetched
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


object Utils {

    private val LOG_TAG = Utils::class.java.simpleName

    private val DIRECTORY_API_URL = "http://192.168.0.28/api/directory/"
    private val PAGE_API_URL = "http://192.168.0.28/api/page/"

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

    fun getPageUrl(comic: Item, pageNumber: Int): String {
        return String.format("%s%s/%s", PAGE_API_URL, comic.path, pageNumber)
    }
}
