package com.jgasteiz.readcomicsandroid.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.activities.BaseActivity
import com.jgasteiz.readcomicsandroid.activities.DirectoryActivity
import com.jgasteiz.readcomicsandroid.helpers.Utils
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
import kotlinx.android.synthetic.main.list_item.view.*
import java.util.*
import kotlin.collections.ArrayList


class ItemListAdapter(private val context: BaseActivity,
                      private val itemList: ArrayList<Item>,
                      private val itemClick: (Item) -> Unit) : RecyclerView.Adapter<ItemListAdapter.ViewHolder>()
{

    private val LOG_TAG = ItemListAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(itemList[position], context)

        if (Utils.isComicOffline(context, itemList[position])) {
            setRemoveButton(holder.itemView, itemList[position])
        } else {
            setDownloadButton(holder.itemView, itemList[position])
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(view: View, private val itemClick: (Item) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindItem(item: Item, context: BaseActivity) {
            with(item) {
                itemView.item_name.text = item.name
                itemView.setOnClickListener { itemClick(this) }

                if (item.type == ItemType.COMIC) {
                    itemView.action_button.visibility = View.VISIBLE
                } else {
                    itemView.action_button.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Change the action button to `download` a comic.
     * @param cView View instance
     * *
     * @param comic Comic instance
     */
    private fun setDownloadButton(cView: View, comic: Item) {
        val downloadButton = cView.findViewById<Button>(R.id.action_button)
        downloadButton.text = context.getString(R.string.download_comic)

        // Download the comic when the download button is clicked.
        downloadButton.setOnClickListener {
            val downloadComicButton = cView.findViewById<Button>(R.id.action_button)
            val progressTextView = cView.findViewById<TextView>(R.id.progress_text)

            downloadComicButton.visibility = View.GONE
            progressTextView.visibility = View.VISIBLE
            progressTextView.setText(R.string.downloading_comic)

            context.mService?.downloadComic(comic)

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
        val removeComicButton = convertView.findViewById<Button>(R.id.action_button)
        removeComicButton.text = context.getString(R.string.remove_download)

        // Remove the downloaded comic when the button is clicked.
        removeComicButton.setOnClickListener {
            Utils.removeComicDownload(context, comic)
            if (context.hasRemovableItems) {
                itemList.remove(comic)
                notifyDataSetChanged()
            } else {
                setDownloadButton(convertView, comic)
            }
        }
    }

    /**
     * Check whether a download is taking place for the given comic or not, and update its status.
     * TODO: do this in the background, it currently blocks the main thread.
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