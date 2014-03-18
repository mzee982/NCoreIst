package com.example.NCore;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class TorrentListAdapter extends ArrayAdapter<TorrentEntry> {

    // Adapter types
    public static final int TYPE_TORRENT_LIST = 1;
    public static final int TYPE_OTHER_VERSIONS = 2;

    // Item view types
    private static final int TORRENT_LIST_ITEM_VIEW_TYPE = 0;
    private static final int OTHER_VERSIONS_ITEM_VIEW_TYPE = 1;
    private static final int PROGRESS_ITEM_VIEW_TYPE = 2;

    // List item view tags
    public static final String TAG_PROGRESS_ITEM = "TAG_PROGRESS_ITEM";

    /**
     * ViewHolder
     */
    public static class ViewHolder {
        long id;
        ImageView category;
        TextView name;
        TextView title;
        ImageView imdbLogo;
        TextView imdb;
        ProgressBar imdbMeter;
        TextView size;
        TextView uploaded;
        TextView downloaded;
        TextView seeders;
        TextView leechers;
    }

    // Members
    private final LayoutInflater mInflater;
    private final Context mContext;
    private int mLastShowedPageIndex;
    private boolean mHasMoreResults;
    private int mAdapterType;

    /*
     * Constructor
     */

    public TorrentListAdapter(Context context, int adapterType) {
        super(context, android.R.layout.simple_list_item_1);

        mAdapterType = adapterType;
        mContext = context;

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Defaults
        mLastShowedPageIndex = 0;
        mHasMoreResults = false;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void setData(List<TorrentEntry> data) {
        clear();
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

    @Override
    public int getViewTypeCount() {

        /*
         * View types
         * - Torrent list item
         * - Other versions item
         * - Progress item
         */

        return 3;
    }

    @Override
    public int getItemViewType(int position) {

        // Torrent item list
        if (TYPE_TORRENT_LIST == mAdapterType) {

            // Torrent item view
            if (!hasMoreResults() || (position < (getCount() - 1))) {
                return TORRENT_LIST_ITEM_VIEW_TYPE;
            }

            // Progress item view
            else {
                return PROGRESS_ITEM_VIEW_TYPE;
            }

        }

        // Other versions list
        else if (TYPE_OTHER_VERSIONS == mAdapterType) return OTHER_VERSIONS_ITEM_VIEW_TYPE;

        // Ignore the item
        else return IGNORE_ITEM_VIEW_TYPE;

    }

    /**
     * Populate new items in the list.
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        switch (getItemViewType(position)) {
            case TORRENT_LIST_ITEM_VIEW_TYPE:
                return getTorrentListView(position, convertView, parent);
            case OTHER_VERSIONS_ITEM_VIEW_TYPE:
                return getOtherVersionsListView(position, convertView, parent);
            case PROGRESS_ITEM_VIEW_TYPE:
                return getProgressListView(position, convertView, parent);
            default:
                return null;
        }

    }

    public View getTorrentListView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // List row view and holder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.torrent_list_item, parent, false);

            holder = new ViewHolder();

            holder.category = (ImageView) convertView.findViewById(R.id.category_image);
            holder.name = (TextView) convertView.findViewById(R.id.name_value);
            holder.title = (TextView) convertView.findViewById(R.id.title_value);
            holder.imdbLogo = (ImageView) convertView.findViewById(R.id.imdb_logo);
            holder.imdb = (TextView) convertView.findViewById(R.id.imdb_value);
            holder.imdbMeter = (ProgressBar) convertView.findViewById(R.id.imdb_meter);
            holder.size = (TextView) convertView.findViewById(R.id.size_value);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Item to show
        TorrentEntry item = getItem(position);

        // Populate values to views
        if (item != null) {
            holder.id = item.getId();
            holder.category.setImageResource(TorrentDetails.getCategoryIconResId(item.getCategory()));
            holder.name.setText(item.getName());
            holder.title.setText(item.getTitle());
            holder.size.setText(item.getSize());

            // Hide IMDB section
            if (item.getImdb() == null) {
                holder.imdbLogo.setVisibility(ImageView.GONE);
                holder.imdb.setVisibility(TextView.GONE);
                holder.imdbMeter.setVisibility(ProgressBar.GONE);
            }

            // Show IMDB section
            else {
                holder.imdbLogo.setVisibility(ImageView.VISIBLE);
                holder.imdb.setVisibility(TextView.VISIBLE);
                holder.imdbMeter.setVisibility(ProgressBar.VISIBLE);

                // Calculate linear progress
                int progress = Math.round(item.getImdbValue() * 10);
                //int progress = (int) Math.round(Math.pow(item.getImdbValue(), 2) / Math.pow(10, 2) * 100);

                holder.imdb.setText(item.getImdb());
                holder.imdbMeter.setProgress(progress);
            }

        }

        // Background color of the odd/even row
        if (position % 2 == 0) {
            convertView.setBackgroundResource(R.drawable.torrent_list_item_even_selector);
        }
        else {
            convertView.setBackgroundResource(R.drawable.torrent_list_item_odd_selector);
        }

        return convertView;
    }

    public View getOtherVersionsListView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // List row view and holder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.other_versions_list_item, parent, false);

            holder = new ViewHolder();

            holder.category = (ImageView) convertView.findViewById(R.id.category_image);
            holder.name = (TextView) convertView.findViewById(R.id.name_value);
            holder.uploaded = (TextView) convertView.findViewById(R.id.uploaded_value);
            holder.size = (TextView) convertView.findViewById(R.id.size_value);
            holder.downloaded = (TextView) convertView.findViewById(R.id.downloaded_value);
            holder.seeders = (TextView) convertView.findViewById(R.id.seeders_value);
            holder.leechers = (TextView) convertView.findViewById(R.id.leechers_value);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Item to show
        TorrentEntry item = getItem(position);

        // Populate values to views
        if (item != null) {
            holder.id = item.getId();
            holder.category.setImageResource(TorrentDetails.getCategoryIconResId(item.getCategory()));
            holder.name.setText(item.getName());
            holder.uploaded.setText(item.getUploaded());
            holder.size.setText(item.getSize());
            holder.downloaded.setText(item.getDownloaded());
            holder.seeders.setText(item.getSeeders());
            holder.leechers.setText(item.getLeechers());
        }

        // Background color of the odd/even row
        if (position % 2 == 0) {
            convertView.setBackgroundResource(R.drawable.torrent_list_item_even_selector);
        }
        else {
            convertView.setBackgroundResource(R.drawable.torrent_list_item_odd_selector);
        }

        return convertView;
    }

    public View getProgressListView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.progress_list_item, parent, false);
            convertView.setTag(TAG_PROGRESS_ITEM);
        }

        // Background color of the odd/even row
//        if (position % 2 == 0) {
//            convertView.setBackgroundResource(R.drawable.torrent_list_item_even_selector);
//        }
//        else {
//            convertView.setBackgroundResource(R.drawable.torrent_list_item_odd_selector);
//        }

        return convertView;
    }


        @Override
    public void clear() {

        // Reset
        mLastShowedPageIndex = 0;
        mHasMoreResults = false;

        super.clear();

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
