package com.example.NCore;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import java.util.ArrayList;

public class SlideShowActivity extends ActionBarActivity {

    //
    public static final String EXTRA_IN_TITLE = "EXTRA_IN_TITLE";
    public static final String EXTRA_IN_BITMAPS = "EXTRA_IN_BITMAPS";
    public static final String EXTRA_IN_BITMAP_URLS = "EXTRA_IN_BITMAP_URLS";
    public static final String EXTRA_IN_BITMAP_WIDTH = "EXTRA_IN_BITMAP_WIDTH";
    public static final String EXTRA_IN_BITMAP_HEIGHT = "EXTRA_IN_BITMAP_HEIGHT";

    // Members
    private SlideShowPagerAdapter mSlideShowAdapter;
    private ViewPager mSlideShowPager;
    private ArrayList<Bitmap> mBitmaps;
    private ArrayList<String> mBitmapUrls;
    private int mBitmapWidth;
    private int mBitmapHeight;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Input EXTRAs
        String title = getIntent().getStringExtra(EXTRA_IN_TITLE);
        mBitmaps = getIntent().getParcelableArrayListExtra(EXTRA_IN_BITMAPS);
        mBitmapUrls = getIntent().getStringArrayListExtra(EXTRA_IN_BITMAP_URLS);
        mBitmapWidth = getIntent().getIntExtra(EXTRA_IN_BITMAP_WIDTH, 0);
        mBitmapHeight = getIntent().getIntExtra(EXTRA_IN_BITMAP_HEIGHT, 0);

        // Setup layout
        setContentView(R.layout.slideshow);

        // Action bar
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().hide();

        // Title
        getSupportActionBar().setTitle(title);

        // Setup view pager
        mSlideShowAdapter = new SlideShowPagerAdapter(
                getSupportFragmentManager(), mBitmaps, mBitmapUrls, mBitmapWidth, mBitmapHeight);
        mSlideShowPager = (ViewPager) findViewById(R.id.SlideShowPager);
        mSlideShowPager.setAdapter(mSlideShowAdapter);

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