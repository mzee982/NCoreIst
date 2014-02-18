package com.example.NCore;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import java.util.HashMap;
import java.util.Map;

public class IndexActivity
        extends ActionBarActivity
        implements
                QuestionDialogFragment.QuestionDialogListener,
                AlertDialogFragment.AlertDialogListener,
                LogoutTask.LogoutTaskListener,
                IndexTask.IndexTaskListener {

    // Request codes
    private static final int REQUEST_CODE_LOGIN = 1;

    // Dialog fragment tags
    private static final String TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT = "TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT";
    private static final String TAG_LOGOUT_ALERT_DIALOG_FRAGMENT = "TAG_LOGOUT_ALERT_DIALOG_FRAGMENT";

    // Torrent list fragment tags
    private static final String TAG_TORRENT_LIST_ALL_FRAGMENT = "TAG_TORRENT_LIST_ALL_FRAGMENT";
    private static final String TAG_TORRENT_LIST_MOVIE_FRAGMENT = "TAG_TORRENT_LIST_MOVIE_FRAGMENT";
    private static final String TAG_TORRENT_LIST_SERIES_FRAGMENT = "TAG_TORRENT_LIST_SERIES_FRAGMENT";
    private static final String TAG_TORRENT_LIST_MUSIC_FRAGMENT = "TAG_TORRENT_LIST_MUSIC_FRAGMENT";
    private static final String TAG_TORRENT_LIST_XXX_FRAGMENT = "TAG_TORRENT_LIST_XXX_FRAGMENT";
    private static final String TAG_TORRENT_LIST_GAME_FRAGMENT = "TAG_TORRENT_LIST_GAME_FRAGMENT";
    private static final String TAG_TORRENT_LIST_SOFTWARE_FRAGMENT = "TAG_TORRENT_LIST_SOFTWARE_FRAGMENT";
    private static final String TAG_TORRENT_LIST_BOOK_FRAGMENT = "TAG_TORRENT_LIST_BOOK_FRAGMENT";

    //
    private static final String QUESTION_LOGOUT = "Do you really want to logout?";
    private static final String ALERT_LOGOUT = "Logout failed. Try again.";

    // Members
    private MenuItem mSearchItem;
    private String[] mIndexLeftDrawerItems;
    private DrawerLayout mIndexDrawerLayout;
    private IndexActionBarDrawerToggle mIndexDrawerToggle;
    private ListView mIndexLeftDrawerListView;
    private NCoreSession mNCoreSession;
    private LogoutTask mLogoutTask;
    private IndexTask mIndexTask;
    private String mFragmentTag;

    /**
     * Navigation drawer listener
     */
    private class IndexLeftDrawerItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectIndexLeftDrawerItem(position);
        }

    }

    /**
     * Drop-down navigation listener
     */
    private class IndexNavigationListener implements ActionBar.OnNavigationListener {

        @Override
        public boolean onNavigationItemSelected(int position, long itemId) {
            return selectIndexNavigationItem(position, itemId);
        }
    }

    /**
     * Search view query text listener
     */
    private class IndexQueryTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            return submitIndexQueryText(s);
        }
    }

    /**
     *
     */
    private class IndexActionBarDrawerToggle extends ActionBarDrawerToggle {

        public IndexActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes,
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance();

        // Check the login status
        if (!mNCoreSession.isLoggedIn()) {

            // Jump to login
            jumpToLoginActivity();

        }

        // Logged in
        else {

            // Execute the IndexTask
            mIndexTask = (IndexTask) new IndexTask().execute(new IndexTask.IndexTaskListener[]{this});

            /*
             * Layout
             */

            setContentView(R.layout.index);

            /*
             * Navigation drawer
             */

            // Index left drawer
            mIndexLeftDrawerItems = getResources().getStringArray(R.array.index_left_drawer_items);
            mIndexDrawerLayout = (DrawerLayout) findViewById(R.id.IndexDrawerLayout);

            mIndexDrawerToggle = new IndexActionBarDrawerToggle(this, mIndexDrawerLayout,
                    R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
            mIndexDrawerLayout.setDrawerListener(mIndexDrawerToggle);

            mIndexLeftDrawerListView = (ListView) findViewById(R.id.IndexLeftDrawer);

            // Set the adapter for the list view
            mIndexLeftDrawerListView.setAdapter(
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_activated_1, mIndexLeftDrawerItems));

            // Set the list's click listener
            mIndexLeftDrawerListView.setOnItemClickListener(new IndexLeftDrawerItemClickListener());

            /*
             * Action bar
             */

            // Drop-down navigation
            SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(
                    this, R.array.index_action_list, android.R.layout.simple_spinner_dropdown_item);

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, new IndexNavigationListener());

            // Action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mIndexDrawerToggle != null) mIndexDrawerToggle.syncState();

    }

    @Override
    protected void onDestroy() {

        // Cancel the running LogoutTask
        if ((mLogoutTask != null) && (mLogoutTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mLogoutTask.cancel(true);
        }

        // Cancel the running IndexTask
        if ((mIndexTask != null) && (mIndexTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mIndexTask.cancel(true);
        }

        // Release
        mLogoutTask = null;
        mIndexTask = null;

        super.onDestroy();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mIndexDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.index_activity_actions, menu);
        mSearchItem = menu.findItem(R.id.index_action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

        // Configure the search info and add any event listeners
        searchView.setOnQueryTextListener(new IndexQueryTextListener());

        /*
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });
        */

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Navigation drawer handler
        if (mIndexDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_LOGIN) {

            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    finish();

                    break;

                // Activity.RESULT_OK, ...
                default:

                    // Restart the index activity
                    restartActivity(data);

            }

        }

    }

    @Override
    public void onQuestionDialogResult(int responseCode, DialogFragment dialogFragment) {

        // Logout question
        if (TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {

            switch (responseCode) {
                case QuestionDialogFragment.RESPONSE_YES:
                    logout();

                    break;
                case QuestionDialogFragment.RESPONSE_NO:
                    // Nothing to do
                    break;
                // QuestionDialogFragment.RESPONSE_CANCEL
                default:
                    // Nothing to do
            }

        }

    }

    @Override
    public void onAlertDialogResult(int responseCode, DialogFragment dialogFragment) {

        // Logout alert
        if (TAG_LOGOUT_ALERT_DIALOG_FRAGMENT.equals(dialogFragment.getTag())) {

            switch (responseCode) {
                case AlertDialogFragment.RESPONSE_CANCEL:
                    //mNCoreSession.logout();
                    finish();

                    break;
                default:
                    // Nothing to do
            }

        }

    }

    @Override
    public void onLogoutTaskResult(Boolean result) {

        // Successful logout
        if ((result != null) && result.booleanValue()) {

            // Close the session
            mNCoreSession.logout();

            finish();
        }

        // Logout failed
        else {

            // Inform the user
            AlertDialogFragment alertDialog = new AlertDialogFragment(ALERT_LOGOUT);
            alertDialog.show(getSupportFragmentManager(), TAG_LOGOUT_ALERT_DIALOG_FRAGMENT);

        }

    }

    @Override
    public void onIndexTaskResult(Map<String, Object> result) {

        if (result != null) {
            String logoutUrl = (String) result.get(IndexTask.PARAM_OUT_LOGOUT_URL);

            // Store the logout URL in the session
            mNCoreSession.setLogoutUrl(logoutUrl);
        }

    }

    @Override
    public void onBackPressed() {

        // Initiate the logout process
        QuestionDialogFragment questionDialog = new QuestionDialogFragment(QUESTION_LOGOUT);
        questionDialog.show(getSupportFragmentManager(), TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT);

        //super.onBackPressed();
    }

    private void jumpToLoginActivity() {

        // Jump to the login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);

    }

    private void logout() {

        // Setup LogoutTask input parameters
        HashMap<String,Object> inputParams = new HashMap<String,Object>();

        inputParams.put(LogoutTask.PARAM_IN_EXECUTOR, this);
        inputParams.put(LogoutTask.PARAM_IN_LOGOUT_URL, mNCoreSession.getLogoutUrl());

        HashMap<String,Object>[] inputParamsArray = new HashMap[1];
        inputParamsArray[0] = inputParams;

        // Execute the Logout task
        mLogoutTask = (LogoutTask) new LogoutTask().execute(inputParamsArray);

    }

    private void restartActivity(Intent data) {

        // Restart the index activity
        Intent intent = new Intent(this, IndexActivity.class);
        intent.putExtras(data);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private void selectIndexLeftDrawerItem(int position) {

        // Highlight the selected item, update the title, and close the drawer
        //mIndexLeftDrawerListView.setItemChecked(position, true);
        //setTitle(mIndexLeftDrawerItems[position]);
        mIndexDrawerLayout.closeDrawer(mIndexLeftDrawerListView);

    }

    private boolean selectIndexNavigationItem(int position, long itemId) {
        mFragmentTag = null;
        Bundle fragmentArguments = null;

        // Select the fragment tag
        switch (position) {
            case 0:
                mFragmentTag = TAG_TORRENT_LIST_ALL_FRAGMENT;
                break;
            case 1:
                mFragmentTag = TAG_TORRENT_LIST_MOVIE_FRAGMENT;
                break;
            case 2:
                mFragmentTag = TAG_TORRENT_LIST_SERIES_FRAGMENT;
                break;
            case 3:
                mFragmentTag = TAG_TORRENT_LIST_MUSIC_FRAGMENT;
                break;
            case 4:
                mFragmentTag = TAG_TORRENT_LIST_XXX_FRAGMENT;
                break;
            case 5:
                mFragmentTag = TAG_TORRENT_LIST_GAME_FRAGMENT;
                break;
            case 6:
                mFragmentTag = TAG_TORRENT_LIST_SOFTWARE_FRAGMENT;
                break;
            case 7:
                mFragmentTag = TAG_TORRENT_LIST_BOOK_FRAGMENT;
                break;
        }

        // Create a new fragment based on position
        Fragment torrentListFragment = new TorrentListFragment();

        // Set input arguments
        fragmentArguments = new Bundle();
        fragmentArguments.putInt(TorrentListFragment.ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX, position);
        torrentListFragment.setArguments(fragmentArguments);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.IndexContentFrame, torrentListFragment, mFragmentTag).commit();

        return true;
    }

    private boolean submitIndexQueryText(String query) {

        // Start the search activity
        Intent intent = new Intent(this, SearchActivity.class);

        intent.putExtra(SearchActivity.EXTRA_TORRENT_LIST_CATEGORY_INDEX, getSupportActionBar().getSelectedNavigationIndex());
        intent.putExtra(SearchActivity.EXTRA_TORRENT_LIST_SEARCH_QUERY, query);

        startActivity(intent);

        // Close the search action view
        MenuItemCompat.collapseActionView(mSearchItem);

        return true;
    }

}
