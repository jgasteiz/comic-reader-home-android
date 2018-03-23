package com.jgasteiz.readcomicsandroid.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jgasteiz.readcomicsandroid.R;
import com.jgasteiz.readcomicsandroid.activities.BaseActivity;
import com.jgasteiz.readcomicsandroid.models.Item;
import com.jgasteiz.readcomicsandroid.models.ItemType;

import java.util.ArrayList;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    private ArrayList<Item> mItemList;
    private BaseActivity mContext;
    private View.OnClickListener mOnItemClick;
    private View.OnClickListener mOnDownloadClick;
    private View.OnClickListener mOnRemoveClick;

    public ItemListAdapter(BaseActivity context, ArrayList<Item> itemList, View.OnClickListener onItemClick, View.OnClickListener onDownloadClick, View.OnClickListener onRemoveClick) {
        mItemList = itemList;
        mContext = context;
        mOnItemClick = onItemClick;
        mOnDownloadClick = onDownloadClick;
        mOnRemoveClick = onRemoveClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view, mOnItemClick);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = mItemList.get(position);
        holder.bindItem(item, mContext);

//        TODO
//        if (item.type == ItemType.COMIC) {
//            if (item.isComicDownloading) {
//                setDownloadInProgress(holder.itemView, item)
//            } else if (item.isComicOffline) {
//                setRemoveButton(holder.itemView, item)
//            } else {
//                setDownloadButton(holder.itemView, item)
//            }
//        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    // TODO
    private void setDownloadButton() {

    }
    // TODO
    private void setDownloadInProgress() {

    }
    // TODO
    private void setRemoveButton() {

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View.OnClickListener onItemClick;

        ViewHolder(View itemView, View.OnClickListener onItemClick) {
            super(itemView);
            this.onItemClick = onItemClick;
        }

        void bindItem(Item item, BaseActivity context) {
            ((TextView) itemView.findViewById(R.id.item_name)).setText(item.name);
            itemView.setOnClickListener(onItemClick);

            if (item.getType() == ItemType.COMIC) {
                itemView.findViewById(R.id.action_button).setVisibility(View.VISIBLE);
            } else {
                itemView.findViewById(R.id.action_button).setVisibility(View.GONE);
            }
        }
    }
}
