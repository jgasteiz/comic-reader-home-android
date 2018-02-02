package com.jgasteiz.readcomicsandroid.adapters

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
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
        val downloadButtonView = cView.findViewById<Button>(R.id.download_button)

        // Set the comic/direcotry name
        itemTitleView.text = item.name

        // Show the download button if the item is a comic.
        if (item.type == ItemType.COMIC) {
            downloadButtonView.visibility = View.VISIBLE
        }

        // Download the comic when the download button is clicked.
        downloadButtonView.setOnClickListener {
            val downloadComicButton = cView.findViewById(R.id.download_button) as Button
            val progressTextView = cView.findViewById(R.id.progress_text) as TextView

            downloadComicButton.visibility = View.GONE
            progressTextView.visibility = View.VISIBLE
            progressTextView.setText(R.string.downloading_comic)

            // TODO
            // Utils.downloadComic(context, comic)
        }

        return cView
    }
}