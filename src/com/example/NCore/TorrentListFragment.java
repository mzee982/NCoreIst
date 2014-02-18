package com.example.NCore;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

public class TorrentListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<TorrentEntry>> {

    // Arguments
    public static final String ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX = "ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX";
    public static final String ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY = "ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY";

    // Loaders
    private static final int LOADER_TORRENT_LIST = 1;
    private static final int LOADER_TORRENT_SEARCH_LIST = 2;

    // Members
    private int mTorrentListCategoryIndex;
    private String mTorrentListSearchQuery;
    private TorrentListAdapter mAdapter;

    /**
     * More button click listener
     */
    private class MoreButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            onMoreButtonClick(view);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Input arguments
        Bundle args = getArguments();
        if (args != null) {
            mTorrentListCategoryIndex = args.getInt(ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX);
            mTorrentListSearchQuery = args.getString(ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

        /*
         * Layout
         */

        //return inflater.inflate(R.layout.torrent_list_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
         * Torrent list
         */

        Button moreButton = new Button(getActivity());
        moreButton.setText(getResources().getText(R.string.more_button_label));
        moreButton.setLayoutParams(
                new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        moreButton.setOnClickListener(new MoreButtonClickListener());
        getListView().addFooterView(moreButton);

        mAdapter = new TorrentListAdapter(getActivity());
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
    public void onDestroy() {

        // Release
        //mAdapter = null;

        super.onDestroy();
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        // Torrent list loader/Torrent search loader
        if ((i == LOADER_TORRENT_LIST) || (i == LOADER_TORRENT_SEARCH_LIST)) {
            return new TorrentListLoader(getActivity(), bundle);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<TorrentEntry>> listLoader, List<TorrentEntry> torrentEntries) {

        if ((listLoader.getId() == LOADER_TORRENT_LIST) || (listLoader.getId() == LOADER_TORRENT_SEARCH_LIST)) {
            mAdapter.appendData(torrentEntries);
        }

        // Notify the list view that the data has changed
        mAdapter.notifyDataSetChanged();

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

        mAdapter.clear();
        mAdapter.notifyDataSetInvalidated();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // TODO: Implement
    }

    public void onMoreButtonClick(View view) {

        // Start out with a progress indicator
        setListShown(false);

        // Set input arguments
        Bundle args = new Bundle();
        args.putInt(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX, mTorrentListCategoryIndex);
        args.putString(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY, mTorrentListSearchQuery);
        args.putInt(TorrentListLoader.ARGUMENT_IN_TORRENT_LIST_PAGE_INDEX, mAdapter.getLastPageIndex() + 1);

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
