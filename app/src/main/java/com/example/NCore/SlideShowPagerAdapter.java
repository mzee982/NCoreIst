package com.example.NCore;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class SlideShowPagerAdapter extends FragmentStatePagerAdapter {

    // Members
    private SlideShowRetainFragment mSlideShowRetainFragment;
    private ArrayList<Bitmap> mBitmaps;
    private ArrayList<String> mBitmapUrls;
    private int mBitmapWidth;
    private int mBitmapHeight;

    public SlideShowPagerAdapter(
            FragmentManager fragmentManager,
            SlideShowRetainFragment slideShowRetainFragment,
            ArrayList<Bitmap> aBitmaps,
            ArrayList<String> aBitmapUrls,
            int aBitmapWidth,
            int aBitmapHeight) {
        super(fragmentManager);

        mSlideShowRetainFragment = slideShowRetainFragment;
        mBitmaps = aBitmaps;
        mBitmapUrls = aBitmapUrls;
        mBitmapWidth = aBitmapWidth;
        mBitmapHeight = aBitmapHeight;
    }

    @Override
    public int getCount() {
        return mBitmaps.size() + mBitmapUrls.size();
    }

    @Override
    public Fragment getItem(int position) {

        // Bitmap already loaded
        if (position < mBitmaps.size()) {
            return SlideShowFragment.create(mBitmaps.get(position), null, mBitmapWidth, mBitmapHeight);

        }

        // Load bitmap from cache or pass the image URL
        else {
            String bitmapUrl = mBitmapUrls.get(position - mBitmaps.size());

            // Lookup in bitmap memory cache
            Bitmap bitmap = mSlideShowRetainFragment.getBitmapFromMemCache(bitmapUrl);

            if (bitmap != null) {
                return SlideShowFragment.create(bitmap, null, mBitmapWidth, mBitmapHeight);
            }

            else {
                return SlideShowFragment.create(null, bitmapUrl, mBitmapWidth, mBitmapHeight);
            }

        }

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return String.valueOf(position + 1);
    }
    
}
