package com.example.NCore;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TorrentListAdapter extends ArrayAdapter<TorrentEntry> {

    /**
     * ViewHolder
     */
    private static class ViewHolder {
        ImageView category;
        TextView name;
        TextView title;
        TextView imdb;
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
        ViewHolder holder;

        // List row view and holder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.torrent_list_item, parent, false);

            holder = new ViewHolder();

            holder.category = (ImageView) convertView.findViewById(R.id.imageCategory);
            holder.name = (TextView) convertView.findViewById(R.id.textName);
            holder.title = (TextView) convertView.findViewById(R.id.textTitle);
            holder.imdb = (TextView) convertView.findViewById(R.id.textImdb);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Item to show
        TorrentEntry item = getItem(position);

        // Populate values to views
        if (item != null) {
            holder.category.setImageResource(getIconResIdByTorrentCategory(item.getCategory()));
            holder.name.setText(item.getName());
            holder.title.setText(item.getTitle());
            holder.imdb.setText(item.getImdb());
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
        if ("XviD/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xvid_hun;
        if ("XviD/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xvid;
        if ("DVDR/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvd_hun;
        if ("DVDR/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvd;
        if ("DVD9/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvd9_hun;
        if ("DVD9/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvd9;
        if ("HD/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_hd_hun;
        if ("HD/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_hd;

        // Series
        if ("XviD/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xvidser_hun;
        if ("XviD/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xvidser;
        if ("DVDR/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvdser_hun;
        if ("DVDR/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_dvdser;
        if ("HD/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_hdser_hun;
        if ("HD/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_hdser;

        // Music
        if ("MP3/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_mp3_hun;
        if ("MP3/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_mp3;
        if ("Lossless/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_lossless_hun;
        if ("Lossless/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_lossless;
        if ("Klip".equals(torrentCategory)) return R.drawable.ic_torrent_cat_clip;

        // XXX
        if ("XviD".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xxx_xvid;
        if ("DVDR".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xxx_dvd;
        if ("Imageset".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xxx_imageset;
        if ("HD".equals(torrentCategory)) return R.drawable.ic_torrent_cat_xxx_hd;

        // Game
        if ("PC/ISO".equals(torrentCategory)) return R.drawable.ic_torrent_cat_game_iso;
        if ("PC/RIP".equals(torrentCategory)) return R.drawable.ic_torrent_cat_game_rip;
        if ("Konzol".equals(torrentCategory)) return R.drawable.ic_torrent_cat_console;

        // Software
        if ("Prog/ISO".equals(torrentCategory)) return R.drawable.ic_torrent_cat_iso;
        if ("Prog/RIP".equals(torrentCategory)) return R.drawable.ic_torrent_cat_misc;
        if ("Prog/Mobil".equals(torrentCategory)) return R.drawable.ic_torrent_cat_mobil;

        // Book
        if ("eBook/HU".equals(torrentCategory)) return R.drawable.ic_torrent_cat_ebook_hun;
        if ("eBook/EN".equals(torrentCategory)) return R.drawable.ic_torrent_cat_ebook;

        //
        return android.R.drawable.ic_dialog_alert;
    }

}
