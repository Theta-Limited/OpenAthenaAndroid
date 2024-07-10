// DemListAdapter.java
// Bobby Krupczak with ChatGPT
// rdk@theta.limited
// implement an adapter for our list/table of DEMs
// that we will manage with DemCacheListActivity

package com.openathena;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DemListAdapter extends RecyclerView.Adapter<DemListAdapter.ViewHolder>
{
    public static String TAG = DemListAdapter.class.getSimpleName();

    private DemCache demCache;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    DemListAdapter(Context context, DemCache demCache) {
        this.mInflater = LayoutInflater.from(context);
        this.demCache = demCache;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String filename = demCache.cache.get(position).filename;
        holder.myTextView.setText(filename);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return demCache.cache.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    DemCache.DemCacheEntry getItem(int id) {
        return demCache.cache.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void removeItem(int position)
    {
        Log.d(TAG,"DemCacheAdapter: removing item "+position);
        demCache.removeCacheEntry(position);
        notifyItemRemoved(position);
    }

} // DemListAdapter
