package com.jgasteiz.readcomicsandroid.interfaces

import com.jgasteiz.readcomicsandroid.models.Item

interface OnDirectoryContentFetched {
    fun callback(itemList: ArrayList<Item>)
}