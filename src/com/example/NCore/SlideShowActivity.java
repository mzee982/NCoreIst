package com.example.NCore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SlideShowActivity extends ActionBarActivity implements SlideShowFragment.SlideShowFragmentListener {

    // Intent extras
    private static final String EXTRA_IN_TITLE = "EXTRA_IN_TITLE";
    private static final String EXTRA_IN_BITMAPS = "EXTRA_IN_BITMAPS";
    private static final String EXTRA_IN_BITMAP_URLS = "EXTRA_IN_BITMAP_URLS";
    private static final String EXTRA_IN_MAX_WIDTH = "EXTRA_IN_MAX_WIDTH";
    private static final String EXTRA_IN_MAX_HEIGHT = "EXTRA_IN_MAX_HEIGHT";

    // SlideShow dot indicators
    private static final int SLIDE_SHOW_DOT_INDICATORS_ON_SCREEN_TIME = 2000;
    private static final int SLIDE_SHOW_DOT_INDICATORS_FADE_OUT_TIME = 1000;

    // Members
    private SlideShowPagerAdapter mSlideShowAdapter;
    private ViewPager mSlideShowPager;
    private ViewGroup mSlideShowDotIndicatorContainer;
    private SlideShowRetainFragment mSlideShowRetainFragment;
    private Timer mTimer;
    private TimerTask mFadeOutTimerTask;
    private String mTitle;
    private ArrayList<Bitmap> mBitmaps;
    private ArrayList<String> mBitmapUrls;
    private int mBitmapWidth;
    private int mBitmapHeight;

    /**
     * SlideShow page change listener
     */
    private class SlideShowPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            showSlideShowDotIndicators(position, true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            Log.d(" *** SlideShowPageChangeListener.onPageScrollStateChanged", "state: " + state);

            switch (state) {
                case ViewPager.SCROLL_STATE_IDLE:
                    startFadeOutSlideShowDotIndicators();
                    break;
                case ViewPager.SCROLL_STATE_DRAGGING:
                    showSlideShowDotIndicators(mSlideShowPager.getCurrentItem(), false);
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    // Same as onPageSelected()
                    break;
            }

        }

    }

    private class FadeOutTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        fadeOutSlideShowDotIndicators();
                    }
                });
        }
    }

    public static void start(Context context, String title, ArrayList<Bitmap> bitmaps, ArrayList<String> bitmapUrls,
                             int maxWidth, int maxHeight) {
        Intent intent = new Intent(context, SlideShowActivity.class);

        intent.putExtra(SlideShowActivity.EXTRA_IN_TITLE, title);
        intent.putParcelableArrayListExtra(SlideShowActivity.EXTRA_IN_BITMAPS, bitmaps);
        intent.putStringArrayListExtra(SlideShowActivity.EXTRA_IN_BITMAP_URLS, bitmapUrls);
        intent.putExtra(SlideShowActivity.EXTRA_IN_MAX_WIDTH, maxWidth);
        intent.putExtra(SlideShowActivity.EXTRA_IN_MAX_HEIGHT, maxHeight);

        context.startActivity(intent);
    }

    private void getIntentExtras(Intent intent) {
        mTitle = intent.getStringExtra(EXTRA_IN_TITLE);
        mBitmaps = intent.getParcelableArrayListExtra(EXTRA_IN_BITMAPS);
        mBitmapUrls = intent.getStringArrayListExtra(EXTRA_IN_BITMAP_URLS);
        mBitmapWidth = intent.getIntExtra(EXTRA_IN_MAX_WIDTH, 0);
        mBitmapHeight = intent.getIntExtra(EXTRA_IN_MAX_HEIGHT, 0);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Timer
        mTimer = new Timer();

        // Intent extras
        getIntentExtras(getIntent());

        // Setup layout
        setContentView(R.layout.slideshow_activity);

        // Action bar
        getSupportActionBar().setTitle(mTitle);
        getSupportActionBar().hide();

        // SlideShow retain fragment
        mSlideShowRetainFragment = SlideShowRetainFragment.findOrCreateRetainFragment(getSupportFragmentManager());

        // Setup view pager
        mSlideShowDotIndicatorContainer = (ViewGroup) findViewById(R.id.slideshow_dot_indicator_container);
        mSlideShowAdapter = new SlideShowPagerAdapter(getSupportFragmentManager(), mSlideShowRetainFragment, mBitmaps,
                mBitmapUrls, mBitmapWidth, mBitmapHeight);
        mSlideShowPager = (ViewPager) findViewById(R.id.slideshow_pager);
        mSlideShowPager.setAdapter(mSlideShowAdapter);
        mSlideShowPager.setOnPageChangeListener(new SlideShowPageChangeListener());

        // Setup view pager dot indicators
        createSlideShowDotIndicators();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //
        showSlideShowDotIndicators(mSlideShowPager.getCurrentItem(), true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //
        stopFadeOutSlideShowDotIndicators();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        mSlideShowRetainFragment.addBitmapToMemoryCache(key, bitmap);
    }

    private void createSlideShowDotIndicators() {
        Log.d(" *** SlideShowActivity.createSlideShowDotIndicators", " ");

        // Add dot indicators to container
        for (int i = 0; i < mSlideShowAdapter.getCount(); i++) {
            ImageView dotIndicator = new ImageView(this);
            dotIndicator.setImageResource(R.drawable.dot_indicator_transition);
            mSlideShowDotIndicatorContainer.addView(dotIndicator, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

    }

    private void showSlideShowDotIndicators(int position, boolean startFadeOut) {
        Log.d(" *** SlideShowActivity.showSlideShowDotIndicators", "position: " + position);

        // Reset/Update SlideShow dot indicators
        for (int i = 0; i < mSlideShowDotIndicatorContainer.getChildCount(); i++) {
            ImageView dotIndicator = (ImageView) mSlideShowDotIndicatorContainer.getChildAt(i);

            // Set drawable based on selected page position
            dotIndicator.setImageResource((i == position) ?
                    R.drawable.dot_indicator_selected_transition : R.drawable.dot_indicator_transition);

            // Reset transition
            TransitionDrawable transitionDrawable = (TransitionDrawable) dotIndicator.getDrawable();
            transitionDrawable.setCrossFadeEnabled(true);
            transitionDrawable.resetTransition();

        }

        // Start fade out countdown
        if (startFadeOut) {
            startFadeOutSlideShowDotIndicators();
        }

    }

    private void startFadeOutSlideShowDotIndicators() {
        Log.d(" *** SlideShowActivity.startFadeOutSlideShowDotIndicators", " ");

        // Cancel the previously scheduled task
        stopFadeOutSlideShowDotIndicators();

        // Schedule the new task
        mFadeOutTimerTask = new FadeOutTimerTask();
        mTimer.schedule(mFadeOutTimerTask, SLIDE_SHOW_DOT_INDICATORS_ON_SCREEN_TIME);

    }

    private void stopFadeOutSlideShowDotIndicators() {
        Log.d(" *** SlideShowActivity.stopFadeOutSlideShowDotIndicators",
                "mFadeOutTimerTask: " + ((mFadeOutTimerTask == null) ? "null" : mFadeOutTimerTask.toString()));

        // Cancel the previously scheduled task
        if (mFadeOutTimerTask != null) {
            mFadeOutTimerTask.cancel();
            mFadeOutTimerTask = null;
        }

    }

    private void fadeOutSlideShowDotIndicators() {
        Log.d(" *** SlideShowActivity.fadeOutSlideShowDotIndicators", " ");

        // Start transition for each dot indicator
        for (int i = 0; i < mSlideShowDotIndicatorContainer.getChildCount(); i++) {
            ImageView dotIndicator = (ImageView) mSlideShowDotIndicatorContainer.getChildAt(i);
            TransitionDrawable transitionDrawable = (TransitionDrawable) dotIndicator.getDrawable();
            transitionDrawable.startTransition(SLIDE_SHOW_DOT_INDICATORS_FADE_OUT_TIME);
        }

    }

}
