package com.example.NCore;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class CategoriesPagerAdapter extends FragmentStatePagerAdapter {

    // Members
    private String[] mTitles;

    public CategoriesPagerAdapter(FragmentManager fragmentManager, String[] titles) {
        super(fragmentManager);

        mTitles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        return TorrentListFragment.create(i, null);
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

}
