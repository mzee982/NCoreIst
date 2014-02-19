package com.example.NCore;

public class TorrentEntry {

    // Members
    private String mCategory;
    private String mName;
    private String mTitle;
    private String mImdb;

    /*
     * Constructor
     */

    public TorrentEntry() {
        mCategory = null;
        mName = null;
        mTitle = null;
        mImdb = null;
    }

    public TorrentEntry(String aCategory, String aName, String aTitle, String aImdb) {
        mCategory = aCategory;
        mName = aName;
        mTitle = aTitle;
        mImdb = aImdb;
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

    public String getImdb() {
        return mImdb;
    }

    public void setImdb(String aImdb) {
        mImdb = aImdb;
    }

    public boolean isEmpty() {
        return (mCategory == null) && (mName == null) && (mTitle == null);
    }

}
