package com.example.NCore;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class SlideShowPagerAdapter extends FragmentStatePagerAdapter {

    // Members
    private ArrayList<Bitmap> mBitmaps;
    private ArrayList<String> mBitmapUrls;
    private int mBitmapWidth;
    private int mBitmapHeight;

    public SlideShowPagerAdapter(
            FragmentManager fragmentManager,
            ArrayList<Bitmap> aBitmaps,
            ArrayList<String> aBitmapUrls,
            int aBitmapWidth,
            int aBitmapHeight) {
        super(fragmentManager);

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
        return SlideShowFragment.create(
                position < mBitmaps.size() ? mBitmaps.get(position) : null,
                position >= mBitmaps.size() ? mBitmapUrls.get(position - mBitmaps.size()) : null,
                mBitmapWidth,
                mBitmapHeight);
    }

}
