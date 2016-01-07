package com.mzee982.android.ncoreist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DrawerListAdapter extends BaseAdapter {

    // Item view types
    private static final int DRAWER_LIST_HEADER_VIEW_TYPE = 0;
    private static final int DRAWER_LIST_TITLE_VIEW_TYPE = 1;
    private static final int DRAWER_LIST_ITEM_VIEW_TYPE = 2;

    /**
     * ViewHolder
     */
    public static class ViewHolder {
        ImageView icon;
        TextView label;
    }

    // Members
    private final LayoutInflater mInflater;
    private final Context mContext;
    private ArrayList<DrawerListItem> mDrawerListItems;

    public DrawerListAdapter(Context context, ArrayList<DrawerListItem> drawerListItems) {
        this.mContext = context;
        this.mDrawerListItems = drawerListItems;

        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDrawerListItems.size();
    }

    @Override
    public DrawerListItem getItem(int position) {
        return mDrawerListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isItem();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {

        // HEADER
        if (mDrawerListItems.get(position).isHeader())
            return DRAWER_LIST_HEADER_VIEW_TYPE;

        // TITLE
        else if (mDrawerListItems.get(position).isTitle())
            return DRAWER_LIST_TITLE_VIEW_TYPE;

        // ITEM
        else if (mDrawerListItems.get(position).isItem())
            return DRAWER_LIST_ITEM_VIEW_TYPE;

        // Ignore the item
        else
            return IGNORE_ITEM_VIEW_TYPE;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Inflate new list item view and create holder object
        if (convertView == null) {
            holder = new ViewHolder();

            switch (getItemViewType(position)) {

                // HEADER
                case DRAWER_LIST_HEADER_VIEW_TYPE:
                    convertView = mInflater.inflate(R.layout.drawer_list_header, parent, false);
                    holder.icon = (ImageView) convertView.findViewById(R.id.drawer_list_header_icon);
                    holder.label = (TextView) convertView.findViewById(R.id.drawer_list_header_label);
                    break;

                // TITLE
                case DRAWER_LIST_TITLE_VIEW_TYPE:
                    convertView = mInflater.inflate(R.layout.drawer_list_title, parent, false);
                    holder.label = (TextView) convertView.findViewById(R.id.drawer_list_title_label);
                    break;

                // ITEM
                case DRAWER_LIST_ITEM_VIEW_TYPE:
                    convertView = mInflater.inflate(R.layout.drawer_list_item, parent, false);
                    holder.icon = (ImageView) convertView.findViewById(R.id.drawer_list_item_icon);
                    holder.label = (TextView) convertView.findViewById(R.id.drawer_list_item_label);
                    break;

            }

            // Store holder object in created view
            convertView.setTag(holder);
        }

        // Retrieve holder object from view
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Item to show
        DrawerListItem item = getItem(position);

        // Populate
        switch (getItemViewType(position)) {

            // HEADER
            case DRAWER_LIST_HEADER_VIEW_TYPE:

                if (item.getIcon() > -1)
                    holder.icon.setImageResource(item.getIcon());
                else
                    holder.icon.setImageDrawable(null);

                holder.label.setText(item.getLabel());

                break;

            // TITLE
            case DRAWER_LIST_TITLE_VIEW_TYPE:
                holder.label.setText(item.getLabel());
                break;

            // ITEM
            case DRAWER_LIST_ITEM_VIEW_TYPE:

                if (item.getIcon() > -1)
                    holder.icon.setImageResource(item.getIcon());
                else
                    holder.icon.setImageDrawable(null);

                holder.label.setText(item.getLabel());

                break;

        }

        return convertView;
    }

}
