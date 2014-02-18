package com.example.NCore;

public class TorrentEntry {

    // Members
    private String mCategory;
    private String mName;
    private String mTitle;

    /*
     * Constructor
     */

    public TorrentEntry() {
        mCategory = null;
        mName = null;
        mTitle = null;
    }

    public TorrentEntry(String aCategory, String aName, String aTitle) {
        mCategory = aCategory;
        mName = aName;
        mTitle = aTitle;
    }

    /*
     * Accessors
     */

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String aCategory) {
        mCategory = aCategory;
    }

    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String aTitle) {
        mTitle = aTitle;
    }

    public boolean isEmpty() {
        return (mCategory == null) && (mName == null) && (mTitle == null);
    }

}
