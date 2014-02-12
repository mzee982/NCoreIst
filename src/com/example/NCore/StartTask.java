package com.example.NCore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class StartTask extends AsyncTask<StartTask.StartTaskListener,Void,Map<String,Object>> {

    // Exceptions
    private static final String EXCEPTION_GET_LOGIN_PAGE = "Cannot get the login page. HTTP response code: %";
    private static final String EXCEPTION_GET_CAPTCHA_IMAGE = "Cannot get the reCAPTCA image. HTTP response code: %";

    // Parameters
    public static final String PARAM_OUT_CAPTCHA_CHALLENGE_VALUE = "PARAM_OUT_CAPTCHA_CHALLENGE_VALUE";
    public static final String PARAM_OUT_CAPTCHA_CHALLENGE_BITMAP = "PARAM_OUT_CAPTCHA_CHALLENGE_BITMAP";

    // Members
    private StartTaskListener mExecutor;

    /**
     * Interface to communicate with the task executor activity
     */
    public interface StartTaskListener {
        public void onStartTaskResult(Map<String,Object> result);
    }

    @Override
    protected Map<String,Object> doInBackground(StartTaskListener... listeners) {
        HttpURLConnection loginConnection = null;
        HttpURLConnection imgConnection = null;
        InputStream loginInputStream = null;
        InputStream imgInputStream = null;
        int responseCode = 0;
        String captchaImgUrl = null;
        String captchaChallengeValue = null;
        Bitmap captchaChallengeBitmap = null;

        // Get the executor activity of the task
        mExecutor = listeners[0];

        //
        try {
            try {

                /*
                 * Get the login page
                 */

                if (!isCancelled()) {

                    // Connection
                    loginConnection = NCoreConnectionManager.openGetConnection(NCoreConnectionManager.URL_LOGIN);
                    loginConnection.connect();

                    // Response
                    responseCode = loginConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        loginInputStream = new BufferedInputStream(loginConnection.getInputStream());
                    }
                    else {
                        throw new IOException(EXCEPTION_GET_LOGIN_PAGE.replace("%", String.valueOf(responseCode)));
                    }
                }

                /*
                 * Looking for reCAPTCHA data
                 */

                if (!isCancelled() && (loginInputStream != null)) {

                    // Parse the HTML login page
                    Map<String,String> parseResult = NCoreParser.parseLoginForCaptcha(loginInputStream);

                    // Parse results
                    if (parseResult != null) {
                        captchaChallengeValue = parseResult.get(NCoreParser.PARAM_CAPTCHA_CHALLENGE_VALUE);
                        captchaImgUrl = parseResult.get(NCoreParser.PARAM_CAPTCHA_IMAGE_URL);
                    }

                }

                /*
                 * Download the reCAPTCHA challenge image, if available
                 */

                if (!isCancelled() && (captchaImgUrl != null)) {

                    // Get the image
                    imgConnection = NCoreConnectionManager.openGetConnection(captchaImgUrl);
                    imgConnection.connect();

                    // Response
                    responseCode = imgConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        imgInputStream = new BufferedInputStream(imgConnection.getInputStream());
                    }
                    else {
                        throw new IOException(EXCEPTION_GET_CAPTCHA_IMAGE.replace("%", String.valueOf(responseCode)));
                    }

                    // Decode the bitmap from the HTML input stream
                    captchaChallengeBitmap = BitmapFactory.decodeStream(imgInputStream);

                }

                /*
                 * Results
                 */

                if (!isCancelled() && (captchaChallengeValue != null) && (captchaChallengeBitmap != null)) {
                    HashMap<String,Object> result = new HashMap<String,Object>();
                    result.put(PARAM_OUT_CAPTCHA_CHALLENGE_VALUE, captchaChallengeValue);
                    result.put(PARAM_OUT_CAPTCHA_CHALLENGE_BITMAP, captchaChallengeBitmap);

                    return result;
                }

                else {
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (loginInputStream != null) loginInputStream.close();
                if (imgInputStream != null) imgInputStream.close();
                if (loginConnection != null) loginConnection.disconnect();
                if (imgConnection != null) imgConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Map<String,Object> result) {
        mExecutor.onStartTaskResult(result);
    }

    @Override
    protected void onCancelled(Map<String,Object> result) {
        Log.d("onCancelled", "");
    }

}
