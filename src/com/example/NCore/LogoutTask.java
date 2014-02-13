package com.example.NCore;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Map;

public class LogoutTask extends AsyncTask<Map<String,Object>,Void,Boolean> {

    // Parameters
    public static final String PARAM_IN_EXECUTOR = "PARAM_IN_EXECUTOR";
    public static final String PARAM_IN_LOGOUT_URL = "PARAM_IN_LOGOUT_URL";

    private LogoutTaskListener mExecutor;

    public interface LogoutTaskListener {
        public void onLogoutTaskResult(Boolean result);
    }

    @Override
    protected Boolean doInBackground(Map<String,Object>... maps) {
        HttpURLConnection logoutConnection = null;
        int responseCode = 0;
        String logoutUrl = null;

        // Get input parameters
        Map<String,Object> inputParams = maps[0];

        // Get the executor activity of the task
        mExecutor = (LogoutTaskListener) inputParams.get(PARAM_IN_EXECUTOR);

        // Extract input parameters
        logoutUrl = (String) inputParams.get(PARAM_IN_LOGOUT_URL);

        try {

            /*
             * Send logout request
             */

            if (!isCancelled()) {

                // Connection
                logoutConnection = NCoreConnectionManager.openGetConnection(logoutUrl);
                logoutConnection.connect();

                // Response
                responseCode = logoutConnection.getResponseCode();

                // Evaluate the logout response
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    return Boolean.TRUE;
                }
                else {
                    return Boolean.FALSE;
                }

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (logoutConnection != null) logoutConnection.disconnect();
        }

        return null;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        mExecutor.onLogoutTaskResult(result);
    }

    @Override
    protected void onCancelled(Boolean result) {
        Log.d("onCancelled", "Void");
    }

}
