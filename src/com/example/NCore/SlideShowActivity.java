package com.example.NCore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import java.util.ArrayList;

public class SlideShowActivity extends ActionBarActivity {

    // Intent extras
    private static final String EXTRA_IN_TITLE = "EXTRA_IN_TITLE";
    private static final String EXTRA_IN_BITMAPS = "EXTRA_IN_BITMAPS";
    private static final String EXTRA_IN_BITMAP_URLS = "EXTRA_IN_BITMAP_URLS";
    private static final String EXTRA_IN_MAX_WIDTH = "EXTRA_IN_MAX_WIDTH";
    private static final String EXTRA_IN_MAX_HEIGHT = "EXTRA_IN_MAX_HEIGHT";

    // Members
    private SlideShowPagerAdapter mSlideshowAdapter;
    private ViewPager mSlideshowPager;
    private String mTitle;
    private ArrayList<Bitmap> mBitmaps;
    private ArrayList<String> mBitmapUrls;
    private int mBitmapWidth;
    private int mBitmapHeight;

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

        // Intent extras
        getIntentExtras(getIntent());

        // Setup layout
        setContentView(R.layout.slideshow_activity);

        // Action bar
        getSupportActionBar().hide();

        // Title
        getSupportActionBar().setTitle(mTitle);

        // Setup view pager
        mSlideshowAdapter = new SlideShowPagerAdapter(getSupportFragmentManager(), mBitmaps, mBitmapUrls, mBitmapWidth,
                mBitmapHeight);
        mSlideshowPager = (ViewPager) findViewById(R.id.slideshow_pager);
        mSlideshowPager.setAdapter(mSlideshowAdapter);

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

}