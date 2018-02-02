package com.jgasteiz.readcomicsandroid.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.models.Item
import java.util.ArrayList

class DirectoryAdapter(
        context: Context,
        itemList: ArrayList<Item>
) : ArrayAdapter<Item>(context, 0, itemList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cView = convertView
        // Get the data item for this position
        val item = getItem(position)

        // Inflate the view if necessary.
        if (cView == null) {
            cView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }

        // Get references to the text views.
        val itemTitleView = cView!!.findViewById<TextView>(R.id.item_name)

        // Set the comic/direcotry name
        itemTitleView.text = item.name

        return cView
    }
}