package com.example.NCore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SearchResultsActivity extends ActionBarActivity {

    // Intent extras
    private static final String EXTRA_TORRENT_LIST_CATEGORY_INDEX = "EXTRA_TORRENT_LIST_CATEGORY_INDEX";
    private static final String EXTRA_TORRENT_LIST_SEARCH_QUERY = "EXTRA_TORRENT_LIST_SEARCH_QUERY";

    // Torrent list fragment tags
    private static final String TAG_TORRENT_LIST_SEARCH_FRAGMENT = "TAG_TORRENT_LIST_SEARCH_FRAGMENT";

    // Members
    private int mTorrentListCategoryIndex;
    private String mTorrentListSearchQuery;
    private String mFragmentTag;

    public static void start(Context context, int categoryIndex, String searchQuery) {
        Intent intent = new Intent(context, SearchResultsActivity.class);

        intent.putExtra(SearchResultsActivity.EXTRA_TORRENT_LIST_CATEGORY_INDEX, categoryIndex);
        intent.putExtra(SearchResultsActivity.EXTRA_TORRENT_LIST_SEARCH_QUERY, searchQuery);

        context.startActivity(intent);
    }

    private void getIntentExtras(Intent intent) {

        // Get the intent extra content
        mTorrentListCategoryIndex = intent.getIntExtra(EXTRA_TORRENT_LIST_CATEGORY_INDEX, 0);
        mTorrentListSearchQuery = intent.getStringExtra(EXTRA_TORRENT_LIST_SEARCH_QUERY);

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent extra content
        getIntentExtras(getIntent());

        /*
         * Layout
         */

        setContentView(R.layout.search_results_activity);

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

        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragmentTag = TAG_TORRENT_LIST_SEARCH_FRAGMENT;
        Fragment torrentListFragment = TorrentListFragment.create(mTorrentListCategoryIndex, mTorrentListSearchQuery);

        // Insert the fragment by replacing any existing fragment
        fragmentManager.beginTransaction().replace(R.id.search_results_frame, torrentListFragment, mFragmentTag)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.search_results_activity_actions, menu);

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
