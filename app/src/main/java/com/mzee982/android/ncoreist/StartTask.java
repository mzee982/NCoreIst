package com.mzee982.android.ncoreist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Map;

public class StartTask extends NCoreAsyncTask<StartTask.StartTaskListener,Void> {

    // Exceptions
    private static final String EXCEPTION_GET_LOGIN_PAGE = "Cannot get the login page. HTTP response code: %";
    private static final String EXCEPTION_GET_CHALLENGE_SCRIPT =
            "Cannot get the reCAPTCHA challenge script. HTTP response code: %";
    private static final String EXCEPTION_GET_CAPTCHA_IMAGE = "Cannot get the reCAPTCHA image. HTTP response code: %";

    /**
     * Interface to communicate with the task executor activity
     */
    public interface StartTaskListener {
        public void onStartTaskResult(Result result);
        public void onStartTaskException(Exception e);
    }

    /**
     * Param object of the task
     */
    public static class Param extends AbstractParam<StartTaskListener> {

        public Param(StartTaskListener executor) {
            super(executor);
        }

    }

    /**
     * Result object of the task
     */
    public class Result extends AbstractResult {
        public final Boolean loginWithCaptcha;
        public final String captchaChallengeValue;
        public final Bitmap captchaChallengeBitmap;

        public Result(Boolean loginWithCaptcha, String captchaChallengeValue, Bitmap captchaChallengeBitmap) {
            this.loginWithCaptcha = loginWithCaptcha;
            this.captchaChallengeValue = captchaChallengeValue;
            this.captchaChallengeBitmap = captchaChallengeBitmap;
        }
    }

    @Override
    protected AbstractResult doInBackground(AbstractParam... params) {
        HttpURLConnection loginConnection = null;
        HttpURLConnection challengeScriptConnection = null;
        HttpURLConnection imgConnection = null;
        InputStream loginInputStream = null;
        InputStream challengeScriptInputStream = null;
        InputStream imgInputStream = null;
        int responseCode = 0;
        String captchaChallengeScriptUrl = null;
        String captchaImgUrl = null;
        String captchaChallengeValue = null;
        Bitmap captchaChallengeBitmap = null;

        super.doInBackground(params);

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
                 * Looking for reCAPTCHA
                 */

                if (!isCancelled() && (loginInputStream != null)) {

                    // Parse the HTML login page
                    Map<String,String> parseResult = NCoreParser.parseLoginForCaptcha(loginInputStream);

                    // Parse results
                    if (parseResult != null) {
                        captchaChallengeScriptUrl = parseResult.get(NCoreParser.PARAM_CAPTCHA_CHALLENGE_SCRIPT_URL);
                    }

                }

                /*
                 * Download the reCAPTCHA challenge script
                 */

                if (!isCancelled() && (captchaChallengeScriptUrl != null)) {

                    // Connection
                    challengeScriptConnection = NCoreConnectionManager.openGetConnection(captchaChallengeScriptUrl);
                    challengeScriptConnection.connect();

                    // Response
                    responseCode = challengeScriptConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        challengeScriptInputStream = new BufferedInputStream(
                                challengeScriptConnection.getInputStream());
                    }
                    else {
                        throw new IOException(
                                EXCEPTION_GET_CHALLENGE_SCRIPT.replace("%", String.valueOf(responseCode)));
                    }

                }

                /*
                 * Looking for reCAPTCHA challenge value and image
                 */

                if (!isCancelled() && (challengeScriptInputStream != null)) {

                    // Parse the reCAPTCHA challenge script
                    Map<String,String> parseResult =
                            NCoreParser.parseReCaptchaChallengeScript(challengeScriptInputStream);

                    // Parse results
                    if (parseResult != null) {
                        captchaChallengeValue = parseResult.get(NCoreParser.PARAM_CAPTCHA_CHALLENGE_VALUE);

                        // Prepare the image URL
                        captchaImgUrl = NCoreConnectionManager.prepareReCaptchaImageUrl(captchaChallengeValue);

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

                    captchaChallengeBitmap = BitmapFactory.decodeStream(imgInputStream);

                }

                /*
                 * Results
                 */

                if (!isCancelled() && (captchaChallengeValue != null) && (captchaChallengeBitmap != null)) {
                    return new Result(Boolean.TRUE, captchaChallengeValue, captchaChallengeBitmap);
                }

                else {
                    return new Result(Boolean.FALSE, null, null);
                }

            } catch (MalformedURLException e) {
                mException = e;
                e.printStackTrace();
            } catch (JSONException e) {
                mException = e;
                e.printStackTrace();
            } catch (IOException e) {
                mException = e;
                e.printStackTrace();
            } finally {
                if (loginInputStream != null) loginInputStream.close();
                if (challengeScriptInputStream != null) challengeScriptInputStream.close();
                if (imgInputStream != null) imgInputStream.close();
                if (loginConnection != null) loginConnection.disconnect();
                if (challengeScriptConnection != null) challengeScriptConnection.disconnect();
                if (imgConnection != null) imgConnection.disconnect();
            }
        } catch (IOException e) {
            mException = e;
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onTaskResult(AbstractResult result) {
        mExecutor.onStartTaskResult((Result) result);
    }

    @Override
    protected void onTaskException(Exception e) {
        mExecutor.onStartTaskException(e);
    }

}
