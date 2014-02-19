package com.example.NCore;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SearchActivity extends ActionBarActivity {

    // Intent extras
    public static final String EXTRA_TORRENT_LIST_CATEGORY_INDEX = "EXTRA_TORRENT_LIST_CATEGORY_INDEX";
    public static final String EXTRA_TORRENT_LIST_SEARCH_QUERY = "EXTRA_TORRENT_LIST_SEARCH_QUERY";

    // Torrent list fragment tags
    private static final String TAG_TORRENT_LIST_SEARCH_FRAGMENT = "TAG_TORRENT_LIST_SEARCH_FRAGMENT";

    // Members
    private int mTorrentListCategoryIndex;
    private String mTorrentListSearchQuery;
    private NCoreSession mNCoreSession;
    private String mFragmentTag;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance(this);

        /*
         * Intent extras
         */

        mTorrentListCategoryIndex = getIntent().getIntExtra(EXTRA_TORRENT_LIST_CATEGORY_INDEX, 0);
        mTorrentListSearchQuery = getIntent().getStringExtra(EXTRA_TORRENT_LIST_SEARCH_QUERY);

        /*
         * Layout
         */

        setContentView(R.layout.search);

        /*
         * Action bar
         */

        // Action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Set title
        getSupportActionBar().setTitle(
                (getResources().getStringArray(R.array.index_action_list))[mTorrentListCategoryIndex] +
                ": " +
                mTorrentListSearchQuery);

        /*
         * Content fragment
         */

        // Fragment tag
        mFragmentTag = TAG_TORRENT_LIST_SEARCH_FRAGMENT;

        // Create a new fragment
        Fragment torrentListFragment = new TorrentListFragment();

        // Set input arguments
        Bundle fragmentArguments = new Bundle();
        fragmentArguments.putInt(
                TorrentListFragment.ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX, mTorrentListCategoryIndex);
        fragmentArguments.putString(TorrentListFragment.ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY, mTorrentListSearchQuery);

        torrentListFragment.setArguments(fragmentArguments);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.SearchContentFrame, torrentListFragment, mFragmentTag).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.search_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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
