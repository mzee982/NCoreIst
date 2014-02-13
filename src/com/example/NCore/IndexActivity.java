package com.example.NCore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class IndexActivity
        extends FragmentActivity
        implements
                QuestionDialogFragment.QuestionDialogListener,
                AlertDialogFragment.AlertDialogListener,
                LogoutTask.LogoutTaskListener,
                IndexTask.IndexTaskListener {

    // Request codes
    private static final int REQUEST_CODE_LOGIN = 1;

    // Dialog fragment tags
    private static final String TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT = "TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT";
    private static final String TAG_LOGOUT_ALERT_DIALOG_FRAGMENT = "TAG_LOGOUT_ALERT_DIALOG_FRAGMENT";

    //
    private static final String QUESTION_LOGOUT = "Do you really want to logout?";
    private static final String ALERT_LOGOUT = "Logout failed. Try again.";

    // Members
    private NCoreSession mNCoreSession;
    private LogoutTask mLogoutTask;
    private IndexTask mIndexTask;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance();

        // Check the login status
        if (!mNCoreSession.isLoggedIn()) {

            // Jump to login
            jumpToLoginActivity();

        }

        // Logged in
        else {

            // Setup layout
            setContentView(R.layout.index);

            // TODO: Testing purpose only
            // Execute the IndexTask
            mIndexTask = (IndexTask) new IndexTask().execute(new IndexTask.IndexTaskListener[]{this});

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        TextView outputTextView = (TextView) findViewById(R.id.OutputTextView);
        if (outputTextView != null) outputTextView.setText("Hello, mizu?");
    }

    @Override
    protected void onDestroy() {

        // Cancel the running LogoutTask
        if ((mLogoutTask != null) && (mLogoutTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mLogoutTask.cancel(true);
        }

        // Cancel the running IndexTask
        if ((mIndexTask != null) && (mIndexTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mIndexTask.cancel(true);
        }

        // Release
        mLogoutTask = null;
        mIndexTask = null;

        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_LOGIN) {

            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    finish();

                    break;

                // Activity.RESULT_OK, ...
                default:

                    // Restart the index activity
                    restartActivity(data);

            }

        }

    }

    @Override
    public void onQuestionDialogResult(int responseCode, DialogFragment dialogFragment) {

        // Logout question
        if (TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {

            switch (responseCode) {
                case QuestionDialogFragment.RESPONSE_YES:
                    logout();

                    break;
                case QuestionDialogFragment.RESPONSE_NO:
                    // Nothing to do
                    break;
                // QuestionDialogFragment.RESPONSE_CANCEL
                default:
                    // Nothing to do
            }

        }

    }

    @Override
    public void onAlertDialogResult(int responseCode, DialogFragment dialogFragment) {

        // Logout alert
        if (TAG_LOGOUT_ALERT_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {

            switch (responseCode) {
                case AlertDialogFragment.RESPONSE_CANCEL:
                    //mNCoreSession.logout();
                    finish();

                    break;
                default:
                    // Nothing to do
            }

        }

    }

    @Override
    public void onLogoutTaskResult(Boolean result) {

        // Successful logout
        if ((result != null) && result.booleanValue()) {

            // Close the session
            mNCoreSession.logout();

            finish();
        }

        // Logout failed
        else {

            // Inform the user
            AlertDialogFragment alertDialog = new AlertDialogFragment(ALERT_LOGOUT);
            alertDialog.show(getSupportFragmentManager(), TAG_LOGOUT_ALERT_DIALOG_FRAGMENT);

        }

    }

    @Override
    public void onIndexTaskResult(Map<String, Object> result) {

        if (result != null) {
            String logoutUrl = (String) result.get(IndexTask.PARAM_OUT_LOGOUT_URL);

            // Store the logout URL in the session
            mNCoreSession.setLogoutUrl(logoutUrl);
        }

    }

    @Override
    public void onBackPressed() {

        // Initiate the logout process
        QuestionDialogFragment questionDialog = new QuestionDialogFragment(QUESTION_LOGOUT);
        questionDialog.show(getSupportFragmentManager(), TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT);

        //super.onBackPressed();
    }

    private void jumpToLoginActivity() {

        // Jump to the login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);

    }

    private void logout() {

        // Setup LogoutTask input parameters
        HashMap<String,Object> inputParams = new HashMap<String,Object>();

        inputParams.put(LogoutTask.PARAM_IN_EXECUTOR, this);
        inputParams.put(LogoutTask.PARAM_IN_LOGOUT_URL, mNCoreSession.getLogoutUrl());

        HashMap<String,Object>[] inputParamsArray = new HashMap[1];
        inputParamsArray[0] = inputParams;

        // Execute the Logout task
        mLogoutTask = (LogoutTask) new LogoutTask().execute(inputParamsArray);

    }

    private void restartActivity(Intent data) {

        // Restart the index activity
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtras(data);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

}
