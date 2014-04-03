package com.example.NCore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SearchResultsActivity extends ActionBarActivity {

    // Intent extras
    private static final String EXTRA_TORRENT_LIST_CATEGORY_INDEX = "EXTRA_TORRENT_LIST_CATEGORY_INDEX";
    private static final String EXTRA_TORRENT_LIST_SEARCH_QUERY = "EXTRA_TORRENT_LIST_SEARCH_QUERY";

    // Torrent list fragment tags
    private static final String TAG_TORRENT_LIST_SEARCH_FRAGMENT = "TAG_TORRENT_LIST_SEARCH_FRAGMENT";

    // Members
    private int mTorrentListCategoryIndex;
    private String mTorrentListSearchQuery;
    private MenuItem mSearchItem;

    /**
     * Search view query text listener
     */
    private class SearchResultsQueryTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            return submitSearchResultsQueryText(s);
        }
    }

    /**
     * Search click listener
     */
    private class SearchResultsSearchClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            clickSearch(view);
        }
    }

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
        Fragment torrentListFragment = TorrentListFragment.create(mTorrentListCategoryIndex, mTorrentListSearchQuery);

        // Insert the fragment by replacing any existing fragment
        fragmentManager.beginTransaction().replace(R.id.search_results_frame, torrentListFragment,
                TAG_TORRENT_LIST_SEARCH_FRAGMENT).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.search_results_activity_actions, menu);

        // Search menu item and view
        mSearchItem = menu.findItem(R.id.search_results_action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

        // Add event listeners
        searchView.setOnQueryTextListener(new SearchResultsQueryTextListener());
        searchView.setOnSearchClickListener(new SearchResultsSearchClickListener());

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

    private boolean submitSearchResultsQueryText(String query) {

        // Start a new SearchResultsActivity
        navigateToSearchResultsActivity(mTorrentListCategoryIndex, query);

        // Close the search action view
        MenuItemCompat.collapseActionView(mSearchItem);

        return true;
    }

    private void clickSearch(View view) {

        // Set search view default query text
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        searchView.setQuery(mTorrentListSearchQuery, false);

    }

    private void navigateToSearchResultsActivity(int categoryIndex, String searchQuery) {

        // Navigate to the SearchResultsActivity
        SearchResultsActivity.start(this, categoryIndex, searchQuery);

    }

}
