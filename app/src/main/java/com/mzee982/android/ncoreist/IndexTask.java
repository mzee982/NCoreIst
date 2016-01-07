package com.mzee982.android.ncoreist;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Map;

public class IndexTask extends NCoreAsyncTask<IndexTask.IndexTaskListener,Void> {

    // Exceptions
    private static final String EXCEPTION_GET_INDEX_PAGE = "Cannot get the index page. HTTP response code: %";

    /**
     * Interface to communicate with the task executor activity
     */
    public interface IndexTaskListener {
        public void onIndexTaskResult(Result result);
        public void onIndexTaskException(Exception e);

    }

    /**
     * Param object of the task
     */
    public static class Param extends AbstractParam<IndexTaskListener> {

        public Param(IndexTaskListener executor) {
            super(executor);
        }

    }

    /**
     * Result object of the task
     */
    public class Result extends AbstractResult {
        public final String logoutUrl;

        public Result(String logoutUrl) {
            this.logoutUrl = logoutUrl;
        }

    }

    @Override
    protected AbstractResult doInBackground(AbstractParam... params) {
        HttpURLConnection indexConnection = null;
        int responseCode = 0;
        InputStream indexInputStream = null;
        String logoutUrl = null;

        super.doInBackground(params);

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
                    return new Result(logoutUrl);
                }

                else {
                    return null;
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
                if (indexInputStream != null) indexInputStream.close();
                if (indexConnection != null) indexConnection.disconnect();
            }
        } catch (IOException e) {
            mException = e;
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onTaskResult(AbstractResult result) {
        mExecutor.onIndexTaskResult((Result) result);
    }

    @Override
    protected void onTaskException(Exception e) {
        mExecutor.onIndexTaskException(e);
    }

}
