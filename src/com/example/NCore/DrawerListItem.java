package com.example.NCore;

import android.content.Context;
import android.content.res.TypedArray;

import java.util.ArrayList;

public class DrawerListItem {

    // Drawer item types
    public static final String DRAWER_ITEM_TYPE_HEADER = "DRAWER_ITEM_TYPE_HEADER";
    public static final String DRAWER_ITEM_TYPE_TITLE = "DRAWER_ITEM_TYPE_TITLE";
    public static final String DRAWER_ITEM_TYPE_ITEM = "DRAWER_ITEM_TYPE_ITEM";

    // Drawer item indexes
    public static final int DRAWER_ITEM_INDEX_CATEGORIES = 2;
    public static final int DRAWER_ITEM_INDEX_SEARCH = 3;
    public static final int DRAWER_ITEM_INDEX_TOPLIST = 4;
    public static final int DRAWER_ITEM_INDEX_SETTINGS = 6;
    public static final int DRAWER_ITEM_INDEX_ABOUT = 7;

    // Properties
    private String type;
    private int icon;
    private String label;

    public DrawerListItem(String type, int icon, String label) {
        this.type = type;
        this.icon = icon;
        this.label = label;
    }

    public static ArrayList<DrawerListItem> buildDrawerItemList(Context context, NCoreSession session,
                                        int drawerTypesResource, int drawerIconsResource, int drawerLabelsResource) {
        ArrayList<DrawerListItem> drawerListItems = new ArrayList<DrawerListItem>();

        // Load resources
        String[] drawerTypes = context.getResources().getStringArray(drawerTypesResource);
        TypedArray drawerIcons = context.getResources().obtainTypedArray(drawerIconsResource);
        String[] drawerLabels = context.getResources().getStringArray(drawerLabelsResource);

        // Build the list
        for (int i = 0; i < drawerTypes.length; i++) {
            DrawerListItem item;

            // HEADER
            if (DRAWER_ITEM_TYPE_HEADER.equals(drawerTypes[i])) {
                item = new DrawerListItem(drawerTypes[i], drawerIcons.getResourceId(i, -1),
                        session.getLoginName().toUpperCase());
            }

            // TITLE
            else if (DRAWER_ITEM_TYPE_TITLE.equals(drawerTypes[i])) {
                item = new DrawerListItem(drawerTypes[i], drawerIcons.getResourceId(i, -1),
                        drawerLabels[i].toUpperCase());
            }

            // ITEM
            else {
                item = new DrawerListItem(drawerTypes[i], drawerIcons.getResourceId(i, -1), drawerLabels[i]);
            }

            drawerListItems.add(item);
        }

        // Recycle
        drawerIcons.recycle();

        return drawerListItems;
    }

    public boolean isHeader() {
        return DRAWER_ITEM_TYPE_HEADER.equals(type);
    }

    public boolean isTitle() {
        return DRAWER_ITEM_TYPE_TITLE.equals(type);
    }

    public boolean isItem() {
        return DRAWER_ITEM_TYPE_ITEM.equals(type);
    }

    /*
     * Accessors
     */

    public String getType() {
        return type;
    }

    public int getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

}
