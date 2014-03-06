package com.example.NCore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class LoginActivity extends FragmentActivity implements AlertDialogFragment.AlertDialogListener,
        LoginTask.LoginTaskListener {

    // Intent extras
    private static final String EXTRA_LOGIN_WITH_CAPTCHA = "EXTRA_LOGIN_WITH_CAPTCHA";
    private static final String EXTRA_CAPTCHA_CHALLENGE_VALUE = "EXTRA_CAPTCHA_CHALLENGE_VALUE";
    private static final String EXTRA_CAPTCHA_CHALLENGE_BITMAP = "EXTRA_CAPTCHA_CHALLENGE_BITMAP";

    // Validation
    private static final String VALIDATION_FAILED_LOGIN_NAME = "Felhasználónév kötelező!";
    private static final String VALIDATION_FAILED_LOGIN_PASSWORD = "Jelszó kötelező!";
    private static final String VALIDATION_FAILED_CAPTCHA_RESPONSE = "reCAPTCHA kötelező!";

    // Dialog fragment tags
    private static final String TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT = "TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT";

    // Members
    private NCoreSession mNCoreSession;
    private LoginTask mLoginTask;
    private String mLoginName;
    private String mLoginPassword;
    private boolean bLoginWithCaptcha;
    private String mCaptchaChallengeValue;
    private Bitmap mCaptchaChallengeBitmap;

    public static void start(Context context, Boolean loginWithCaptcha, String captchaChallengeValue,
                        Bitmap captchaChallengeBitmap) {
        Intent intent = new Intent(context, LoginActivity.class);

        intent.putExtra(EXTRA_LOGIN_WITH_CAPTCHA, loginWithCaptcha);
        intent.putExtra(EXTRA_CAPTCHA_CHALLENGE_VALUE, captchaChallengeValue);
        intent.putExtra(EXTRA_CAPTCHA_CHALLENGE_BITMAP, captchaChallengeBitmap);

        context.startActivity(intent);
    }

    private void getIntentExtras(Intent intent) {

        // Get the intent extra content
        bLoginWithCaptcha = intent.getBooleanExtra(EXTRA_LOGIN_WITH_CAPTCHA, false);
        mCaptchaChallengeValue = intent.getStringExtra(EXTRA_CAPTCHA_CHALLENGE_VALUE);
        mCaptchaChallengeBitmap = intent.getParcelableExtra(EXTRA_CAPTCHA_CHALLENGE_BITMAP);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance(this);

        // Get intent extra content
        getIntentExtras(getIntent());

        // Login layout with CAPTCHA
        if (bLoginWithCaptcha) {
            setContentView(R.layout.login_captcha_activity);

            // Set the captcha bitmap
            ImageView captchaImageView = (ImageView) findViewById(R.id.captcha_image);
            if (captchaImageView != null) captchaImageView.setImageBitmap(mCaptchaChallengeBitmap);

        }

        // Simple login layout
        else {
            setContentView(R.layout.login_activity);
        }

        // Set default field values
        if (mNCoreSession.isRememberCredentials()) {
            EditText loginNameEditText = (EditText) findViewById(R.id.login_name_value);
            EditText loginPasswordEditText = (EditText) findViewById(R.id.login_password_value);
            CheckBox rememberCredentialsCheckBox = (CheckBox) findViewById(R.id.remember_login_checkbox);

            loginNameEditText.setText(mNCoreSession.getLoginName());
            loginPasswordEditText.setText(mNCoreSession.getLoginPassword());
            rememberCredentialsCheckBox.setChecked(mNCoreSession.isRememberCredentials());
        }

    }

    @Override
    protected void onDestroy() {

        // Cancel the LoginTask if not finished yet
        if ((mLoginTask != null) && (mLoginTask.getStatus() != LoginTask.Status.FINISHED)) {
            mLoginTask.cancel(true);
        }

        super.onDestroy();
    }

    public void onShowPassword(View aView) {
        ImageButton showPasswordButton = (ImageButton) aView;
        EditText loginPassword = (EditText) findViewById(R.id.login_password_value);

        // Show
        if (loginPassword.getTransformationMethod() != null) {
            loginPassword.setTransformationMethod(null);
            showPasswordButton.setImageResource(R.drawable.ic_secure);
        }

        // Hide
        else {
            loginPassword.setTransformationMethod(new PasswordTransformationMethod());
            showPasswordButton.setImageResource(R.drawable.ic_not_secure);
        }

    }

    public void onLogin(View aView) {

        // Validate input field values
        String validationResult = validateLoginInputs();

        /*
         * Validation succeeded
         */
        if (validationResult == null) {
            mLoginName = null;
            mLoginPassword = null;
            String captchaResponseValue = null;

            // Get login fields values
            EditText loginNameEditText = (EditText) findViewById(R.id.login_name_value);
            EditText loginPasswordEditText = (EditText) findViewById(R.id.login_password_value);
            mLoginName = loginNameEditText.getText().toString();
            mLoginPassword = loginPasswordEditText.getText().toString();

            if (bLoginWithCaptcha) {
                EditText captchaEditText = (EditText) findViewById(R.id.captcha_value);
                captchaResponseValue = captchaEditText.getText().toString();
            }

            // Setup LoginTask input parameters
            LoginTask.Param loginTaskParam = new LoginTask.Param(this, mLoginName, mLoginPassword,
                    bLoginWithCaptcha, mCaptchaChallengeValue, captchaResponseValue);

            // Execute the LoginTask
            mLoginTask = (LoginTask) new LoginTask().execute(new LoginTask.Param[]{loginTaskParam});
        }

        /*
         * Validation failed
         */
        else {

            // Inform the user
            new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, validationResult).show();

        }

    }

    @Override
    public void onLoginTaskResult(LoginTask.Result result) {

        // Successful login
        if ((result != null) && (result.result == null)) {
            loginSuccess();
        }

        // Login failed
        else {
            loginFailure(result.result);
        }

    }

    @Override
    public void onLoginTaskException(Exception e) {

        // Alert toast
        new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, e.getMessage()).show();

    }

    @Override
    public void onAlertDialogResult(int responseCode, DialogFragment dialogFragment) {

        // Login failure alert
        if (TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {
            navigateToStartActivity();
        }

    }

    private String validateLoginInputs() {

        // Mandatory login name
        String loginName = ((EditText) findViewById(R.id.login_name_value)).getText().toString();

        if (loginName.trim().length() == 0) {
            return new String(VALIDATION_FAILED_LOGIN_NAME);
        }

        // Mandatory password
        String loginPassword = ((EditText) findViewById(R.id.login_password_value)).getText().toString();

        if (loginPassword.trim().length() == 0) {
            return new String(VALIDATION_FAILED_LOGIN_PASSWORD);
        }

        // Mandatory captcha response
        if (bLoginWithCaptcha) {
            String captchaResponseValue = ((EditText) findViewById(R.id.captcha_value)).getText().toString();

            if (captchaResponseValue.trim().length() == 0) {
                return new String(VALIDATION_FAILED_CAPTCHA_RESPONSE);
            }
        }

        return null;
    }

    private void loginSuccess() {

        // Start the login session
        mNCoreSession.login(
                this, mLoginName, mLoginPassword, ((CheckBox) findViewById(R.id.remember_login_checkbox)).isChecked());

        // Go to the categories page
        navigateToCategoriesActivity();

    }

    private void loginFailure(String response) {

        // Inform the user
        AlertDialogFragment alertDialog = new AlertDialogFragment(response);
        alertDialog.show(getSupportFragmentManager(), TAG_LOGIN_FAILURE_ALERT_DIALOG_FRAGMENT);

    }

    private void navigateToStartActivity() {

        // Navigate to the StartActivity
        StartActivity.start(this);

        // This activity should be finished
        finish();

    }

    private void navigateToCategoriesActivity() {

        // Navigate to the CategoriesActivity
        CategoriesActivity.start(this);

        // This activity should be finished
        finish();

    }

}
