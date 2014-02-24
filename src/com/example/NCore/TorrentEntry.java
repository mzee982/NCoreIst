package com.example.NCore;

public class TorrentEntry {

    // Properties
    private long id;
    private String category;
    private String name;
    private String title;
    private String imdb;
    private String size;

    /*
     * Constructor
     */

    public TorrentEntry() {
        id = -1L;
        category = null;
        name = null;
        title = null;
        imdb = null;
    }

    public TorrentEntry(long aId, String aCategory, String aName, String aTitle, String aImdb) {
        this.id = aId;
        category = aCategory;
        name = aName;
        title = aTitle;
        imdb = aImdb;
    }

    /*
     * Accessors
     */

    public String getCategory() {
        return category;
    }

    public void setCategory(String aCategory) {
        category = aCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String aName) {
        name = aName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String aTitle) {
        title = aTitle;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String aImdb) {
        imdb = aImdb;
    }

    public long getId() {
        return id;
    }

    public void setId(long aId) {
        id = aId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String aSize) {
        size = aSize;
    }

    public boolean isEmpty() {
        return (category == null) && (name == null) && (title == null);
    }

}
