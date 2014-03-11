package com.example.NCore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
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

public class CategoriesActivity extends ActionBarActivity implements QuestionDialogFragment.QuestionDialogListener,
        AlertDialogFragment.AlertDialogListener, LogoutTask.LogoutTaskListener, IndexTask.IndexTaskListener {

    // Dialog fragment tags
    private static final String TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT = "TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT";
    private static final String TAG_LOGOUT_ALERT_DIALOG_FRAGMENT = "TAG_LOGOUT_ALERT_DIALOG_FRAGMENT";

    // Dialog labels
    private static final String QUESTION_LOGOUT = "Tényleg kilépsz?";
    private static final String ALERT_LOGOUT = "Sikertelen kijelentkezés, próbáld újra!";

    // Members
    private IndexTask mIndexTask;
    private LogoutTask mLogoutTask;
    private MenuItem mSearchItem;
    private String[] mIndexLeftDrawerItems;
    private DrawerLayout mCategoriesDrawerLayout;
    private IndexActionBarDrawerToggle mIndexDrawerToggle;
    private ListView mIndexLeftDrawerListView;
    private NCoreSession mNCoreSession;
    private CategoriesPagerAdapter mCategoriesPagerAdapter;
    private ViewPager mViewPager;

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
     * Tab navigation listener
     */
    private class IndexTabNavigationListener implements ActionBar.TabListener {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

    }

    /**
     * View pager listener
     */
    private class IndexViewPagerListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            getSupportActionBar().setSelectedNavigationItem(position);
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
     * Navigation drawer toggle
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

    public static void start(Context context) {
        Intent intent = new Intent(context, CategoriesActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance(this);

        // Check the login status
        if (!mNCoreSession.isLoggedIn()) {

            // Navigate to the LoginActivity
            navigateToLoginActivity();

        }

        // Logged in
        else {

            // Execute the IndexTask
            mIndexTask = (IndexTask) new IndexTask().execute(new IndexTask.Param[]{new IndexTask.Param(this)});

            /*
             * Layout
             */

            setContentView(R.layout.categories_activity);

            /*
             * Navigation drawer
             */

            // Index left drawer
            mIndexLeftDrawerItems = getResources().getStringArray(R.array.index_left_drawer_items);
            mCategoriesDrawerLayout = (DrawerLayout) findViewById(R.id.categories_drawer_layout);

            mIndexDrawerToggle = new IndexActionBarDrawerToggle(this, mCategoriesDrawerLayout,
                    R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close);
            mCategoriesDrawerLayout.setDrawerListener(mIndexDrawerToggle);

            mIndexLeftDrawerListView = (ListView) findViewById(R.id.categories_drawer);

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

            // Get tab labels
            String[] tabs = getResources().getStringArray(R.array.index_action_list);

            // Drop-down navigation
            //SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(
            //        this, R.array.index_action_list, android.R.layout.simple_spinner_dropdown_item);

            //getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            //getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, new IndexNavigationListener());

            /*
             * View pager
             */

            mCategoriesPagerAdapter = new CategoriesPagerAdapter(getSupportFragmentManager(), tabs);
            mViewPager = (ViewPager) findViewById(R.id.categories_pager);
            mViewPager.setOnPageChangeListener(new IndexViewPagerListener());
            mViewPager.setAdapter(mCategoriesPagerAdapter);

            // Tab navigation
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Add navigation tabs
            IndexTabNavigationListener listener = new IndexTabNavigationListener();

            for (int i = 0; i < tabs.length; i++) {
                getSupportActionBar()
                        .addTab(getSupportActionBar()
                                .newTab()
                                        .setText(tabs[i])
                                                .setTabListener(listener));
            }

            // Action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

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
        if ((mLogoutTask != null) && (mLogoutTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mLogoutTask.cancel(true);
        }

        // Cancel the running IndexTask
        if ((mIndexTask != null) && (mIndexTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mIndexTask.cancel(true);
        }

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
        getMenuInflater().inflate(R.menu.categories_activity_actions, menu);
        mSearchItem = menu.findItem(R.id.categories_action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

        // Configure the search info and add any event listeners
        searchView.setOnQueryTextListener(new IndexQueryTextListener());

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
    public void onLogoutTaskResult(LogoutTask.Result result) {

        // Successful logout
        if ((result != null) && result.loggedOut) {

            // Close the session
            mNCoreSession.logout(this);

            finish();
        }

        // Logout failed
        else {

            // Inform the user
            new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, ALERT_LOGOUT).show();

        }

    }

    @Override
    public void onLogoutTaskException(Exception e) {

        // Alert toast
        new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, e.getMessage()).show();

    }

    @Override
    public void onIndexTaskResult(IndexTask.Result result) {

        if (result != null) {

            // Store the logout URL in the session
            mNCoreSession.setLogoutUrl(result.logoutUrl);
        }

    }

    @Override
    public void onIndexTaskException(Exception e) {

        // Alert toast
        new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, e.getMessage()).show();

    }

    @Override
    public void onBackPressed() {

        // Initiate the logout process
        QuestionDialogFragment questionDialog = new QuestionDialogFragment(QUESTION_LOGOUT);
        questionDialog.show(getSupportFragmentManager(), TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT);

    }

    private void logout() {
        LogoutTask.Param logoutTaskParam = new LogoutTask.Param(this, mNCoreSession.getLogoutUrl());

        // Execute the Logout task
        mLogoutTask = (LogoutTask) new LogoutTask().execute(new LogoutTask.Param[]{logoutTaskParam});

    }

    private void selectIndexLeftDrawerItem(int position) {

        // Highlight the selected item, update the title, and close the drawer
        //mIndexLeftDrawerListView.setItemChecked(position, true);
        //setTitle(mIndexLeftDrawerItems[position]);
        mCategoriesDrawerLayout.closeDrawer(mIndexLeftDrawerListView);

    }

    private boolean selectIndexNavigationItem(int position, long itemId) {
/*
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
*/

        return true;
    }

    private boolean submitIndexQueryText(String query) {

        // Start the SearchResultsActivity
        navigateToSearchResultsActivity(getSupportActionBar().getSelectedNavigationIndex(), query);

        // Close the search action view
        MenuItemCompat.collapseActionView(mSearchItem);

        return true;
    }

    private void navigateToLoginActivity() {

        // Navigate to the LoginActivity
        // TODO: LoginActivity felkészítése
        LoginActivity.start(this, null, null, null);

        // This activity should be finished
        finish();

    }

    private void navigateToSearchResultsActivity(int categoryIndex, String searchQuery) {

        // Navigate to the SearchResultsActivity
        SearchResultsActivity.start(this, categoryIndex, searchQuery);

    }

}
