package com.mzee982.android.ncoreist;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class LoginTask extends NCoreAsyncTask<LoginTask.LoginTaskListener,Void> {

    /**
     * Interface to communicate with the task executor activity
     */
    public interface LoginTaskListener {
        public void onLoginTaskResult(Result result);
        public void onLoginTaskException(Exception e);
    }

    /**
     * Param object of the task
     */
    public static class Param extends AbstractParam<LoginTaskListener> {
        public final String loginName;
        public final String loginPassword;
        public final boolean loginWithCaptcha;
        public final String captchaChallengeValue;
        public final String captchaResponseValue;

        public Param(LoginTaskListener executor, String loginName, String loginPassword, boolean loginWithCaptcha,
                     String captchaChallengeValue, String captchaResponseValue) {
            super(executor);

            this.loginName = loginName;
            this.loginPassword = loginPassword;
            this.loginWithCaptcha = loginWithCaptcha;
            this.captchaChallengeValue = captchaChallengeValue;
            this.captchaResponseValue = captchaResponseValue;
        }

    }

    /**
     * Result object of the task
     */
    public class Result extends AbstractResult {
        public final String result;

        public Result(String result) {
            this.result = result;
        }

    }

    @Override
    protected AbstractResult doInBackground(AbstractParam... params) {
        HttpURLConnection loginConnection = null;
        int responseCode = 0;
        Map<String, List<String>> headerFields = null;

        super.doInBackground(params);

        // Get input parameters
        Param param = (Param) params[0];

        // Extract input parameters
        String loginName = param.loginName;
        String loginPassword = param.loginPassword;
        boolean loginWithCaptcha = param.loginWithCaptcha;
        String captchaChallengeValue = param.captchaChallengeValue;
        String captchaResponseValue = param.captchaResponseValue;

        try {

            // Collect the POST parameters
            Map<String,String> postParamsMap = NCoreConnectionManager.prepareLoginPostParams( loginName, loginPassword,
                    captchaChallengeValue, captchaResponseValue, loginWithCaptcha);

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
                return new Result(NCoreConnectionManager.evaluateLoginResponse(responseCode, headerFields));
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
            if (loginConnection != null) loginConnection.disconnect();
        }

        return null;
    }

    @Override
    protected void onTaskResult(AbstractResult result) {
        mExecutor.onLoginTaskResult((Result) result);
    }

    @Override
    protected void onTaskException(Exception e) {
        mExecutor.onLoginTaskException(e);
    }

}
