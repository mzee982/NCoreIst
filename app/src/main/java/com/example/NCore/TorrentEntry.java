package com.example.NCore;

public class TorrentEntry {

    // Properties
    private long id;
    private String category;
    private String name;
    private String title;
    private String imdb;
    private Float imdbValue;
    private String size;
    private String uploaded;
    private String downloaded;
    private String seeders;
    private String leechers;

    /*
     * Constructor
     */

    public TorrentEntry() {
        id = -1L;
        category = null;
        name = null;
        title = null;
        imdb = null;
        imdbValue = null;
        uploaded = null;
        downloaded = null;
        seeders = null;
        leechers = null;
    }

    public boolean isEmpty() {
        return (category == null) && (name == null) && (title == null);
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

    public Float getImdbValue() {
        return imdbValue;
    }

    public void setImdbValue(Float imdbValue) {
        this.imdbValue = imdbValue;
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

    public String getUploaded() {
        return uploaded;
    }

    public void setUploaded(String uploaded) {
        this.uploaded = uploaded;
    }

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }

    public String getSeeders() {
        return seeders;
    }

    public void setSeeders(String seeders) {
        this.seeders = seeders;
    }

    public String getLeechers() {
        return leechers;
    }

    public void setLeechers(String leechers) {
        this.leechers = leechers;
    }

}
