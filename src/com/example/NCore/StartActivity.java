package com.example.NCore;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class StartActivity extends FragmentActivity implements StartTask.StartTaskListener,
        AlertDialogFragment.AlertDialogListener {

    // Dialog fragment tags
    private static final String TAG_EXCEPTION_ALERT_DIALOG_FRAGMENT = "TAG_EXCEPTION_ALERT_DIALOG_FRAGMENT";
    private static final String TAG_CONNECTION_ALERT_DIALOG_FRAGMENT = "TAG_CONNECTION_ALERT_DIALOG_FRAGMENT";

    // Error messages
    private static final String ERROR_MESSAGE_NO_NETWORK_CONNECTION = "Nincs hálózati kapcsolat!";

    // Members
    private StartTask mStartTask;

    public static void start(Context context) {
        context.startActivity(new Intent(context, StartActivity.class));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup layout
        setContentView(R.layout.start_activity);

        // Setup StartTask input parameters
        StartTask.Param startTaskParam = new StartTask.Param(this);

        // Get network information
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        // Check the connection
        if ((networkInfo == null) || (!networkInfo.isConnected())) {

            // Alert dialog
            AlertDialogFragment alertDialog = new AlertDialogFragment(ERROR_MESSAGE_NO_NETWORK_CONNECTION);
            alertDialog.show(getSupportFragmentManager(), TAG_CONNECTION_ALERT_DIALOG_FRAGMENT);

        }

        else {

            // Execute the StartTask
            mStartTask = (StartTask) new StartTask().execute(new StartTask.Param[]{startTaskParam});

        }

    }

    @Override
    protected void onDestroy() {

        // Cancel the StartTask if not finished yet
        if ((mStartTask != null) && (mStartTask.getStatus() != StartTask.Status.FINISHED))
            mStartTask.cancel(true);

        super.onDestroy();
    }

    @Override
    public void onStartTaskResult(StartTask.Result result) {

        // Navigate to the login activity
        navigateToLoginActivity(result);

    }

    @Override
    public void onStartTaskException(Exception e) {

        // Alert dialog
        AlertDialogFragment alertDialog = new AlertDialogFragment(e.getMessage());
        alertDialog.show(getSupportFragmentManager(), TAG_EXCEPTION_ALERT_DIALOG_FRAGMENT);

    }

    @Override
    public void onAlertDialogResult(int responseCode, DialogFragment dialogFragment) {

        // On StartTask exception
        if (TAG_EXCEPTION_ALERT_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {
            finish();
        }

        // No network connection
        else if (TAG_CONNECTION_ALERT_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {
            finish();
        }

    }

    private void navigateToLoginActivity(StartTask.Result result) {

        if (result != null) {

            // Navigate to the login activity
            LoginActivity.start(this, result.loginWithCaptcha, result.captchaChallengeValue,
                    result.captchaChallengeBitmap);

            // This activity should be finished
            finish();

        }

    }

}
