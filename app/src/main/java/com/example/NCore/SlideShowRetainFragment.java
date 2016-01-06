package com.example.NCore;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;

public class SlideShowRetainFragment extends Fragment {
    private static final String TAG = "SLIDE_SHOW_RETAIN_FRAGMENT";
    private LruCache<String, Bitmap> mBitmapMemoryCache;

    public SlideShowRetainFragment() {

        /*
         * Initialize memory cache
         */

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mBitmapMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;

            }

        };

    }

    public static SlideShowRetainFragment findOrCreateRetainFragment(FragmentManager fragmentManager) {
        SlideShowRetainFragment fragment = (SlideShowRetainFragment) fragmentManager.findFragmentByTag(TAG);

        if (fragment == null) {
            fragment = new SlideShowRetainFragment();
            fragmentManager.beginTransaction().add(fragment, TAG).commit();
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mBitmapMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mBitmapMemoryCache.get(key);
    }

}
