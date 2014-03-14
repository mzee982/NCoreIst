package com.example.NCore;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.*;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.List;

public class TorrentListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<TorrentEntry>> {

    // Arguments
    private static final String ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX = "ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX";
    private static final String ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY = "ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY";

    // File provider authority
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.NCore.FileProvider";

    // Loaders
    private static final int LOADER_TORRENT_LIST = 1;
    private static final int LOADER_TORRENT_SEARCH_LIST = 2;

    // Instance states
    private static final String INSTANCE_STATE_LOAD_IN_PROGRESS = "INSTANCE_STATE_LOAD_IN_PROGRESS";

    // Members
    private int mTorrentListCategoryIndex;
    private String mTorrentListSearchQuery;
    private TorrentListAdapter mAdapter;
    private boolean bLoadInProgress;

    /**
     * List scroll listener
     */
    private class ListScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {}

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            onListScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

    }

    public static TorrentListFragment create(int categoryIndex, String searchQuery) {
        TorrentListFragment torrentListFragment = new TorrentListFragment();
        Bundle fragmentArguments = new Bundle();

        // Set input arguments
        fragmentArguments.putInt(TorrentListFragment.ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX, categoryIndex);
        fragmentArguments.putString(TorrentListFragment.ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY, searchQuery);
        torrentListFragment.setArguments(fragmentArguments);

        return torrentListFragment;
    }

    private void getInputArguments(Bundle args) {

        if (args != null) {
            mTorrentListCategoryIndex = args.getInt(ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX);
            mTorrentListSearchQuery = args.getString(ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Input arguments
        getInputArguments(getArguments());

        // Previous instance state
        if (savedInstanceState != null) {
            bLoadInProgress = savedInstanceState.getBoolean(INSTANCE_STATE_LOAD_IN_PROGRESS, false);
        }

        // Action bar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
         * Torrent list view
         */

        // Enabling fast scroll on ListView
        getListView().setFastScrollEnabled(true);

        // List scroll listener
        getListView().setOnScrollListener(new ListScrollListener());

        // List adapter
        mAdapter = new TorrentListAdapter(getActivity(), TorrentListAdapter.TYPE_TORRENT_LIST);
        setListAdapter(mAdapter);

        // Start out with a progress indicator
        setListShown(false);

        /*
         * Loader
         */

        // Set input arguments
        Bundle args = new Bundle();
        args.putInt(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX, mTorrentListCategoryIndex);
        if (mTorrentListSearchQuery != null) {
            args.putString(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY, mTorrentListSearchQuery);
        }

        // Init the search loader
        if (mTorrentListSearchQuery != null) {
            getLoaderManager().initLoader(LOADER_TORRENT_SEARCH_LIST, args, this);
        }

        // Init the torrent list loader
        else {
            getLoaderManager().initLoader(LOADER_TORRENT_LIST, args, this);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(INSTANCE_STATE_LOAD_IN_PROGRESS, bLoadInProgress);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.torrent_list_fragment_actions, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.torrent_list_action_refresh) {
            onRefresh();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        // Torrent list loader / Torrent search loader
        if ((i == LOADER_TORRENT_LIST) || (i == LOADER_TORRENT_SEARCH_LIST)) {
            return new TorrentListLoader(getActivity(), bundle);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<TorrentEntry>> listLoader, List<TorrentEntry> torrentEntries) {

        // Load data into adapter
        if ((listLoader.getId() == LOADER_TORRENT_LIST) || (listLoader.getId() == LOADER_TORRENT_SEARCH_LIST)) {
            mAdapter.appendData(torrentEntries);
            mAdapter.setHasMoreResults(((TorrentListLoader) listLoader).hasMoreResults());
            bLoadInProgress = false;
        }

        // The list should now be shown
        if (isResumed()) {
            setListShown(true);
        }
        else {
            setListShownNoAnimation(true);
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        bLoadInProgress = false;
        mAdapter.clear();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        // Filter out progress list item
        if (v.getTag() != TorrentListAdapter.TAG_PROGRESS_ITEM) {

            // Navigate to the TorrentDetailsActivity
            navigateToTorrentDetailsActivity(id);

        }

    }

    public void onListScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        //
        if ((mAdapter != null) && mAdapter.hasMoreResults() && !bLoadInProgress
                && ((firstVisibleItem + visibleItemCount - 1) >= (totalItemCount - 2 * visibleItemCount))) {

            bLoadInProgress = true;

            // Set input arguments
            Bundle args = new Bundle();
            args.putInt(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX, mTorrentListCategoryIndex);
            args.putString(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY, mTorrentListSearchQuery);
            args.putInt(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_PAGE_INDEX, mAdapter.getLastShowedPageIndex() + 1);

            // Restart the torrent list loader
            if (mTorrentListSearchQuery != null) {
                getLoaderManager().restartLoader(LOADER_TORRENT_SEARCH_LIST, args, this);
            }

            // Restart the torrent search loader
            else {
                getLoaderManager().restartLoader(LOADER_TORRENT_LIST, args, this);
            }

        }

    }

    public void onRefresh() {

        // Reset list adapter
        setListAdapter(null);
        mAdapter.clear();
        setListAdapter(mAdapter);

        // Start out with a progress indicator
        setListShown(false);

        // Set input arguments
        Bundle args = new Bundle();
        args.putInt(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX, mTorrentListCategoryIndex);
        args.putString(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY, mTorrentListSearchQuery);
        args.putInt(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_PAGE_INDEX, 0);

        // Restart the torrent list loader
        if (mTorrentListSearchQuery != null) {
            getLoaderManager().restartLoader(LOADER_TORRENT_SEARCH_LIST, args, this);
        }

        // Restart the torrent search loader
        else {
            getLoaderManager().restartLoader(LOADER_TORRENT_LIST, args, this);
        }

    }

    private void navigateToTorrentDetailsActivity(long torrentId) {

        // Navigate to the TorrentDetailsActivity
        TorrentDetailsActivity.start(getActivity(), torrentId);

    }

}
