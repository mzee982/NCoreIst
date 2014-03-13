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

    /**
     * Populate new items in the list.
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (TYPE_TORRENT_LIST == mAdapterType) return getTorrentListView(position, convertView, parent);
        else if (TYPE_OTHER_VERSIONS == mAdapterType) return getOtherVersionsView(position, convertView, parent);
        else return null;

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
            holder.category.setImageResource(getIconResIdByTorrentCategory(item.getCategory()));
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

    public View getOtherVersionsView(int position, View convertView, ViewGroup parent) {
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
            holder.category.setImageResource(getIconResIdByTorrentCategory(item.getCategory()));
            holder.name.setText(item.getName());
            holder.uploaded.setText(item.getUploaded());
            holder.size.setText(item.getSize());
            holder.downloaded.setText("D:" + item.getDownloaded());
            holder.seeders.setText("S:" + item.getSeeders());
            holder.leechers.setText("L:" + item.getLeechers());
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

    private int getIconResIdByTorrentCategory(String torrentCategory) {

        // Movie
        if ("xvid_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xvid_hun;
        if ("xvid".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xvid;
        if ("dvd_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvd_hun;
        if ("dvd".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvd;
        if ("dvd9_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvd9_hun;
        if ("dvd9".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvd9;
        if ("hd_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_hd_hun;
        if ("hd".equals(torrentCategory)) return R.drawable.ic_torrent_cat_hd;

        // Series
        if ("xvidser_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xvidser_hun;
        if ("xvidser".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xvidser;
        if ("dvdser_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvdser_hun;
        if ("dvdser".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvdser;
        if ("hdser_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_hdser_hun;
        if ("hdser".equals(torrentCategory)) return R.drawable.ic_torrent_cat_hdser;

        // Music
        if ("mp3_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_mp3_hun;
        if ("mp3".equals(torrentCategory)) return R.drawable.ic_torrent_cat_mp3;
        if ("lossless_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_lossless_hun;
        if ("lossless".equals(torrentCategory)) return R.drawable.ic_torrent_cat_lossless;
        if ("clip".equals(torrentCategory)) return R.drawable.ic_torrent_cat_clip;

        // XXX
        if ("xxx_xvid".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xxx_xvid;
        if ("xxx_dvd".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xxx_dvd;
        if ("xxx_imageset".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xxx_imageset;
        if ("xxx_hd".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xxx_hd;

        // Game
        if ("game_iso".equals(torrentCategory)) return R.drawable.ic_torrent_cat_game_iso;
        if ("game_rip".equals(torrentCategory)) return R.drawable.ic_torrent_cat_game_rip;
        if ("console".equals(torrentCategory)) return R.drawable.ic_torrent_cat_console;

        // Software
        if ("iso".equals(torrentCategory)) return R.drawable.ic_torrent_cat_iso;
        if ("misc".equals(torrentCategory)) return R.drawable.ic_torrent_cat_misc;
        if ("mobil".equals(torrentCategory)) return R.drawable.ic_torrent_cat_mobil;

        // Book
        if ("ebook_hun".equals(torrentCategory)) return R.drawable.ic_torrent_cat_ebook_hun;
        if ("ebook".equals(torrentCategory)) return R.drawable.ic_torrent_cat_ebook;

        //
        return android.R.drawable.ic_dialog_alert;
    }

}
