package com.example.NCore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ListView;

import java.util.ArrayList;

public class CategoriesActivity extends ActionBarActivity implements QuestionDialogFragment.QuestionDialogListener,
        AlertDialogFragment.AlertDialogListener, LogoutTask.LogoutTaskListener, IndexTask.IndexTaskListener,
        DialogInterface.OnCancelListener {

    // Dialog fragment tags
    private static final String TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT = "TAG_LOGOUT_QUESTION_DIALOG_FRAGMENT";
    private static final String TAG_LOGOUT_ALERT_DIALOG_FRAGMENT = "TAG_LOGOUT_ALERT_DIALOG_FRAGMENT";

    // Dialog labels
    private static final String QUESTION_LOGOUT = "Tényleg kilépsz?";
    private static final String ALERT_LOGOUT = "Sikertelen kijelentkezés, próbáld újra!";

    // Progress dialog
    private static final String PROGRESS_MESSAGE_LOGOUT = "Kijelentkezés...";

    // Members
    private IndexTask mIndexTask;
    private LogoutTask mLogoutTask;
    private MenuItem mSearchItem;
    private ArrayList<DrawerListItem> mDrawerItems;
    private DrawerLayout mCategoriesDrawerLayout;
    private CategoriesActionBarDrawerToggle mDrawerToggle;
    private ListView mCategoriesDrawerListView;
    private NCoreSession mNCoreSession;
    private CategoriesPagerAdapter mCategoriesPagerAdapter;
    private ViewPager mCategoriesViewPager;
    private ProgressDialog mProgressDialog;

    /**
     * Navigation drawer listener
     */
    private class CategoriesDrawerItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectCategoriesDrawerItem(position);
        }

    }

    /**
     * Tab navigation listener
     */
    private class CategoriesTabNavigationListener implements ActionBar.TabListener {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            mCategoriesViewPager.setCurrentItem(tab.getPosition());
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
    private class CategoriesViewPagerListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            getSupportActionBar().setSelectedNavigationItem(position);
        }

    }

    /**
     * Search view query text listener
     */
    private class CategoriesQueryTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            return submitCategoriesQueryText(s);
        }
    }

    /**
     * Navigation drawer toggle
     */
    private class CategoriesActionBarDrawerToggle extends ActionBarDrawerToggle {

        public CategoriesActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes,
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

            // Execute the IndexTask (only once in a session)
            if (mNCoreSession.getLogoutUrl() == null) {
                mIndexTask = (IndexTask) new IndexTask().execute(new IndexTask.Param[]{new IndexTask.Param(this)});
            }

            /*
             * Layout
             */

            setContentView(R.layout.categories_activity);

            /*
             * Navigation drawer
             */

            // Views
            mCategoriesDrawerLayout = (DrawerLayout) findViewById(R.id.categories_drawer_layout);
            mCategoriesDrawerListView = (ListView) findViewById(R.id.categories_drawer);

            // Drawer listener
            mDrawerToggle = new CategoriesActionBarDrawerToggle(this, mCategoriesDrawerLayout,
                    R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close);
            mCategoriesDrawerLayout.setDrawerListener(mDrawerToggle);

            // Adapter
            mDrawerItems = DrawerListItem.buildDrawerItemList(this, mNCoreSession, R.array.drawer_types,
                    R.array.drawer_icons, R.array.drawer_labels);
            mCategoriesDrawerListView.setAdapter(new DrawerListAdapter(this, mDrawerItems));

            // Click listener
            mCategoriesDrawerListView.setOnItemClickListener(new CategoriesDrawerItemClickListener());

            // Default
            mCategoriesDrawerListView.setItemChecked(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES, true);
            mCategoriesDrawerListView.setSelection(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES);

            /*
             * Action bar
             */

            // Get tab labels
            String[] tabLabels = getResources().getStringArray(R.array.categories_action_list);

            /*
             * View pager
             */

            mCategoriesPagerAdapter = new CategoriesPagerAdapter(getSupportFragmentManager(), tabLabels);
            mCategoriesViewPager = (ViewPager) findViewById(R.id.categories_pager);
            mCategoriesViewPager.setOnPageChangeListener(new CategoriesViewPagerListener());
            mCategoriesViewPager.setAdapter(mCategoriesPagerAdapter);

            // Tab navigation
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Add navigation tabs
            CategoriesTabNavigationListener listener = new CategoriesTabNavigationListener();

            for (int i = 0; i < tabLabels.length; i++) {
                getSupportActionBar()
                        .addTab(getSupportActionBar()
                                .newTab()
                                        .setText(tabLabels[i])
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
        if (mDrawerToggle != null) mDrawerToggle.syncState();

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

        // Cancel the progress dialog
        if ((mProgressDialog != null) && (mProgressDialog.isShowing())) mProgressDialog.cancel();

        super.onDestroy();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.categories_activity_actions, menu);

        // Search menu item and view
        mSearchItem = menu.findItem(R.id.categories_action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

        // Add event listeners
        searchView.setOnQueryTextListener(new CategoriesQueryTextListener());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Navigation drawer handler
        if (mDrawerToggle.onOptionsItemSelected(item)) {
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

        // Cancel the progress dialog
        mProgressDialog.cancel();

        // Successful logout
        if ((result != null) && result.loggedOut) {

            // Close the session
            mNCoreSession.logout(this);

            finish();
        }

        // Logout failed
        else {
            // Absorb logout failure

            // Inform the user
            //new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, ALERT_LOGOUT).show();

            // Close the session
            mNCoreSession.logout(this);

            finish();
        }

    }

    @Override
    public void onLogoutTaskException(Exception e) {

        // Cancel the progress dialog
        mProgressDialog.cancel();

        // Absorb logout failure

        // Alert toast
        //new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, e.getMessage()).show();

        // Close the session
        mNCoreSession.logout(this);

        finish();
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

    @Override
    public void onCancel(DialogInterface dialogInterface) {

        // Cancel the running LogoutTask
        if ((mLogoutTask != null) && (mLogoutTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mLogoutTask.cancel(true);
        }

    }

    private void logout() {

        // Show progress dialog
        mProgressDialog = ProgressDialog.show(this, null, PROGRESS_MESSAGE_LOGOUT, true, true, this);

        LogoutTask.Param logoutTaskParam = new LogoutTask.Param(this, mNCoreSession.getLogoutUrl());

        // Execute the Logout task
        mLogoutTask = (LogoutTask) new LogoutTask().execute(new LogoutTask.Param[]{logoutTaskParam});

    }

    private void selectCategoriesDrawerItem(int position) {

        switch (position) {

            // CATEGORIES
            case DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES:

                // Highlight the selected item
                mCategoriesDrawerListView.setItemChecked(position, true);
                mCategoriesDrawerListView.setSelection(position);

                break;

            default:

                // Highlight the default item
                mCategoriesDrawerListView.setItemChecked(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES, true);
                mCategoriesDrawerListView.setSelection(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES);

                new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_INFO, getResources()
                        .getString(R.string.drawer_info_later)).show();

                break;
        }

        // Update the title
        //setTitle(mDrawerItems[position]);

        // Close the drawer
        mCategoriesDrawerLayout.closeDrawer(mCategoriesDrawerListView);

    }

    private boolean submitCategoriesQueryText(String query) {

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
