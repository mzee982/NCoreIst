package com.example.NCore;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Map;

/**
 *
 */
public class StartActivity extends Activity implements StartTask.StartTaskListener {

    // Members
    private NCoreSession mNCoreSession;
    private StartTask mStartTask;
    private ValueAnimator mAnimation;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance();

        // Setup layout
        setContentView(R.layout.start);

        // Execute the StartTask
        mStartTask = (StartTask) new StartTask().execute(new StartTask.StartTaskListener[]{this});

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check runtime version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            // Animate
            mAnimation = ValueAnimator.ofInt(0, 30);
            mAnimation.setDuration(1000);
            mAnimation.setRepeatCount(ValueAnimator.INFINITE);
            mAnimation.setRepeatMode(ValueAnimator.REVERSE);
            mAnimation.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int animatedValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                            String dot1 = ".";
                            String dot2 = ".";
                            String dot3 = ".";
                            String ten = "          ";
                            char[] tenChars = ten.toCharArray();

                            if ((0 <= animatedValue) && (animatedValue < 10)) {
                                tenChars[animatedValue] = '.';
                                dot3 = new String(tenChars);
                            }
                            else if ((10 <= animatedValue) && (animatedValue < 20)) {
                                tenChars[animatedValue - 10] = '.';
                                dot2 = new String(tenChars);
                            }
                            else if ((20 <= animatedValue) && (animatedValue < 30)) {
                                tenChars[animatedValue - 20] = '.';
                                dot1 = new String(tenChars);
                            }

                            ((TextView) findViewById(R.id.ProgressTextView)).setText(dot1 + dot2 + dot3);
                        }
                    }
            );

            // TODO: Disabled animation
            //mAnimation.start();

        }

    }

    @Override
    protected void onPause() {

        // Check runtime version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mAnimation.isStarted()) {
                mAnimation.cancel();
            }
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        // Cancel the running StartTask
        if ((mStartTask != null) && (mStartTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mStartTask.cancel(true);
        }

        // Release
        mStartTask = null;

        super.onDestroy();
    }

    @Override
    public void onStartTaskResult(Map<String,Object> result) {

        // Jump to the login page
        jumpToLoginActivity(result);

    }

    private void jumpToLoginActivity(Map<String,Object> extras) {

        // Initialize the intent
        Intent intent = new Intent(this, LoginActivity.class);

        // Put the extras
        if (extras != null) {
            intent.putExtra(
                    LoginActivity.EXTRA_LOGIN_WITH_CAPTHCA,
                    ((Boolean) extras.get(StartTask.PARAM_OUT_LOGIN_WITH_CAPTCHA)).booleanValue());
            intent.putExtra(
                    LoginActivity.EXTRA_CAPTCHA_CHALLENGE_VALUE,
                    (String) extras.get(StartTask.PARAM_OUT_CAPTCHA_CHALLENGE_VALUE));
            intent.putExtra(
                    LoginActivity.EXTRA_CAPTCHA_CHALLENGE_BITMAP,
                    (Bitmap) extras.get(StartTask.PARAM_OUT_CAPTCHA_CHALLENGE_BITMAP));
        }

        // Jump back to the login page
        setResult(RESULT_OK, intent);
        finish();

    }

}
