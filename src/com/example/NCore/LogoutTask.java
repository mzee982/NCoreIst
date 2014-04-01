package com.example.NCore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class LogoutTask extends NCoreAsyncTask<LogoutTask.LogoutTaskListener,Void> {

    /**
     * Interface to communicate with the task executor activity
     */
    public interface LogoutTaskListener {
        public void onLogoutTaskResult(Result result);
        public void onLogoutTaskException(Exception e);
    }

    /**
     * Param object of the task
     */
    public static class Param extends AbstractParam<LogoutTaskListener> {
        public final String logoutUrl;

        public Param(LogoutTaskListener executor, String logoutUrl) {
            super(executor);

            this.logoutUrl = logoutUrl;
        }

    }

    /**
     * Result object of the task
     */
    public class Result extends AbstractResult {
        public final Boolean loggedOut;

        public Result(Boolean loggedOut) {
            this.loggedOut = loggedOut;
        }

    }

    @Override
    protected AbstractResult doInBackground(AbstractParam... params) {
        HttpURLConnection logoutConnection = null;
        int responseCode = 0;
        String logoutUrl = null;

        super.doInBackground(params);

        // Get input parameters
        Param param = (Param) params[0];

        // Extract input parameters
        logoutUrl = param.logoutUrl;

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
                    return new Result(Boolean.TRUE);
                }
                else {
                    return new Result(Boolean.FALSE);
                }

            }

        } catch (UnsupportedEncodingException e) {
            mException = e;
            e.printStackTrace();
        } catch (MalformedURLException e) {
            mException = e;
            e.printStackTrace();
        } catch (IOException e) {
            mException = e;
            e.printStackTrace();
        } finally {
            if (logoutConnection != null) logoutConnection.disconnect();
        }

        return null;

    }

    @Override
    protected void onTaskResult(AbstractResult result) {
        mExecutor.onLogoutTaskResult((Result) result);
    }

    @Override
    protected void onTaskException(Exception e) {
        mExecutor.onLogoutTaskException(e);
    }

}
