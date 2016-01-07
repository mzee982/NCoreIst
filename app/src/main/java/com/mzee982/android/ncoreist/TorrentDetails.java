package com.mzee982.android.ncoreist;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class TorrentDetails {

    // Movie properties
    private static final String MOVIE_PROPERTY_1080P = "1080p";
    private static final String MOVIE_PROPERTY_720P = "720p";

    // Properties
    private String name;
    private String type;
    private String category;
    private String uploaded;
    private String uploader;
    private String seeders;
    private String leechers;
    private String downloaded;
    private String speed;
    private String size;
    private String files;
    private String title;
    private String releaseDate;
    private String director;
    private String cast;
    private String country;
    private String duration;
    private String labels;
    private String imdb;
    private String coverUrl;
    private Bitmap coverImg;
    private String sampleImg1Url;
    private String sampleImg2Url;
    private String sampleImg3Url;
    private String sampleLargeImg1Url;
    private String sampleLargeImg2Url;
    private String sampleLargeImg3Url;
    private Bitmap sampleImg1;
    private Bitmap sampleImg2;
    private Bitmap sampleImg3;
    private String otherVersionsId;
    private String otherVersionsFid;
    private List<TorrentEntry> otherVersions;
    private List<Integer> moviePropertyIcons;

    /*
     * Constructor
     */

    public TorrentDetails() {
        name = null;
        type = null;
        category = null;
        uploaded = null;
        uploader = null;
        seeders = null;
        leechers = null;
        downloaded = null;
        speed = null;
        size = null;
        files = null;
        title = null;
        releaseDate = null;
        director = null;
        cast = null;
        country = null;
        duration = null;
        labels = null;
        imdb = null;
        coverUrl = null;
        coverImg = null;
        sampleImg1Url = null;
        sampleImg2Url = null;
        sampleImg3Url = null;
        sampleLargeImg1Url = null;
        sampleLargeImg2Url = null;
        sampleLargeImg3Url = null;
        sampleImg1 = null;
        sampleImg2 = null;
        sampleImg3 = null;
        otherVersionsId = null;
        otherVersionsFid = null;
        otherVersions = null;
        moviePropertyIcons = null;
    }

    public boolean hasMovieDetails() {
        return (title != null);
    }

    public boolean hasSampleImages() {
        return (sampleImg1Url != null);
    }

    public boolean hasOtherVersions() {
        return (otherVersionsId != null) && (otherVersionsFid != null);
    }

    public boolean hasMovieProperties() {
        return (getMoviePropertyIcons() != null) && (getMoviePropertyIcons().size() > 0);
    }

    public String getMainTitle() {
        return (title != null) ? title : name;
    }

    public int getCategoryIconResId() {
        return getCategoryIconResId(category);
    }

    public static int getCategoryIconResId(String torrentCategory) {

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

    public List<Integer> getMoviePropertyIcons() {

        // Build on first access
        if (moviePropertyIcons == null) buildMovieProperties();

        return moviePropertyIcons;
    }

    private void buildMovieProperties() {
        moviePropertyIcons = new ArrayList<Integer>();

        if (name.toLowerCase().contains(MOVIE_PROPERTY_1080P))
            moviePropertyIcons.add(R.drawable.ic_fullhd1080p);

        if (name.toLowerCase().contains(MOVIE_PROPERTY_720P))
            moviePropertyIcons.add(R.drawable.ic_hd720p);

    }

    /*
     * Accessors
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUploaded() {
        return uploaded;
    }

    public void setUploaded(String uploaded) {
        this.uploaded = uploaded;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
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

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Bitmap getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(Bitmap coverImg) {
        this.coverImg = coverImg;
    }

    public String getSampleImg1Url() {
        return sampleImg1Url;
    }

    public void setSampleImg1Url(String sampleImg1Url) {
        this.sampleImg1Url = sampleImg1Url;
    }

    public String getSampleImg2Url() {
        return sampleImg2Url;
    }

    public void setSampleImg2Url(String sampleImg2Url) {
        this.sampleImg2Url = sampleImg2Url;
    }

    public String getSampleImg3Url() {
        return sampleImg3Url;
    }

    public void setSampleImg3Url(String sampleImg3Url) {
        this.sampleImg3Url = sampleImg3Url;
    }

    public String getSampleLargeImg1Url() {
        return sampleLargeImg1Url;
    }

    public void setSampleLargeImg1Url(String sampleLargeImg1Url) {
        this.sampleLargeImg1Url = sampleLargeImg1Url;
    }

    public String getSampleLargeImg2Url() {
        return sampleLargeImg2Url;
    }

    public void setSampleLargeImg2Url(String sampleLargeImg2Url) {
        this.sampleLargeImg2Url = sampleLargeImg2Url;
    }

    public String getSampleLargeImg3Url() {
        return sampleLargeImg3Url;
    }

    public void setSampleLargeImg3Url(String sampleLargeImg3Url) {
        this.sampleLargeImg3Url = sampleLargeImg3Url;
    }

    public Bitmap getSampleImg1() {
        return sampleImg1;
    }

    public void setSampleImg1(Bitmap sampleImg1) {
        this.sampleImg1 = sampleImg1;
    }

    public Bitmap getSampleImg2() {
        return sampleImg2;
    }

    public void setSampleImg2(Bitmap sampleImg2) {
        this.sampleImg2 = sampleImg2;
    }

    public Bitmap getSampleImg3() {
        return sampleImg3;
    }

    public void setSampleImg3(Bitmap sampleImg3) {
        this.sampleImg3 = sampleImg3;
    }

    public String getOtherVersionsId() {
        return otherVersionsId;
    }

    public void setOtherVersionsId(String otherVersionsId) {
        this.otherVersionsId = otherVersionsId;
    }

    public String getOtherVersionsFid() {
        return otherVersionsFid;
    }

    public void setOtherVersionsFid(String otherVersionsFid) {
        this.otherVersionsFid = otherVersionsFid;
    }

    public List<TorrentEntry> getOtherVersions() {
        return otherVersions;
    }

    public void setOtherVersions(List<TorrentEntry> otherVersions) {
        this.otherVersions = otherVersions;
    }

}
