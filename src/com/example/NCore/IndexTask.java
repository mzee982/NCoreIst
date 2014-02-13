package com.example.NCore;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class IndexTask extends AsyncTask<IndexTask.IndexTaskListener,Void,Map<String,Object>> {

    // Exceptions
    private static final String EXCEPTION_GET_INDEX_PAGE = "Cannot get the index page. HTTP response code: %";

    // Parameters
    public static final String PARAM_OUT_LOGOUT_URL = "PARAM_OUT_LOGOUT_URL";

    // Members
    private IndexTaskListener mExecutor;

    /**
     * Interface to communicate with the task executor activity
     */
    public interface IndexTaskListener {
        public void onIndexTaskResult(Map<String,Object> result);
    }

    @Override
    protected Map<String,Object> doInBackground(IndexTaskListener... listeners) {
        HttpURLConnection indexConnection = null;
        int responseCode = 0;
        InputStream indexInputStream = null;
        String logoutUrl = null;

        // Get the executor activity of the task
        mExecutor = listeners[0];

        try {
            try {

                /*
                 * Get the index page
                 */

                if (!isCancelled()) {

                    // Connection
                    indexConnection = NCoreConnectionManager.openGetConnection(NCoreConnectionManager.URL_INDEX);
                    indexConnection.connect();

                    // Response
                    responseCode = indexConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        indexInputStream = new BufferedInputStream(indexConnection.getInputStream());
                    }
                    else {
                        throw new IOException(EXCEPTION_GET_INDEX_PAGE.replace("%", String.valueOf(responseCode)));
                    }

                }

                /*
                 * Parse
                 */

                if (!isCancelled() && (indexInputStream != null)) {

                    // Parse the HTML index page
                    Map<String,String> parseResult = NCoreParser.parseIndex(indexInputStream);

                    // Parse results
                    if (parseResult != null) {
                        logoutUrl = parseResult.get(NCoreParser.PARAM_LOGOUT_URL);
                    }

                }

                /*
                 * Result
                 */

                if (!isCancelled() && (logoutUrl != null)) {

                    HashMap<String,Object> result = new HashMap<String,Object>();
                    result.put(PARAM_OUT_LOGOUT_URL, logoutUrl);

                    return result;

                }

                else {
                    return null;
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (indexInputStream != null) indexInputStream.close();
                if (indexConnection != null) indexConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Map<String,Object> result) {
        mExecutor.onIndexTaskResult(result);
    }

    @Override
    protected void onCancelled(Map<String,Object> result) {
        Log.d("onCancelled", "Void");
    }

}
