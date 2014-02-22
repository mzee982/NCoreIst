package com.example.NCore;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class TorrentListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<List<TorrentEntry>>, DownloadTask.DownloadTaskListener {

    // Arguments
    public static final String ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX = "ARGUMENT_IN_TORRENT_LIST_CATEGORY_INDEX";
    public static final String ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY = "ARGUMENT_IN_TORRENT_LIST_SEARCH_QUERY";

    // Toast
    private static final String TOAST_NO_MORE_RESULTS = "No more results";
    private static final String TOAST_TORRENT_DOWNLOAD_IN_PROGRESS = "Downloading the torrent...";
    private static final String TOAST_TORRENT_DOWNLOAD_READY = "Download finished";

    // App chooser
    private static final String APP_CHOOSER_TORRENT_TITLE = "Open torrent via";

    // File provider authority
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.NCore.FileProvider";

    // Loaders
    private static final int LOADER_TORRENT_LIST = 1;
    private static final int LOADER_TORRENT_SEARCH_LIST = 2;

    // Members
    private int mTorrentListCategoryIndex;
    private String mTorrentListSearchQuery;
    private TorrentListAdapter mAdapter;
    private Button mMoreButton;
    private DownloadTask mDownloadTask;

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

        // Action bar
        setHasOptionsMenu(true);
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

        mMoreButton = new Button(getActivity());
        mMoreButton.setText(getResources().getText(R.string.more_button_label));
        mMoreButton.setLayoutParams(
                new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        mMoreButton.setOnClickListener(new MoreButtonClickListener());
        getListView().addFooterView(mMoreButton);

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

        // Cancel the running StartTask
        if ((mDownloadTask != null) && (mDownloadTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mDownloadTask.cancel(true);
        }

        // Release
        mDownloadTask = null;

        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.torrent_list_fragment_actions, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.index_action_refresh) {
            onRefresh();

            return true;
        }

        return super.onOptionsItemSelected(item);
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

        // Load data into adapter
        if ((listLoader.getId() == LOADER_TORRENT_LIST) || (listLoader.getId() == LOADER_TORRENT_SEARCH_LIST)) {
            mAdapter.appendData(torrentEntries);
            mAdapter.setHasMoreResults(((TorrentListLoader) listLoader).hasMoreResults());
        }

        // Show/hide more button
        if (mAdapter.hasMoreResults()) {
            mMoreButton.setVisibility(View.VISIBLE);
        }
        else {
            mMoreButton.setVisibility(View.GONE);
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
        mAdapter.clear();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        // Toast
        Toast.makeText(getActivity(), TOAST_TORRENT_DOWNLOAD_IN_PROGRESS, Toast.LENGTH_LONG).show();

        /*
         * Download the torrent
         */

        // Setup DownloadTask input parameters
        HashMap<String,Object> inputParams = new HashMap<String,Object>();

        inputParams.put(DownloadTask.PARAM_IN_EXECUTOR, this);
        inputParams.put(DownloadTask.PARAM_IN_TORRENT_ID, id);

        HashMap<String,Object>[] inputParamsArray = new HashMap[1];
        inputParamsArray[0] = inputParams;

        // Execute the DownloadTask
        mDownloadTask = (DownloadTask) new DownloadTask().execute(inputParamsArray);

    }

    public void onMoreButtonClick(View view) {

        // Has more results
        if (mAdapter.hasMoreResults()) {

            // Start out with a progress indicator
            setListShown(false);

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

        else {
            Toast.makeText(getActivity(), TOAST_NO_MORE_RESULTS, Toast.LENGTH_SHORT).show();
        }

    }

    public void onRefresh() {

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

        // List view
        getListView().setSelectionAfterHeaderView();

        // Reset adapter
        mAdapter.clear();

    }

    @Override
    public void onDownloadTaskResult(File aFile) {

        Toast.makeText(getActivity(), TOAST_TORRENT_DOWNLOAD_READY, Toast.LENGTH_SHORT).show();

        // Get torrent file from the file provider
        Uri torrentUri = Uri.fromFile(aFile);
        //Uri torrentUri = FileProvider.getUriForFile(getActivity(), FILE_PROVIDER_AUTHORITY, aFile);

        // Get MIME type
        String fileName = aFile.getName();
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        //String mimeType = getActivity().getContentResolver().getType(torrentUri);

        // Intent
        Intent torrentIntent = new Intent();
        torrentIntent.setAction(Intent.ACTION_VIEW);
        torrentIntent.setDataAndType(torrentUri, mimeType);
        torrentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //torrentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //torrentIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Show app chooser
        Intent chooserIntent = Intent.createChooser(torrentIntent, APP_CHOOSER_TORRENT_TITLE);

        if (chooserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(chooserIntent);
        }

    }

}
