package com.mzee982.android.ncoreist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

public class OtherVersionsLoader extends AsyncTaskLoader<TorrentDetails>  {

    // Exceptions
    private static final String EXCEPTION_GET_OTHER_VERSIONS_PAGE = "Cannot get the other versions page. HTTP response code: %";

    // Arguments
    public static final String ARGUMENT_IN_OTHER_VERSIONS_ID = "ARGUMENT_IN_OTHER_VERSIONS_ID";
    public static final String ARGUMENT_IN_OTHER_VERSIONS_FID = "ARGUMENT_IN_OTHER_VERSIONS_FID";

    // Members
    private String mOtherVersionsId;
    private String mOtherVersionsFid;
    private TorrentDetails mTorrentDetails;

    /*
     * Constructor
     */

    public OtherVersionsLoader(Context aContext, Bundle aArgs) {
        super(aContext);

        // Input arguments
        if (aArgs != null) {
            mOtherVersionsId = aArgs.getString(ARGUMENT_IN_OTHER_VERSIONS_ID);
            mOtherVersionsFid = aArgs.getString(ARGUMENT_IN_OTHER_VERSIONS_FID);
        }

    }

    @Override
    public TorrentDetails loadInBackground() {
        HttpURLConnection otherVersionsConnection = null;
        InputStream otherVersionsInputStream = null;
        String otherVersionsUrlString = null;
        int responseCode = 0;
        List<TorrentEntry> otherVersionsList = null;

        // TODO: Cancel points
        try {
            try {

                /*
                 * Prepare the URL
                 */

                // URL for torrent details
                otherVersionsUrlString =
                        NCoreConnectionManager.prepareOtherVersionsUrl(mOtherVersionsId, mOtherVersionsFid);

                /*
                 * Get the other versions
                 */

                otherVersionsConnection = NCoreConnectionManager.openAjaxGetConnection(otherVersionsUrlString);
                otherVersionsConnection.connect();

                // Response
                responseCode = otherVersionsConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    otherVersionsInputStream = new BufferedInputStream(otherVersionsConnection.getInputStream());
                }
                else {
                    throw new IOException(EXCEPTION_GET_OTHER_VERSIONS_PAGE.replace("%", String.valueOf(responseCode)));
                }

                /*
                 * Parse the other versions
                 */

                if (otherVersionsInputStream != null) {

                    // Parse the HTML torrent details page
                    otherVersionsList = NCoreParser.parseOtherVersions(otherVersionsInputStream);

                }

                /*
                 * Results
                 */

                TorrentDetails retTorrentDetails = new TorrentDetails();
                retTorrentDetails.setOtherVersions(otherVersionsList);

                return retTorrentDetails;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (otherVersionsInputStream != null) otherVersionsInputStream.close();
                if (otherVersionsConnection != null) otherVersionsConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TorrentDetails retTorrentDetails = new TorrentDetails();
        retTorrentDetails.setOtherVersions(otherVersionsList);

        return retTorrentDetails;
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
            super.deliverResult(mTorrentDetails);
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
