package com.mzee982.android.ncoreist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchResultsActivity extends ActionBarActivity {

    // Intent extras
    private static final String EXTRA_TORRENT_LIST_CATEGORY_INDEX = "EXTRA_TORRENT_LIST_CATEGORY_INDEX";
    private static final String EXTRA_TORRENT_LIST_SEARCH_QUERY = "EXTRA_TORRENT_LIST_SEARCH_QUERY";

    // Torrent list fragment tags
    private static final String TAG_TORRENT_LIST_SEARCH_FRAGMENT = "TAG_TORRENT_LIST_SEARCH_FRAGMENT";

    // Members
    private NCoreSession mNCoreSession;
    private DrawerLayout mSearchResultsDrawerLayout;
    private ListView mSearchResultsDrawerListView;
    private SearchResultsActionBarDrawerToggle mDrawerToggle;
    private ArrayList<DrawerListItem> mDrawerItems;
    private int mTorrentListCategoryIndex;
    private String mTorrentListSearchQuery;
    private MenuItem mSearchItem;

    /**
     * Navigation drawer toggle
     */
    private class SearchResultsActionBarDrawerToggle extends ActionBarDrawerToggle {

        public SearchResultsActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes,
                                                   int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);

            //getSupportActionBar().setTitle();
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);

            //getSupportActionBar().setTitle();
            invalidateOptionsMenu();
        }

    }

    /**
     * Navigation drawer listener
     */
    private class SearchResultsDrawerItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectSearchResultsDrawerItem(position);
        }

    }

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

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance(this);

        // Get the intent extra content
        getIntentExtras(getIntent());

        /*
         * Layout
         */

        setContentView(R.layout.search_results_activity);

        /*
         * Navigation drawer
         */

        // Views
        mSearchResultsDrawerLayout = (DrawerLayout) findViewById(R.id.search_results_drawer_layout);
        mSearchResultsDrawerListView = (ListView) findViewById(R.id.search_results_drawer);

        // Drawer listener
        mDrawerToggle = new SearchResultsActionBarDrawerToggle(this, mSearchResultsDrawerLayout,
                R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close);
        mSearchResultsDrawerLayout.setDrawerListener(mDrawerToggle);

        // Adapter
        mDrawerItems = DrawerListItem.buildDrawerItemList(this, mNCoreSession, R.array.drawer_types,
                R.array.drawer_icons, R.array.drawer_labels);
        mSearchResultsDrawerListView.setAdapter(new DrawerListAdapter(this, mDrawerItems));

        // Click listener
        mSearchResultsDrawerListView.setOnItemClickListener(new SearchResultsDrawerItemClickListener());

        // Default
        mSearchResultsDrawerListView.clearChoices();
        //mSearchResultsDrawerListView.setItemChecked(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES, true);
        //mSearchResultsDrawerListView.setSelection(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES);

        /*
         * Action bar
         */

        // Action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Set title
        getSupportActionBar().setTitle(
                (getResources().getStringArray(R.array.categories_action_list))[mTorrentListCategoryIndex] +
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

    private void selectSearchResultsDrawerItem(int position) {

        switch (position) {

            // CATEGORIES
            case DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES:

                // Highlight the selected item
                mSearchResultsDrawerListView.setItemChecked(position, true);
                mSearchResultsDrawerListView.setSelection(position);

                // Navigate to Categories
                NavUtils.navigateUpFromSameTask(this);

                break;

            default:

                // Highlight the default item
                mSearchResultsDrawerListView.clearChoices();
                ((DrawerListAdapter) mSearchResultsDrawerListView.getAdapter()).notifyDataSetChanged();
                //mSearchResultsDrawerListView.setItemChecked(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES, true);
                //mSearchResultsDrawerListView.setSelection(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES);

                new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_INFO, getResources()
                        .getString(R.string.drawer_info_later)).show();

                break;
        }

        // Update the title
        //setTitle(mDrawerItems[position]);

        // Close the drawer
        mSearchResultsDrawerLayout.closeDrawer(mSearchResultsDrawerListView);

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
