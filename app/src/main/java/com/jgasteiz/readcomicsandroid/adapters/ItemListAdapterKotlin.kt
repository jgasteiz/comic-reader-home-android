package com.jgasteiz.readcomicsandroid.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.jgasteiz.readcomicsandroid.R
import com.jgasteiz.readcomicsandroid.activities.BaseActivity
import com.jgasteiz.readcomicsandroid.models.Item
import com.jgasteiz.readcomicsandroid.models.ItemType
import kotlinx.android.synthetic.main.list_item.view.*
import kotlin.collections.ArrayList


class ItemListAdapterKotlin(private val context: BaseActivity,
                            private val itemList: ArrayList<Item>,
                            private val onItemClick: (Item) -> Unit,
                            private val onDownloadClick: ((Item) -> Unit)?,
                            private val onRemoveClick: (Item) -> Boolean) : RecyclerView.Adapter<ItemListAdapterKotlin.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bindItem(item, context)

        if (item.type == ItemType.COMIC) {
            if (item.isComicDownloading) {
                setDownloadInProgress(holder.itemView, item)
            } else if (item.isComicOffline) {
                setRemoveButton(holder.itemView, item)
            } else {
                setDownloadButton(holder.itemView, item)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(view: View, private val onItemClick: (Item) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindItem(item: Item, context: BaseActivity) {
            with(item) {
                itemView.item_name.text = item.name
                itemView.setOnClickListener { onItemClick(this) }

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
        val progressText = cView.findViewById<TextView>(R.id.progress_text)
        progressText.visibility = View.GONE
        val downloadButton = cView.findViewById<Button>(R.id.action_button)
        downloadButton.visibility = View.VISIBLE
        downloadButton.text = context.getString(R.string.download_comic)

        // Download the comic when the download button is clicked.
        downloadButton.setOnClickListener { onDownloadClick?.invoke(comic) }
    }

    /**
     * Change the action button to `download` a comic.
     * @param cView View instance
     * *
     * @param comic Comic instance
     */
    private fun setDownloadInProgress(cView: View, comic: Item) {
        val downloadButton = cView.findViewById<Button>(R.id.action_button)
        downloadButton.visibility = View.GONE
        val progressText = cView.findViewById<TextView>(R.id.progress_text)
        progressText.visibility = View.VISIBLE
        progressText.text = comic.downloadProgress
    }

    /**
     * Change the action button to `remove` a downloaded comic.
     * @param convertView View instance
     * *
     * @param comic Comic instance
     */
    private fun setRemoveButton(cView: View, comic: Item) {
        val progressText = cView.findViewById<TextView>(R.id.progress_text)
        progressText.visibility = View.GONE
        val removeComicButton = cView.findViewById<Button>(R.id.action_button)
        removeComicButton.visibility = View.VISIBLE
        removeComicButton.text = context.getString(R.string.remove_download)

        // Remove the downloaded comic when the button is clicked.
        removeComicButton.setOnClickListener {
            if (onRemoveClick(comic)) {
                notifyDataSetChanged()
            } else {
                setDownloadButton(cView, comic)
            }
        }
    }

}
