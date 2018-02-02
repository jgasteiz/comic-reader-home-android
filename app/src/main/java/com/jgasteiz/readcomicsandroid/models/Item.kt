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

    var name: String? = null
    var path: String? = null
    var parentDirectory: Item? = null
    var type: ItemType? = ItemType.COMIC

    constructor(jsonObject: JSONObject, type: ItemType) {
        this.type = type
        try {
            this.name = jsonObject.get("name") as String
            this.path = jsonObject.get("path") as String
        } catch (e: JSONException) {
            Log.e(LOG_TAG, e.message)
        }
    }

    constructor(name: String?, path: String?, parentDirectory: Item?, type: ItemType?) {
        this.name = name
        this.path = path
        this.parentDirectory = parentDirectory
        this.type = type
    }
}