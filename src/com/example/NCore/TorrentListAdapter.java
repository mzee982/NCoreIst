package com.example.NCore;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TorrentListAdapter extends ArrayAdapter<TorrentEntry> {

    /**
     * ViewHolder
     */
    private static class ViewHolder {
        TextView category;
        TextView name;
        TextView title;
    }

    // Members
    private final LayoutInflater mInflater;
    private int mLastShowedPageIndex;
    private boolean mHasMoreResults;

    /*
     * Constructor
     */

    public TorrentListAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mLastShowedPageIndex = 0;
    }

    public void setData(List<TorrentEntry> data) {
        clear();
        mHasMoreResults = false;
        mLastShowedPageIndex = 0;

        appendData(data);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void appendData(List<TorrentEntry> data) {
        if (data != null) {

            //If the platform supports it, use addAll, otherwise add in loop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                addAll(data);
            } else {
                for(TorrentEntry item: data) {
                    add(item);
                }
            }

            mLastShowedPageIndex++;
        }
    }

    /**
     * Populate new items in the list.
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.torrent_list_item, parent, false);

            holder = new ViewHolder();

            holder.category = (TextView) convertView.findViewById(R.id.text_category);
            holder.name = (TextView) convertView.findViewById(R.id.text_name);
            holder.title = (TextView) convertView.findViewById(R.id.text_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TorrentEntry item = getItem(position);

        if (item != null) {
            holder.category.setText(item.getCategory());
            holder.name.setText(item.getName());
            holder.title.setText(item.getTitle());
        }

        return convertView;
    }

    public int getLastShowedPageIndex() {
        return mLastShowedPageIndex;
    }

    public void setHasMoreResults(boolean aMore) {
        mHasMoreResults = aMore;
    }

    public boolean hasMoreResults() {
        return mHasMoreResults;
    }

}
