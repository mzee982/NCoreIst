package com.example.NCore;

public class TorrentEntry {

    // Members
    private long mId;
    private String mCategory;
    private String mName;
    private String mTitle;
    private String mImdb;

    /*
     * Constructor
     */

    public TorrentEntry() {
        mId = -1L;
        mCategory = null;
        mName = null;
        mTitle = null;
        mImdb = null;
    }

    public TorrentEntry(long id, String aCategory, String aName, String aTitle, String aImdb) {
        mId = id;
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

    public long getId() {
        return mId;
    }

    public void setId(long aId) {
        mId = aId;
    }

    public boolean isEmpty() {
        return (mCategory == null) && (mName == null) && (mTitle == null);
    }

}
