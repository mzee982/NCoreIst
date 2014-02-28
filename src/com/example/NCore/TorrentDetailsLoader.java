package com.example.NCore;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class TorrentDetailsLoader extends AsyncTaskLoader<TorrentDetails> {

    // Exceptions
    private static final String EXCEPTION_GET_TORRENT_DETAILS_PAGE = "Cannot get the torrent details page. HTTP response code: %";

    // Arguments
    public static final String ARGUMENT_IN_TORRENT_ID = "ARGUMENT_IN_TORRENT_ID";

    // Members
    private long mTorrentId;
    private TorrentDetails mTorrentDetails;

    /*
     * Constructor
     */

    public TorrentDetailsLoader(Context aContext, Bundle aArgs) {
        super(aContext);

        // Input arguments
        if (aArgs != null) {
            mTorrentId = aArgs.getLong(ARGUMENT_IN_TORRENT_ID);
        }

    }

    @Override
    public TorrentDetails loadInBackground() {
        HttpURLConnection torrentDetailsConnection = null;
        InputStream torrentDetailsInputStream = null;
        String torrentDetailsUrlString = null;
        int responseCode = 0;
        TorrentDetails torrentDetails = null;

        // TODO: Cancel points
        try {
            try {

                /*
                 * Prepare the URL
                 */

                // URL for torrent details
                torrentDetailsUrlString = NCoreConnectionManager.prepareTorrentDetailsUrl(mTorrentId);

                /*
                 * Get the torrent details
                 */

                torrentDetailsConnection = NCoreConnectionManager.openGetConnection(torrentDetailsUrlString);
                torrentDetailsConnection.connect();

                // Response
                responseCode = torrentDetailsConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    torrentDetailsInputStream = new BufferedInputStream(torrentDetailsConnection.getInputStream());
                }
                else {
                    throw new IOException(EXCEPTION_GET_TORRENT_DETAILS_PAGE.replace("%", String.valueOf(responseCode)));
                }

                /*
                 * Parse the torrent list
                 */

                if (torrentDetailsInputStream != null) {

                    // Parse the HTML torrent details page
                    torrentDetails = NCoreParser.parseTorrentDetails(torrentDetailsInputStream);

                }

                /*
                 * Results
                 */

                return torrentDetails;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (torrentDetailsInputStream != null) torrentDetailsInputStream.close();
                if (torrentDetailsConnection != null) torrentDetailsConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return torrentDetails;
    }

    @Override
    public void deliverResult(TorrentDetails aTorrentDetails) {

        if (isReset()) {
            if (aTorrentDetails != null) {
                onReleaseResources(aTorrentDetails);
            }
        }

        TorrentDetails oldTorrentDetails = mTorrentDetails;
        mTorrentDetails = aTorrentDetails;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(aTorrentDetails);
        }

        if (oldTorrentDetails != null) {
            onReleaseResources(oldTorrentDetails);
        }

    }

    @Override
    protected void onStartLoading() {

        if (mTorrentDetails != null) {
            deliverResult(mTorrentDetails);
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
    public void onCanceled(TorrentDetails aTorrentDetails) {
        super.onCanceled(aTorrentDetails);

        onReleaseResources(aTorrentDetails);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mTorrentDetails != null) {
            onReleaseResources(mTorrentDetails);
        }

        // TODO: Stop monitoring for changes
    }

    protected void onReleaseResources(TorrentDetails aTorrentDetails) {

        aTorrentDetails = null;

    }

}
