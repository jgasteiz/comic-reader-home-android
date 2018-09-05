package com.jgasteiz.readcomicsandroid.models

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

enum class ItemType {
    COMIC, DIRECTORY;
}

class Item : Serializable {
    private val LOG_TAG = Item::class.java.simpleName

    var pk: Int = 0
    lateinit var name: String
    lateinit var path: String
    var parentDirectory: Item? = null
    var type: ItemType
    // Flags to be used by the adapter.
    var isComicOffline = false
    var isComicDownloading = false
    var downloadProgress = ""

    // Only used when downloading or reading a comic.
    var numPages: Int? = null

    constructor(jsonObject: JSONObject, type: ItemType) {
        this.type = type
        try {
            this.pk = jsonObject.get("pk") as Int
            this.name = jsonObject.get("name") as String
            this.path = jsonObject.get("path") as String
        } catch (e: JSONException) {
            Log.e(LOG_TAG, e.message)
        }
    }

    constructor(pk: Int?, name: String?, path: String?, type: ItemType?) {
        this.pk = pk!!
        this.name = name!!
        this.path = path!!
        this.type = type!!
    }

    constructor(pk: Int?, name: String?, path: String?, parentDirectory: Item?, type: ItemType?) {
        this.pk = pk!!
        this.name = name!!
        this.path = path!!
        this.parentDirectory = parentDirectory
        this.type = type!!
    }
}
