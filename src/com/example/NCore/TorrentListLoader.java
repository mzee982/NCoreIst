package com.example.NCore;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class TorrentListLoader extends AsyncTaskLoader<List<TorrentEntry>> {

    // Exceptions
    private static final String EXCEPTION_GET_TORRENT_LIST_PAGE = "Cannot get the torrent list page. HTTP response code: %";

    // Arguments
    public static final String ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX = "ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX";
    public static final String ARGUMENT_IN_TORRENT_LIST_PAGE_INDEX = "ARGUMENT_IN_TORRENT_LIST_PAGE_INDEX";
    public static final String ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY = "ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY";

    // Members
    private int mTorrentListCategoryIndex;
    private int mTorrentListPageIndex;
    private String mTorrentListSearchQuery;
    private List<TorrentEntry> mTorrentList;
    private boolean mHasMoreResults;

    /*
     * Constructor
     */

    public TorrentListLoader(Context aContext, Bundle aArgs) {
        super(aContext);

        // Input arguments
        if (aArgs != null) {
            mTorrentListCategoryIndex = aArgs.getInt(ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX);
            mTorrentListPageIndex = aArgs.getInt(ARGUMENT_IN_TORRENT_LIST_PAGE_INDEX, 1);
            mTorrentListSearchQuery = aArgs.getString(ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY);
        }

    }

    /*
     *
     */

    @Override
    public List<TorrentEntry> loadInBackground() {
        HttpURLConnection torrentListConnection = null;
        InputStream torrentListInputStream = null;
        String torrentListUrlString = null;
        int responseCode = 0;
        List<TorrentEntry> entries = null;

        // TODO: Cancel points
        try {
            try {

                /*
                 * Prepare the URL
                 */

                // URL for search
                if (mTorrentListSearchQuery != null) {
                    torrentListUrlString = NCoreConnectionManager.URL_TORRENT_LIST;
                }

                // URL for category listing
                else {
                    torrentListUrlString = NCoreConnectionManager.prepareTorrentListUrlForCategory(
                            mTorrentListCategoryIndex, mTorrentListPageIndex);
                }

                /*
                 * Get the torrent list
                 */

                // Connection

                // Search
                if (mTorrentListSearchQuery != null) {

                    // Prepare POST parameters
                    Map<String,String> postParams =
                            NCoreConnectionManager.prepareSearchPostParams(
                                    mTorrentListCategoryIndex, mTorrentListSearchQuery, mTorrentListPageIndex);

                    //
                    torrentListConnection =
                            NCoreConnectionManager.openPostConnectionForResult(torrentListUrlString, postParams);
                }

                // Category listing
                else {
                    torrentListConnection = NCoreConnectionManager.openGetConnection(torrentListUrlString);
                }

                torrentListConnection.connect();

                // Response
                responseCode = torrentListConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    torrentListInputStream = new BufferedInputStream(torrentListConnection.getInputStream());
                }
                else {
                    throw new IOException(EXCEPTION_GET_TORRENT_LIST_PAGE.replace("%", String.valueOf(responseCode)));
                }

                /*
                 * Parse the torrent list
                 */

                if (torrentListInputStream != null) {

                    // Parse the HTML torrent list page
                    entries = NCoreParser.parseTorrentList(torrentListInputStream);

                    // Has more results
                    mHasMoreResults = (entries.size() > 0) && (entries.get(entries.size() - 1).isEmpty());
                    if (mHasMoreResults) {
                        entries.remove(entries.size() - 1);
                    }

                }

                /*
                 * Results
                 */

                return entries;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (torrentListInputStream != null) torrentListInputStream.close();
                if (torrentListConnection != null) torrentListConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return entries;
    }

    @Override
    public void deliverResult(List<TorrentEntry> aTorrentList) {

        if (isReset()) {
            if (aTorrentList != null) {
                onReleaseResources(aTorrentList);
            }
        }

        List<TorrentEntry> oldTorrentList = mTorrentList;
        mTorrentList = aTorrentList;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(aTorrentList);
        }

        if (oldTorrentList != null) {
            onReleaseResources(oldTorrentList);
        }

    }

    @Override
    protected void onStartLoading() {

        if (mTorrentList != null) {
            deliverResult(mTorrentList);
        }

        else {
            forceLoad();
        }

    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<TorrentEntry> aTorrentList) {
        super.onCanceled(aTorrentList);

        onReleaseResources(aTorrentList);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mTorrentList != null) {
            onReleaseResources(mTorrentList);
            mTorrentList = null;
        }

        // TODO: Stop monitoring for changes
    }

    protected void onReleaseResources(List<TorrentEntry> aTorrentList) {

        if (aTorrentList != null) {
            aTorrentList.clear();
            aTorrentList = null;
        }

    }

    public boolean hasMoreResults() {
        return mHasMoreResults;
    }

}
