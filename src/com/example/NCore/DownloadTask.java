package com.example.NCore;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class DownloadTask extends AsyncTask<Map<String,Object>,Void,File> {

    // Exceptions
    private static final String EXCEPTION_GET_TORRENT = "Cannot get the torrent. HTTP response code: %";

    // Parameters
    public static final String PARAM_IN_EXECUTOR = "PARAM_IN_EXECUTOR";
    public static final String PARAM_IN_TORRENT_ID = "PARAM_IN_TORRENT_ID";

    // Members
    private DownloadTaskListener mExecutor;

    /**
     * Interface to communicate with the task executor activity
     */
    public interface DownloadTaskListener {
        public Context getActivity();
        public void onDownloadTaskResult(File aFile);
    }

    @Override
    protected File doInBackground(Map<String,Object>... maps) {
        HttpURLConnection downloadConnection = null;
        InputStream downloadInputStream = null;
        FileOutputStream torrentFileOutputStream = null;
        int responseCode = 0;
        Map<String, List<String>> headerFields = null;
        File torrentFile = null;

        // Get input parameters
        Map<String,Object> inputParams = maps[0];

        // Get the executor activity of the task
        mExecutor = (DownloadTaskListener) inputParams.get(PARAM_IN_EXECUTOR);

        // Context
        Context context = mExecutor.getActivity();

        // Extract input parameters
        long torrentId = (Long) inputParams.get(PARAM_IN_TORRENT_ID);

        //
        try {
            try {

                /*
                 * Get the torrent
                 */

                if (!isCancelled()) {

                    // Prepare the torrent download URL
                    String torrentDownloadUrl = NCoreConnectionManager.prepareTorrentDownloadUrl(torrentId);

                    // Connection
                    downloadConnection = NCoreConnectionManager.openGetConnection(torrentDownloadUrl);
                    downloadConnection.connect();

                    // Response
                    responseCode = downloadConnection.getResponseCode();
                    headerFields = downloadConnection.getHeaderFields();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        downloadInputStream = new BufferedInputStream(downloadConnection.getInputStream());
                    }
                    else {
                        throw new IOException(EXCEPTION_GET_TORRENT.replace("%", String.valueOf(responseCode)));
                    }

                    // Extract the torrent file name from the response header
                    String torrentFileName = "nCOREist.torrent";
                    //String torrentFileName = NCoreConnectionManager.evaluateTorrentDownloadResponse(headerFields);

                    /*
                     * Download the torrent file
                     */

                    if (torrentFileName != null) {

                        // Open the output file
                        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        torrentFile = new File(downloadDir, torrentFileName);
                        torrentFileOutputStream = new FileOutputStream(torrentFile);

                        //torrentFileOutputStream = context.openFileOutput(torrentFileName, Context.MODE_PRIVATE);
                        //torrentFile = context.getFileStreamPath(torrentFileName);

                        // Write the output file
                        byte[] buffer = new byte[8192];

                        int len = downloadInputStream.read(buffer);

                        while (len != -1) {
                            torrentFileOutputStream.write(buffer, 0, len);
                            len = downloadInputStream.read(buffer);
                        }

                        torrentFileOutputStream.flush();
                    }

                    else {
                        throw new IOException(EXCEPTION_GET_TORRENT.replace("%", String.valueOf(responseCode)));
                    }

                }

                /*
                 * Results
                 */

                if (!isCancelled() && (torrentFile != null)) {

                    return torrentFile;

                }

                else {
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (torrentFileOutputStream != null) torrentFileOutputStream.close();
                if (downloadInputStream != null) downloadInputStream.close();
                if (downloadConnection != null) downloadConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(File aFile) {
        mExecutor.onDownloadTaskResult(aFile);
    }

    @Override
    protected void onCancelled(File aFile) {
        Log.d("onCancelled", "");
    }

}
