package com.example.NCore;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
        TextView imdb;
        TextView size;
        TextView uploaded;
        TextView downloaded;
        TextView seeders;
        TextView leechers;
    }

    // Members
    private final LayoutInflater mInflater;
    private int mLastShowedPageIndex;
    private boolean mHasMoreResults;
    private int mAdapterType;

    /*
     * Constructor
     */

    public TorrentListAdapter(Context context, int adapterType) {
        super(context, android.R.layout.simple_list_item_1);

        mAdapterType = adapterType;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

            holder.category = (ImageView) convertView.findViewById(R.id.imageCategory);
            holder.name = (TextView) convertView.findViewById(R.id.textName);
            holder.title = (TextView) convertView.findViewById(R.id.textTitle);
            holder.imdb = (TextView) convertView.findViewById(R.id.textImdb);
            holder.size = (TextView) convertView.findViewById(R.id.textSize);

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
            holder.imdb.setText(item.getImdb());
            holder.size.setText(item.getSize());
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

            holder.category = (ImageView) convertView.findViewById(R.id.imageCategory);
            holder.name = (TextView) convertView.findViewById(R.id.textName);
            holder.uploaded = (TextView) convertView.findViewById(R.id.textUploaded);
            holder.size = (TextView) convertView.findViewById(R.id.textSize);
            holder.downloaded = (TextView) convertView.findViewById(R.id.textDownloaded);
            holder.seeders = (TextView) convertView.findViewById(R.id.textSeeders);
            holder.leechers = (TextView) convertView.findViewById(R.id.textLeechers);

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
