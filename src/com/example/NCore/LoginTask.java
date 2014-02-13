package com.example.NCore;

import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class LoginTask extends AsyncTask<Map<String,Object>,Void,String> {

    // Parameters
    public static final String PARAM_IN_EXECUTOR = "PARAM_IN_EXECUTOR";
    public static final String PARAM_IN_LOGIN_NAME = "PARAM_IN_LOGIN_NAME";
    public static final String PARAM_IN_LOGIN_PASSWORD = "PARAM_IN_LOGIN_PASSWORD";
    public static final String PARAM_IN_LOGIN_WITH_CAPTCHA = "PARAM_IN_LOGIN_WITH_CAPTCHA";
    public static final String PARAM_IN_CAPTCHA_CHALLENGE_VALUE = "PARAM_IN_CAPTCHA_CHALLENGE_VALUE";
    public static final String PARAM_IN_CAPTCHA_RESPONSE_VALUE = "PARAM_IN_CAPTCHA_RESPONSE_VALUE";

    // Members
    private LoginTaskListener mExecutor;

    /**
     * Interface to communicate with the task executor activity
     */
    public interface LoginTaskListener {
        public void onLoginTaskResult(String response);
    }

    @Override
    protected String doInBackground(Map<String,Object>... maps) {
        HttpURLConnection loginConnection = null;
        int responseCode = 0;
        Map<String, List<String>> headerFields = null;

        // Get input parameters
        Map<String,Object> inputParams = maps[0];

        // Get the executor activity of the task
        mExecutor = (LoginTaskListener) inputParams.get(PARAM_IN_EXECUTOR);

        // Extract input parameters
        String loginName = (String) inputParams.get(PARAM_IN_LOGIN_NAME);
        String loginPassword = (String) inputParams.get(PARAM_IN_LOGIN_PASSWORD);
        boolean loginWithCaptcha = ((Boolean) inputParams.get(PARAM_IN_LOGIN_WITH_CAPTCHA)).booleanValue();
        String captchaChallengeValue = (String) inputParams.get(PARAM_IN_CAPTCHA_CHALLENGE_VALUE);
        String captchaResponseValue = (String) inputParams.get(PARAM_IN_CAPTCHA_RESPONSE_VALUE);

        try {

            // Collect the POST parameters
            Map<String,String> postParamsMap = NCoreConnectionManager.prepareLoginPostParams(
                                                       loginName,
                                                       loginPassword,
                                                       captchaChallengeValue,
                                                       captchaResponseValue,
                                                       loginWithCaptcha);

            /*
             * POST the login page
             */

            if (!isCancelled()) {

                // Connection
                loginConnection = NCoreConnectionManager.openPostConnection(NCoreConnectionManager.URL_LOGIN, postParamsMap);
                loginConnection.connect();

                // Response
                responseCode = loginConnection.getResponseCode();
                headerFields = loginConnection.getHeaderFields();

            }

            /*
             * Evaluate the login response
             */

            if (!isCancelled()) {
                return NCoreConnectionManager.evaluateLoginResponse(responseCode, headerFields);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (loginConnection != null) loginConnection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        mExecutor.onLoginTaskResult(response);
    }

    @Override
    protected void onCancelled(String response) {
        Log.d("onCancelled", "");
    }

}
