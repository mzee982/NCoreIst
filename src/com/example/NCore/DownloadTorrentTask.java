package com.example.NCore;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class DownloadTorrentTask extends NCoreAsyncTask<DownloadTorrentTask.DownloadTorrentTaskListener,Void> {

    // Exceptions
    private static final String EXCEPTION_GET_TORRENT = "Cannot get the torrent. HTTP response code: %";

    // File names
    public static final String TORRENT_FILE_NAME = "nCOREist.torrent";

    /**
     * Interface to communicate with the task executor activity
     */
    public interface DownloadTorrentTaskListener {
        public void onDownloadTorrentTaskResult(Result result);
        public void onDownloadTorrentTaskException(Exception e);
    }

    /**
     * Param object of the task
     */
    public static class Param extends AbstractParam<DownloadTorrentTaskListener> {
        public final long torrentId;

        public Param(DownloadTorrentTaskListener executor, long torrentId) {
            super(executor);

            this.torrentId = torrentId;
        }

    }

    /**
     * Result object of the task
     */
    public class Result extends AbstractResult {
        public File file;

        public Result(File file) {
            this.file = file;
        }

    }

    @Override
    protected AbstractResult doInBackground(AbstractParam... params) {
        HttpURLConnection downloadConnection = null;
        InputStream downloadInputStream = null;
        FileOutputStream torrentFileOutputStream = null;
        int responseCode = 0;
        Map<String, List<String>> headerFields = null;
        File torrentFile = null;

        super.doInBackground(params);

        // Get input parameters
        Param param = (Param) params[0];

        // Context
        //Context context = (Activity) mExecutor;

        // Extract input parameters
        long torrentId = param.torrentId;

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
                    String torrentFileName = TORRENT_FILE_NAME;
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
                    return new Result(torrentFile);
                }

                else {
                    return null;
                }

            } catch (MalformedURLException e) {
                mException = e;
                e.printStackTrace();
            } catch (IOException e) {
                mException = e;
                e.printStackTrace();
            } finally {
                if (torrentFileOutputStream != null) torrentFileOutputStream.close();
                if (downloadInputStream != null) downloadInputStream.close();
                if (downloadConnection != null) downloadConnection.disconnect();
            }
        } catch (IOException e) {
            mException = e;
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onTaskResult(AbstractResult result) {
        mExecutor.onDownloadTorrentTaskResult((Result) result);
    }

    @Override
    protected void onTaskException(Exception e) {
        mExecutor.onDownloadTorrentTaskException(e);
    }

}
