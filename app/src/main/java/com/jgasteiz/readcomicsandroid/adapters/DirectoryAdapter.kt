package com.jgasteiz.readcomicsandroid.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.activities.DirectoryActivity
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
import java.util.*

class DirectoryAdapter(
        context: Context,
        itemList: ArrayList<Item>
) : ArrayAdapter<Item>(context, 0, itemList) {

    private val LOG_TAG = DirectoryAdapter::class.java.simpleName

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

            // Check if a comic is offline or not.
            if (Utils.isComicOffline(context, item)) {
                setRemoveButton(cView, item)
            } else {
                setDownloadButton(cView, item)
            }
        } else {
            downloadButtonView.visibility = View.GONE
        }

        return cView
    }

    /**
     * Change the action button to `download` a comic.
     * @param cView View instance
     * *
     * @param comic Comic instance
     */
    private fun setDownloadButton(cView: View, comic: Item) {
        val downloadButtonView = cView.findViewById<Button>(R.id.download_button)
        downloadButtonView.text = context.getString(R.string.download_comic)

        // Download the comic when the download button is clicked.
        downloadButtonView.setOnClickListener {
            val downloadComicButton = cView.findViewById(R.id.download_button) as Button
            val progressTextView = cView.findViewById(R.id.progress_text) as TextView

            downloadComicButton.visibility = View.GONE
            progressTextView.visibility = View.VISIBLE
            progressTextView.setText(R.string.downloading_comic)

            Utils.downloadComic(context, comic)

            // Check the download status.
            checkActiveDownload(comic, cView, progressTextView, downloadComicButton)
        }
    }

    /**
     * Change the action button to `remove` a downloaded comic.
     * @param convertView View instance
     * *
     * @param comic Comic instance
     */
    private fun setRemoveButton(convertView: View, comic: Item) {
        val downloadComicButton = convertView.findViewById(R.id.download_button) as Button
        downloadComicButton.text = context.getString(R.string.remove_download)

        // Remove the downloaded comic when the button is clicked.
        downloadComicButton.setOnClickListener {
            Utils.removeComicDownload(context, comic)
            setDownloadButton(convertView, comic)
        }
    }

    /**
     * Check whether a download is taking place for the given comic or not, and update its status.
     */
    private fun checkActiveDownload (comic: Item, convertView: View, progressTextView: TextView, downloadComicButton: Button) {
        if (Utils.downloads[comic.path] == null) {
            return
        }

        val timer = Timer("checkDownloadProgress")

        timer.schedule(object : TimerTask() {
            override fun run() {
                val progress = Utils.downloads[comic.path] as Int
                if (progress < 100) {
                    Log.d(LOG_TAG, "Download progress: $progress%")
                    (context as DirectoryActivity).runOnUiThread {
                        val percentage = "$progress%"
                        progressTextView.text = percentage
                    }
                } else {
                    Log.d(LOG_TAG, "Comic downloaded")
                    (context as DirectoryActivity).runOnUiThread {
                        setRemoveButton(convertView, comic)
                        progressTextView.visibility = View.GONE
                        downloadComicButton.visibility = View.VISIBLE
                    }
                    timer.cancel()
                }
            }
        }, 0, 1000)
    }
}