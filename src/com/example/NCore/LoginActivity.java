package com.example.NCore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.HashMap;

public class LoginActivity
        extends FragmentActivity
        implements AlertDialogFragment.AlertDialogListener, LoginTask.LoginTaskListener {

    // Intent extras
    public static final String EXTRA_CAPTCHA_CHALLENGE_VALUE = "EXTRA_CAPTCHA_CHALLENGE_VALUE";
    public static final String EXTRA_CAPTCHA_CHALLENGE_BITMAP = "EXTRA_CAPTCHA_CHALLENGE_BITMAP";

    // Validation
    private static final String VALIDATION_FAILED_LOGIN_NAME = "Mandatory login name";
    private static final String VALIDATION_FAILED_LOGIN_PASSWORD = "Mandatory login password";
    private static final String VALIDATION_FAILED_CAPTCHA_RESPONSE = "Mandatory CAPTCHA response";

    //
    private static final String TAG_VALIDATION_ALERT_DIALOG_FRAGMENT = "TAG_VALIDATION_ALERT_DIALOG_FRAGMENT";
    private static final String TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT = "TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT";

    // Members
    private NCoreSession mNCoreSession;
    private LoginTask mLoginTask;
    private String mLoginName;
    private String mLoginPassword;
    private String captchaChallengeValue;
    private boolean bLoginWithCaptcha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent extra content
        captchaChallengeValue = getIntent().getStringExtra(EXTRA_CAPTCHA_CHALLENGE_VALUE);
        Bitmap captchaChallengeBitmap = (Bitmap) getIntent().getParcelableExtra(EXTRA_CAPTCHA_CHALLENGE_BITMAP);

        // Captcha login layout
        if (captchaChallengeBitmap != null) {
            bLoginWithCaptcha = true;
            setContentView(R.layout.login_captcha);

            ImageView captchaImageView = (ImageView) findViewById(R.id.CaptchaImageView);

            // Set the captcha bitmap
            if (captchaImageView != null) {
                captchaImageView.setImageBitmap(captchaChallengeBitmap);
            }
        }

        // Simple login layout
        else {
            bLoginWithCaptcha = false;
            setContentView(R.layout.login);
        }

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance();
    }

    @Override
    protected void onDestroy() {
        // Cancel the running StartTask
        if ((mLoginTask != null) && (mLoginTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mLoginTask.cancel(true);
        }

        // Release
        mLoginTask = null;

        super.onDestroy();
    }

    private String validateLoginInputs() {

        // Mandatory login name
        String loginName = ((EditText) findViewById(R.id.LoginNameEditText)).getText().toString();

        if (loginName.trim().length() == 0) {
            return new String(VALIDATION_FAILED_LOGIN_NAME);
        }

        // Mandatory password
        String loginPassword = ((EditText) findViewById(R.id.LoginPasswordEditText)).getText().toString();

        if (loginPassword.trim().length() == 0) {
            return new String(VALIDATION_FAILED_LOGIN_PASSWORD);
        }

        // Mandatory captcha response
        if (bLoginWithCaptcha) {
            String captchaResponseValue = ((EditText) findViewById(R.id.CaptchaEditText)).getText().toString();

            if (captchaResponseValue.trim().length() == 0) {
                return new String(VALIDATION_FAILED_CAPTCHA_RESPONSE);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public void login(View aView) {

        // Validate input field values
        String validationResult = validateLoginInputs();

        // Validation succeeded
        if (validationResult == null) {

            // Get network information
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

            // Check the connection
            if ((networkInfo != null) && (networkInfo.isConnected())) {
                mLoginName = null;
                mLoginPassword = null;
                String captchaResponseValue = null;

                // Get login fields values
                EditText loginNameEditText = (EditText) findViewById(R.id.LoginNameEditText);
                EditText loginPasswordEditText = (EditText) findViewById(R.id.LoginPasswordEditText);
                mLoginName = loginNameEditText.getText().toString();
                mLoginPassword = loginPasswordEditText.getText().toString();

                if (bLoginWithCaptcha) {
                    EditText captchaEditText = (EditText) findViewById(R.id.CaptchaEditText);
                    captchaResponseValue = captchaEditText.toString();
                }

                // Setup LoginTask input parameters
                HashMap<String,Object> extraParams = new HashMap<String,Object>();

                extraParams.put(LoginTask.PARAM_IN_EXECUTOR, this);
                extraParams.put(LoginTask.PARAM_IN_LOGIN_NAME, mLoginName);
                extraParams.put(LoginTask.PARAM_IN_LOGIN_PASSWORD, mLoginPassword);
                extraParams.put(LoginTask.PARAM_IN_LOGIN_WITH_CAPTCHA, new Boolean(bLoginWithCaptcha));

                if (bLoginWithCaptcha) {
                    extraParams.put(LoginTask.PARAM_IN_CAPTCHA_CHALLENGE_VALUE, captchaChallengeValue);
                    extraParams.put(LoginTask.PARAM_IN_CAPTCHA_RESPONSE_VALUE, captchaResponseValue);
                }

                HashMap<String,Object>[] extraParamsArray = new HashMap[1];
                extraParamsArray[0] = extraParams;

                // Execute the LoginTask
                mLoginTask = (LoginTask) new LoginTask().execute(extraParamsArray);
            }

            // No network connection is available
            else {
                // TODO: Error handling
            }

        }

        // Validation failed
        else {

            // Inform the user
            AlertDialogFragment alertDialog = new AlertDialogFragment(validationResult);
            alertDialog.show(getSupportFragmentManager(), TAG_VALIDATION_ALERT_DIALOG_FRAGMENT);

        }

    }

    private void loginSuccess() {

        // Start the login session
        mNCoreSession.login(mLoginName, mLoginPassword);

        // Go to the index page
        jumpToIndexActivity();

    }

    private void loginFailure(String response) {

        // Inform the user
        AlertDialogFragment alertDialog = new AlertDialogFragment(response);
        alertDialog.show(getSupportFragmentManager(), TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT);

    }

    private void jumpToStartActivity() {

        // Go back to start page
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private void jumpToIndexActivity() {

        // Go to the index page
        Intent intent = new Intent(this, IndexActivity.class);
        startActivity(intent);

    }

    @Override
    public void onAlertDialogPositiveClick(DialogFragment dialogFragment) {

        // Login failure alert
        if (TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {
            jumpToStartActivity();
        }

    }

    @Override
    public void onLoginTaskResult(String response) {

        // Successful login
        if (response == null) {
            loginSuccess();
        }

        // Login failed
        else {
            loginFailure(response);
        }

    }

}
