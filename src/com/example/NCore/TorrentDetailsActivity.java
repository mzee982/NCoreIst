package com.example.NCore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class TorrentDetailsActivity
        extends ActionBarActivity
        implements
                LoaderManager.LoaderCallbacks<TorrentDetails>,
                DownloadTorrentTask.DownloadTorrentTaskListener,
                DownloadImageTask.DownloadImageTaskListener {

    // Toast
    private static final String TOAST_TORRENT_DOWNLOAD_IN_PROGRESS = "Downloading the torrent...";
    private static final String TOAST_TORRENT_DOWNLOAD_READY = "Download finished";

    // App chooser
    private static final String APP_CHOOSER_TORRENT_TITLE = "Open torrent via";

    // Intent extras
    public static final String EXTRA_TORRENT_DETAILS_ID = "EXTRA_TORRENT_DETAILS_ID";

    // Instance state
    private static final String INSTANCE_STATE_TORRENT_ID = "INSTANCE_STATE_TORRENT_ID";

    // Loaders
    private static final int LOADER_TORRENT_DETAILS = 1;
    private static final int LOADER_OTHER_VERSIONS = 2;

    // Image ids
    private static final int IMAGE_ID_COVER = 1;
    private static final int IMAGE_ID_SAMPLE_1 = 2;
    private static final int IMAGE_ID_SAMPLE_2 = 3;
    private static final int IMAGE_ID_SAMPLE_3 = 4;

    // Members
    private long mTorrentId;
    private TorrentDetails mTorrentDetails;
    private NCoreSession mNCoreSession;
    private DownloadTorrentTask mDownloadTorrentTask;
    private DownloadImageTask mDownloadCoverImageTask;
    private DownloadImageTask mDownloadSampleImage1Task;
    private DownloadImageTask mDownloadSampleImage2Task;
    private DownloadImageTask mDownloadSampleImage3Task;
    private TorrentListAdapter mOtherVersionsAdapter;

    //
    private class OnOtherVersionsItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            onOtherVersionsItemClick(view);
        }

    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance(this);

        /*
         * Intent extras
         */

        mTorrentId = getIntent().getLongExtra(EXTRA_TORRENT_DETAILS_ID, -1L);

        /*
         * Saved instance state
         */

        if ((savedInstanceState != null)
                && (savedInstanceState.containsKey(INSTANCE_STATE_TORRENT_ID))
                && (mTorrentId == -1L)) {
            mTorrentId = savedInstanceState.getLong(INSTANCE_STATE_TORRENT_ID);
        }

        /*
         * Layout
         */

        setContentView(R.layout.torrent_details);

        /*
         * Action bar
         */

        // Action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        /*
         * Loader
         */

        // Set input arguments
        Bundle args = new Bundle();
        args.putLong(TorrentDetailsLoader.ARGUMENT_IN_TORRENT_ID, mTorrentId);

        // Init the torrent details loader
        getSupportLoaderManager().initLoader(LOADER_TORRENT_DETAILS, args, this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putLong(INSTANCE_STATE_TORRENT_ID, mTorrentId);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {

        // Cancel the running tasks
        if ((mDownloadTorrentTask != null) && (mDownloadTorrentTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mDownloadTorrentTask.cancel(true);
        }

        if ((mDownloadCoverImageTask != null) && (mDownloadCoverImageTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mDownloadCoverImageTask.cancel(true);
        }

        if ((mDownloadSampleImage1Task != null) && (mDownloadSampleImage1Task.getStatus() == AsyncTask.Status.RUNNING)) {
            mDownloadSampleImage1Task.cancel(true);
        }

        if ((mDownloadSampleImage2Task != null) && (mDownloadSampleImage2Task.getStatus() == AsyncTask.Status.RUNNING)) {
            mDownloadSampleImage2Task.cancel(true);
        }

        if ((mDownloadSampleImage3Task != null) && (mDownloadSampleImage3Task.getStatus() == AsyncTask.Status.RUNNING)) {
            mDownloadSampleImage3Task.cancel(true);
        }

        // Release
        mDownloadTorrentTask = null;
        mDownloadCoverImageTask = null;
        mDownloadSampleImage1Task = null;
        mDownloadSampleImage2Task = null;
        mDownloadSampleImage3Task = null;

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.torrent_details_activity_actions, menu);

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

            case R.id.torrent_details_action_download:
                downloadTorrent();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<TorrentDetails> onCreateLoader(int i, Bundle bundle) {

        // Torrent details loader
        if (i == LOADER_TORRENT_DETAILS) {
            return new TorrentDetailsLoader(this, bundle);
        }

        // Other versions loader
        else if (i == LOADER_OTHER_VERSIONS) {
            return new OtherVersionsLoader(this, bundle);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<TorrentDetails> torrentDetailsLoader, TorrentDetails torrentDetails) {

        // Torrent list loader
        if (LOADER_TORRENT_DETAILS == torrentDetailsLoader.getId()) {

            // Load data
            mTorrentDetails = torrentDetails;

            // Download the cover image
            if (mTorrentDetails.getCoverUrl() != null) downloadImage(IMAGE_ID_COVER, mTorrentDetails.getCoverUrl());

            // Download the sample images
            if (mTorrentDetails.hasSampleImages()) {
                if (mTorrentDetails.getSampleImg1Url() != null)
                    downloadImage(IMAGE_ID_SAMPLE_1, mTorrentDetails.getSampleImg1Url());

                if (mTorrentDetails.getSampleImg2Url() != null)
                    downloadImage(IMAGE_ID_SAMPLE_2, mTorrentDetails.getSampleImg2Url());

                if (mTorrentDetails.getSampleImg3Url() != null)
                    downloadImage(IMAGE_ID_SAMPLE_3, mTorrentDetails.getSampleImg3Url());
            }

            // Other versions
            if (mTorrentDetails.hasOtherVersions()) downloadOtherVersions();

            // Populate torrent detail fields
            populateTorrentDetails();

        }

        // Other versions loader
        else if (LOADER_OTHER_VERSIONS == torrentDetailsLoader.getId()) {

            // Show other versions list
            LinearLayout otherVersionsLayout = (LinearLayout) findViewById(R.id.OtherVersionsLayout);
            otherVersionsLayout.setVisibility(LinearLayout.VISIBLE);

            // Load data
            if (torrentDetails.getOtherVersions() != null) {
                mTorrentDetails.setOtherVersions(torrentDetails.getOtherVersions());

                // Setup other versions list
                //ListView otherVersionsList = (ListView) findViewById(R.id.OtherVersionsList);
                mOtherVersionsAdapter = new TorrentListAdapter(this, TorrentListAdapter.TYPE_OTHER_VERSIONS);
                mOtherVersionsAdapter.appendData(mTorrentDetails.getOtherVersions());
                //otherVersionsList.setAdapter(mOtherVersionsAdapter);
                //otherVersionsList.setOnItemClickListener(); // AdapterView.OnItemClickListener

                // OnClick listener
                OnOtherVersionsItemClickListener onOtherVersionsItemClickListener =
                        new OnOtherVersionsItemClickListener();

                // Populate other versions
                for (int i = 0; i < mOtherVersionsAdapter.getCount(); i++) {
                    View otherVersionsItem = mOtherVersionsAdapter.getView(i, null, otherVersionsLayout);
                    otherVersionsLayout.addView(otherVersionsItem);
                    otherVersionsItem.setOnClickListener(onOtherVersionsItemClickListener);
                }

            }

        }

    }

    @Override
    public void onLoaderReset(Loader<TorrentDetails> torrentDetailsLoader) {

        if (LOADER_OTHER_VERSIONS == torrentDetailsLoader.getId()) {
            mOtherVersionsAdapter.clear();
        }

    }

    @Override
    public void onDownloadTorrentTaskResult(File aFile) {

        Toast.makeText(this, TOAST_TORRENT_DOWNLOAD_READY, Toast.LENGTH_SHORT).show();

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

        if (chooserIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooserIntent);
        }

    }

    @Override
    public void onDownloadImageTaskResult(int aImageId, Bitmap aBitmap) {

        if (aBitmap != null) {

            // Show images layout
            RelativeLayout torrentDetailsImageLayout = (RelativeLayout) findViewById(R.id.TorrentDetailsImageLayout);
            if (torrentDetailsImageLayout.getVisibility() != RelativeLayout.VISIBLE)
                torrentDetailsImageLayout.setVisibility(RelativeLayout.VISIBLE);

            switch (aImageId) {

                case IMAGE_ID_COVER:
                    mTorrentDetails.setCoverImg(aBitmap);
                    ImageView torrentDetailsImageView = (ImageView) findViewById(R.id.TorrentDetailsCover);
                    torrentDetailsImageView.setImageBitmap(mTorrentDetails.getCoverImg());
                    torrentDetailsImageView.setVisibility(ImageView.VISIBLE);
                    break;

                case IMAGE_ID_SAMPLE_1:
                    mTorrentDetails.setSampleImg1(aBitmap);
                    ImageView torrentDetailsSampleImage1View = (ImageView) findViewById(R.id.TorrentDetailsSampleImage1);
                    torrentDetailsSampleImage1View.setImageBitmap(mTorrentDetails.getSampleImg1());
                    torrentDetailsSampleImage1View.setVisibility(ImageView.VISIBLE);
                    break;

                case IMAGE_ID_SAMPLE_2:
                    mTorrentDetails.setSampleImg2(aBitmap);
                    ImageView torrentDetailsSampleImage2View = (ImageView) findViewById(R.id.TorrentDetailsSampleImage2);
                    torrentDetailsSampleImage2View.setImageBitmap(mTorrentDetails.getSampleImg2());
                    torrentDetailsSampleImage2View.setVisibility(ImageView.VISIBLE);
                    break;

                case IMAGE_ID_SAMPLE_3:
                    mTorrentDetails.setSampleImg3(aBitmap);
                    ImageView torrentDetailsSampleImage3View = (ImageView) findViewById(R.id.TorrentDetailsSampleImage3);
                    torrentDetailsSampleImage3View.setImageBitmap(mTorrentDetails.getSampleImg3());
                    torrentDetailsSampleImage3View.setVisibility(ImageView.VISIBLE);
                    break;

            }
        }

    }

    public void onImagesClick(View aView) {
        String title;
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        ArrayList<String> bitmapUrls = new ArrayList<String>();
        DrawerLayout torrentDetailsDrawerLayout = (DrawerLayout) findViewById(R.id.TorrentDetailsDrawerLayout);

        // Intent extras
        title = mTorrentDetails.getMainTitle();

        if (mTorrentDetails.getCoverImg() != null) bitmaps.add(mTorrentDetails.getCoverImg());

        if (mTorrentDetails.hasSampleImages()) {
            if (mTorrentDetails.getSampleLargeImg1Url() != null)
                bitmapUrls.add(mTorrentDetails.getSampleLargeImg1Url());
            if (mTorrentDetails.getSampleLargeImg2Url() != null)
                bitmapUrls.add(mTorrentDetails.getSampleLargeImg2Url());
            if (mTorrentDetails.getSampleLargeImg3Url() != null)
                bitmapUrls.add(mTorrentDetails.getSampleLargeImg3Url());
        }

        // Intent
        Intent intent = new Intent(this, SlideShowActivity.class);
        intent.putExtra(SlideShowActivity.EXTRA_IN_TITLE, title);
        intent.putParcelableArrayListExtra(SlideShowActivity.EXTRA_IN_BITMAPS, bitmaps);
        intent.putStringArrayListExtra(SlideShowActivity.EXTRA_IN_BITMAP_URLS, bitmapUrls);
        intent.putExtra(SlideShowActivity.EXTRA_IN_BITMAP_WIDTH, torrentDetailsDrawerLayout.getWidth());
        intent.putExtra(SlideShowActivity.EXTRA_IN_BITMAP_HEIGHT, torrentDetailsDrawerLayout.getHeight());

        // SlideShow Activity
        startActivity(intent);

    }

    private void onOtherVersionsItemClick(View view) {
        long torrentId = ((TorrentListAdapter.ViewHolder) view.getTag()).id;

        // Start the search activity
        Intent intent = new Intent(this, TorrentDetailsActivity.class);
        intent.putExtra(TorrentDetailsActivity.EXTRA_TORRENT_DETAILS_ID, torrentId);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private void populateTorrentDetails() {

        // Set action bar title
        setTitle(mTorrentDetails.getMainTitle());

        // Main title
        ((TextView) findViewById(R.id.TorrentDetailsMainTitleValue)).setText(mTorrentDetails.getMainTitle());

        // Details
        ((TextView) findViewById(R.id.TorrentDetailsNameValue)).setText(mTorrentDetails.getName());
        ((TextView) findViewById(R.id.TorrentDetailsTypeValue)).setText(mTorrentDetails.getType());
        ((TextView) findViewById(R.id.TorrentDetailsUploadedValue)).setText(mTorrentDetails.getUploaded());
        ((TextView) findViewById(R.id.TorrentDetailsUploaderValue)).setText(mTorrentDetails.getUploader());
        ((TextView) findViewById(R.id.TorrentDetailsSeedersValue)).setText(mTorrentDetails.getSeeders());
        ((TextView) findViewById(R.id.TorrentDetailsLeechersValue)).setText(mTorrentDetails.getLeechers());
        ((TextView) findViewById(R.id.TorrentDetailsDownloadedValue)).setText(mTorrentDetails.getDownloaded());
        ((TextView) findViewById(R.id.TorrentDetailsSpeedValue)).setText(mTorrentDetails.getSpeed());
        ((TextView) findViewById(R.id.TorrentDetailsSizeValue)).setText(mTorrentDetails.getSize());
        ((TextView) findViewById(R.id.TorrentDetailsFilesValue)).setText(mTorrentDetails.getFiles());

        // Movie details
        if (mTorrentDetails.hasMovieDetails()) {

            ((TableLayout) findViewById(R.id.TorrentMovieDetailsTable)).setVisibility(TableLayout.VISIBLE);

            if (mTorrentDetails.getTitle() != null) {
                TextView title = ((TextView) findViewById(R.id.TorrentDetailsTitleValue));
                title.setText(mTorrentDetails.getTitle());
                ((TableRow) findViewById(R.id.TorrentDetailsTitleRow)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getReleaseDate() != null) {
                TextView releaseDate = ((TextView) findViewById(R.id.TorrentDetailsReleaseDateValue));
                releaseDate.setText(mTorrentDetails.getReleaseDate());
                releaseDate.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.TorrentDetailsReleaseDateRow)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getDirector() != null) {
                TextView director = ((TextView) findViewById(R.id.TorrentDetailsDirectorValue));
                director.setText(mTorrentDetails.getDirector());
                director.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.TorrentDetailsDirectorRow)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getCast() != null) {
                TextView cast = ((TextView) findViewById(R.id.TorrentDetailsCastValue));
                cast.setText(mTorrentDetails.getCast());
                cast.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.TorrentDetailsCastRow)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getCountry() != null) {
                TextView country = ((TextView) findViewById(R.id.TorrentDetailsCountryValue));
                country.setText(mTorrentDetails.getCountry());
                country.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.TorrentDetailsCountryRow)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getDuration() != null) {
                TextView duration = ((TextView) findViewById(R.id.TorrentDetailsDurationValue));
                duration.setText(mTorrentDetails.getDuration());
                duration.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.TorrentDetailsDurationRow)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getLabels() != null) {
                TextView labels = ((TextView) findViewById(R.id.TorrentDetailsLabelsValue));
                labels.setText(mTorrentDetails.getLabels());
                labels.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.TorrentDetailsLabelsRow)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getImdb() != null) {
                TextView imdb = ((TextView) findViewById(R.id.TorrentDetailsImdbValue));
                imdb.setText(mTorrentDetails.getImdb());
                imdb.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.TorrentDetailsImdbRow)).setVisibility(TableRow.VISIBLE);
            }

        }

    }

    private void downloadTorrent() {

        // Toast
        Toast.makeText(this, TOAST_TORRENT_DOWNLOAD_IN_PROGRESS, Toast.LENGTH_LONG).show();

        /*
         * Download the torrent
         */

        // Setup DownloadTorrentTask input parameters
        HashMap<String,Object> inputParams = new HashMap<String,Object>();

        inputParams.put(DownloadTorrentTask.PARAM_IN_EXECUTOR, this);
        inputParams.put(DownloadTorrentTask.PARAM_IN_TORRENT_ID, mTorrentId);

        HashMap<String,Object>[] inputParamsArray = new HashMap[1];
        inputParamsArray[0] = inputParams;

        // Execute the DownloadTorrentTask
        mDownloadTorrentTask = (DownloadTorrentTask) new DownloadTorrentTask().execute(inputParamsArray);

    }

    private void downloadImage(int imageId, String imageUrl) {

        // Setup DownloadImageTask input parameters
        HashMap<String,Object> inputParams = new HashMap<String,Object>();

        inputParams.put(DownloadImageTask.PARAM_IN_EXECUTOR, this);
        inputParams.put(DownloadImageTask.PARAM_IN_IMAGE_URL, imageUrl);
        inputParams.put(DownloadImageTask.PARAM_IN_IMAGE_ID, imageId);

        HashMap<String,Object>[] inputParamsArray = new HashMap[1];
        inputParamsArray[0] = inputParams;

        // Execute the DownloadImageTask
        DownloadImageTask downloadImageTask = (DownloadImageTask) new DownloadImageTask().execute(inputParamsArray);

        switch (imageId) {
            case IMAGE_ID_COVER:
                mDownloadCoverImageTask = downloadImageTask;
                break;
            case IMAGE_ID_SAMPLE_1:
                mDownloadSampleImage1Task = downloadImageTask;
                break;
            case IMAGE_ID_SAMPLE_2:
                mDownloadSampleImage2Task = downloadImageTask;
                break;
            case IMAGE_ID_SAMPLE_3:
                mDownloadSampleImage3Task = downloadImageTask;
                break;
        }

    }

    private void downloadOtherVersions() {

        // Set input arguments
        Bundle args = new Bundle();
        args.putString(OtherVersionsLoader.ARGUMENT_IN_OTHER_VERSIONS_ID, mTorrentDetails.getOtherVersionsId());
        args.putString(OtherVersionsLoader.ARGUMENT_IN_OTHER_VERSIONS_FID, mTorrentDetails.getOtherVersionsFid());

        // Init the other versions loader
        getSupportLoaderManager().initLoader(LOADER_OTHER_VERSIONS, args, this);

    }

}
