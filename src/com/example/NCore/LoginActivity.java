package com.example.NCore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity
        extends FragmentActivity
        implements AlertDialogFragment.AlertDialogListener, LoginTask.LoginTaskListener {

    // Intent extras
    public static final String EXTRA_LOGIN_WITH_CAPTHCA = "EXTRA_LOGIN_WITH_CAPTHCA";
    public static final String EXTRA_CAPTCHA_CHALLENGE_VALUE = "EXTRA_CAPTCHA_CHALLENGE_VALUE";
    public static final String EXTRA_CAPTCHA_CHALLENGE_BITMAP = "EXTRA_CAPTCHA_CHALLENGE_BITMAP";

    // Request codes
    private static final int REQUEST_CODE_START = 1;

    // Validation
    private static final String VALIDATION_FAILED_LOGIN_NAME = "Mandatory login name";
    private static final String VALIDATION_FAILED_LOGIN_PASSWORD = "Mandatory login password";
    private static final String VALIDATION_FAILED_CAPTCHA_RESPONSE = "Mandatory CAPTCHA response";

    // Dialog fragment tags
    private static final String TAG_VALIDATION_ALERT_DIALOG_FRAGMENT = "TAG_VALIDATION_ALERT_DIALOG_FRAGMENT";
    private static final String TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT = "TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT";

    // Members
    private NCoreSession mNCoreSession;
    private LoginTask mLoginTask;
    private String mLoginName;
    private String mLoginPassword;
    private boolean bLoginWithCaptcha;
    private String captchaChallengeValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance(this);

        // Get the login page
        if (!getIntent().hasExtra(EXTRA_LOGIN_WITH_CAPTHCA)) {
            jumpToStartActivity();
        }

        // Show the login page
        else {

            // Get intent extra content
            bLoginWithCaptcha = getIntent().getBooleanExtra(EXTRA_LOGIN_WITH_CAPTHCA, false);
            captchaChallengeValue = getIntent().getStringExtra(EXTRA_CAPTCHA_CHALLENGE_VALUE);
            Bitmap captchaChallengeBitmap = (Bitmap) getIntent().getParcelableExtra(EXTRA_CAPTCHA_CHALLENGE_BITMAP);

            // Captcha login layout
            if (bLoginWithCaptcha) {
                setContentView(R.layout.login_captcha);

                ImageView captchaImageView = (ImageView) findViewById(R.id.CaptchaImageView);

                // Set the captcha bitmap
                if (captchaImageView != null) {
                    captchaImageView.setImageBitmap(captchaChallengeBitmap);
                }
            }

            // Simple login layout
            else {
                setContentView(R.layout.login);
            }

            // Set default field values
            if (mNCoreSession.isRememberCredentials()) {
                EditText loginNameEditText = (EditText) findViewById(R.id.LoginNameEditText);
                EditText loginPasswordEditText = (EditText) findViewById(R.id.LoginPasswordEditText);
                CheckBox rememberCredentialsCheckBox = (CheckBox) findViewById(R.id.RememberCheckBox);

                loginNameEditText.setText(mNCoreSession.getLoginName());
                loginPasswordEditText.setText(mNCoreSession.getLoginPassword());
                rememberCredentialsCheckBox.setChecked(mNCoreSession.isRememberCredentials());
            }

        }

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

    @Override
    public void onAlertDialogResult(int responseCode, DialogFragment dialogFragment) {

        // Login failure alert
        if (TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {
            jumpToStartActivity();
        }

        // Validation failure alert
        else if (TAG_VALIDATION_ALERT_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {
            // Nothing to do
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_START) {

            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    finish();

                    break;

                // Activity.RESULT_OK, ...
                default:

                    // Restart the login activity
                    restartActivity(data);

            }

        }

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
                HashMap<String,Object> inputParams = new HashMap<String,Object>();

                inputParams.put(LoginTask.PARAM_IN_EXECUTOR, this);
                inputParams.put(LoginTask.PARAM_IN_LOGIN_NAME, mLoginName);
                inputParams.put(LoginTask.PARAM_IN_LOGIN_PASSWORD, mLoginPassword);
                inputParams.put(LoginTask.PARAM_IN_LOGIN_WITH_CAPTCHA, new Boolean(bLoginWithCaptcha));

                if (bLoginWithCaptcha) {
                    inputParams.put(LoginTask.PARAM_IN_CAPTCHA_CHALLENGE_VALUE, captchaChallengeValue);
                    inputParams.put(LoginTask.PARAM_IN_CAPTCHA_RESPONSE_VALUE, captchaResponseValue);
                }

                HashMap<String,Object>[] inputParamsArray = new HashMap[1];
                inputParamsArray[0] = inputParams;

                // Execute the LoginTask
                mLoginTask = (LoginTask) new LoginTask().execute(inputParamsArray);
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
        mNCoreSession.login(
                this, mLoginName, mLoginPassword, ((CheckBox) findViewById(R.id.RememberCheckBox)).isChecked());

        // Go to the index page
        jumpToIndexActivity(null);

    }

    private void loginFailure(String response) {

        // Inform the user
        AlertDialogFragment alertDialog = new AlertDialogFragment(response);
        alertDialog.show(getSupportFragmentManager(), TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT);

    }

    private void jumpToStartActivity() {

        // Jump to the start activity
        Intent intent = new Intent(this, StartActivity.class);
        startActivityForResult(intent, REQUEST_CODE_START);

    }

    private void jumpToIndexActivity(Map<String,Object> extras) {

        // Initialize the intent
        Intent intent = new Intent(this, IndexActivity.class);

        // Put the extras
        if (extras != null) {
            // TODO: Put the extras into the intent
        }

        // Jump to the index page
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private void restartActivity(Intent data) {

        // Restart the login activity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtras(data);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

}
