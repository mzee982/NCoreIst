package com.example.NCore;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class IndexPagerAdapter extends FragmentStatePagerAdapter {
    private String[] mTitles;

    public IndexPagerAdapter(FragmentManager fragmentManager, String[] titles) {
        super(fragmentManager);

        mTitles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        Bundle fragmentArguments = null;

        // Create a new fragment based on position
        Fragment torrentListFragment = new TorrentListFragment();

        // Set input arguments
        fragmentArguments = new Bundle();
        fragmentArguments.putInt(TorrentListFragment.ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX, i);
        torrentListFragment.setArguments(fragmentArguments);

        return torrentListFragment;
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
