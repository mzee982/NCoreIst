package com.example.NCore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
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

import java.util.ArrayList;

public class TorrentDetailsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<TorrentDetails>,
                DownloadTorrentTask.DownloadTorrentTaskListener, DownloadImageTask.DownloadImageTaskListener {

    // Intent extras
    private static final String EXTRA_TORRENT_ID = "EXTRA_TORRENT_ID";

    // Toast
    private static final String TOAST_TORRENT_DOWNLOAD_IN_PROGRESS = "Torrent letöltése...";
    private static final String TOAST_TORRENT_DOWNLOAD_READY = "Torrent letöltése kész";

    // App chooser
    private static final String APP_CHOOSER_TORRENT_TITLE = "Open torrent via";

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
    private NCoreSession mNCoreSession;
    private DownloadTorrentTask mDownloadTorrentTask;
    private DownloadImageTask mDownloadCoverImageTask;
    private DownloadImageTask mDownloadSampleImage1Task;
    private DownloadImageTask mDownloadSampleImage2Task;
    private DownloadImageTask mDownloadSampleImage3Task;
    private DrawerLayout mTorrentDetailsDrawerLayout;
    private ListView mTorrentDetailsDrawerListView;
    private TorrentDetailsActionBarDrawerToggle mDrawerToggle;
    private ArrayList<DrawerListItem> mDrawerItems;
    private long mTorrentId;
    private TorrentDetails mTorrentDetails;
    private TorrentListAdapter mOtherVersionsAdapter;
    private InfoAlertToast mInfoToast;

    /**
     * Navigation drawer toggle
     */
    private class TorrentDetailsActionBarDrawerToggle extends ActionBarDrawerToggle {

        public TorrentDetailsActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes,
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
    private class TorrentDetailsDrawerItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectTorrentDetailsDrawerItem(position);
        }

    }

    /**
     * Other versions listener
     */
    private class OtherVersionsItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            onOtherVersionsItemClick(view);
        }

    }

    public static void start(Context context, long torrentId) {
        Intent intent = new Intent(context, TorrentDetailsActivity.class);

        intent.putExtra(TorrentDetailsActivity.EXTRA_TORRENT_ID, torrentId);

        context.startActivity(intent);
    }

    private void getIntentExtras(Intent intent) {
        mTorrentId = intent.getLongExtra(EXTRA_TORRENT_ID, -1L);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nCORE session object
        mNCoreSession = NCoreSession.getInstance(this);

        /*
         * Intent extras
         */

        getIntentExtras(getIntent());

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

        setContentView(R.layout.torrent_details_activity);

        /*
         * Navigation drawer
         */

        // Views
        mTorrentDetailsDrawerLayout = (DrawerLayout) findViewById(R.id.torrent_details_drawer_layout);
        mTorrentDetailsDrawerListView = (ListView) findViewById(R.id.torrent_details_drawer);

        // Drawer listener
        mDrawerToggle = new TorrentDetailsActionBarDrawerToggle(this, mTorrentDetailsDrawerLayout,
                R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close);
        mTorrentDetailsDrawerLayout.setDrawerListener(mDrawerToggle);

        // Adapter
        mDrawerItems = DrawerListItem.buildDrawerItemList(this, mNCoreSession, R.array.drawer_types,
                R.array.drawer_icons, R.array.drawer_labels);
        mTorrentDetailsDrawerListView.setAdapter(new DrawerListAdapter(this, mDrawerItems));

        // Click listener
        mTorrentDetailsDrawerListView.setOnItemClickListener(new TorrentDetailsDrawerItemClickListener());

        // Default
        mTorrentDetailsDrawerListView.clearChoices();
        //mTorrentDetailsDrawerListView.setItemChecked(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES, true);
        //mTorrentDetailsDrawerListView.setSelection(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES);

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
        if ((mDownloadTorrentTask != null) && (mDownloadTorrentTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mDownloadTorrentTask.cancel(true);
        }

        if ((mDownloadCoverImageTask != null) && (mDownloadCoverImageTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mDownloadCoverImageTask.cancel(true);
        }

        if ((mDownloadSampleImage1Task != null) && (mDownloadSampleImage1Task.getStatus() != AsyncTask.Status.FINISHED)) {
            mDownloadSampleImage1Task.cancel(true);
        }

        if ((mDownloadSampleImage2Task != null) && (mDownloadSampleImage2Task.getStatus() != AsyncTask.Status.FINISHED)) {
            mDownloadSampleImage2Task.cancel(true);
        }

        if ((mDownloadSampleImage3Task != null) && (mDownloadSampleImage3Task.getStatus() != AsyncTask.Status.FINISHED)) {
            mDownloadSampleImage3Task.cancel(true);
        }

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
            LinearLayout otherVersionsLayout = (LinearLayout) findViewById(R.id.other_versions_layout);
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
                OtherVersionsItemClickListener otherVersionsItemClickListener =
                        new OtherVersionsItemClickListener();

                // Populate other versions
                for (int i = 0; i < mOtherVersionsAdapter.getCount(); i++) {
                    View otherVersionsItem = mOtherVersionsAdapter.getView(i, null, otherVersionsLayout);
                    otherVersionsLayout.addView(otherVersionsItem);
                    otherVersionsItem.setOnClickListener(otherVersionsItemClickListener);
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
    public void onDownloadTorrentTaskResult(DownloadTorrentTask.Result result) {

        if (result != null) {

            // Info toast
            if (mInfoToast != null) mInfoToast.cancel();
            mInfoToast = new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_INFO,
                    TOAST_TORRENT_DOWNLOAD_READY);
            mInfoToast.setDuration(Toast.LENGTH_SHORT);
            mInfoToast.show();

            // Get torrent file from the file provider
            Uri torrentUri = Uri.fromFile(result.file);
            //Uri torrentUri = FileProvider.getUriForFile(getActivity(), FILE_PROVIDER_AUTHORITY, aFile);

            // Get MIME type
            String fileName = result.file.getName();
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

    }

    @Override
    public void onDownloadTorrentTaskException(Exception e) {

        // Alert toast
        if (mInfoToast != null) mInfoToast.cancel();
        new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, e.getMessage()).show();

    }

    @Override
    public void onDownloadImageTaskResult(DownloadImageTask.Result result) {

        if ((result != null) && (result.bitmap != null)) {

            // Show images layout and divider
            ImageView torrentDetailsImagesHeaderDivider = (ImageView) findViewById(
                    R.id.torrent_details_images_header_divider);
            LinearLayout torrentDetailsImagesLayout = (LinearLayout) findViewById(
                    R.id.torrent_details_images_layout);
            ImageView torrentDetailsImagesFooterDivider = (ImageView) findViewById(
                    R.id.torrent_details_images_footer_divider);

            if (torrentDetailsImagesLayout.getVisibility() != RelativeLayout.VISIBLE) {
                torrentDetailsImagesHeaderDivider.setVisibility(ImageView.VISIBLE);
                torrentDetailsImagesLayout.setVisibility(RelativeLayout.VISIBLE);
                torrentDetailsImagesFooterDivider.setVisibility(ImageView.VISIBLE);
            }

            switch (result.imageId) {

                case IMAGE_ID_COVER:
                    mTorrentDetails.setCoverImg(result.bitmap);
                    ImageView torrentDetailsImageView = (ImageView) findViewById(R.id.torrent_details_cover_image);
                    torrentDetailsImageView.setImageBitmap(mTorrentDetails.getCoverImg());
                    torrentDetailsImageView.setVisibility(ImageView.VISIBLE);
                    break;

                case IMAGE_ID_SAMPLE_1:
                    mTorrentDetails.setSampleImg1(result.bitmap);
                    ImageView torrentDetailsSampleImage1View = (ImageView) findViewById(R.id.torrent_details_sample_image1);
                    torrentDetailsSampleImage1View.setImageBitmap(mTorrentDetails.getSampleImg1());
                    torrentDetailsSampleImage1View.setVisibility(ImageView.VISIBLE);
                    break;

                case IMAGE_ID_SAMPLE_2:
                    mTorrentDetails.setSampleImg2(result.bitmap);
                    ImageView torrentDetailsSampleImage2View = (ImageView) findViewById(R.id.torrent_details_sample_image2);
                    torrentDetailsSampleImage2View.setImageBitmap(mTorrentDetails.getSampleImg2());
                    torrentDetailsSampleImage2View.setVisibility(ImageView.VISIBLE);
                    break;

                case IMAGE_ID_SAMPLE_3:
                    mTorrentDetails.setSampleImg3(result.bitmap);
                    ImageView torrentDetailsSampleImage3View = (ImageView) findViewById(R.id.torrent_details_sample_image3);
                    torrentDetailsSampleImage3View.setImageBitmap(mTorrentDetails.getSampleImg3());
                    torrentDetailsSampleImage3View.setVisibility(ImageView.VISIBLE);
                    break;

            }
        }

    }

    @Override
    public void onDownloadImageTaskException(Exception e) {

        // Alert toast
        new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_ALERT, e.getMessage()).show();

    }

    public void onImagesClick(View aView) {
        String title;
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        ArrayList<String> bitmapUrls = new ArrayList<String>();
        DrawerLayout torrentDetailsDrawerLayout = (DrawerLayout) findViewById(R.id.torrent_details_drawer_layout);

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

        // SlideShow Activity
        // TODO: maxWidth és maxHeight pontosabb meghatározása
        navigateToSlideShowActivity(title, bitmaps, bitmapUrls, torrentDetailsDrawerLayout.getWidth(),
                torrentDetailsDrawerLayout.getHeight());

    }

    private void onOtherVersionsItemClick(View view) {
        long torrentId = ((TorrentListAdapter.ViewHolder) view.getTag()).id;

        // Start the TorrentDetailsActivity
        navigateToTorrentDetailsActivity(torrentId);

    }

    private void selectTorrentDetailsDrawerItem(int position) {

        switch (position) {

            // CATEGORIES
            case DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES:

                // Highlight the selected item
                mTorrentDetailsDrawerListView.setItemChecked(position, true);
                mTorrentDetailsDrawerListView.setSelection(position);

                // Navigate to Categories
                NavUtils.navigateUpFromSameTask(this);

                break;

            default:

                // Highlight the default item
                mTorrentDetailsDrawerListView.clearChoices();
                ((DrawerListAdapter) mTorrentDetailsDrawerListView.getAdapter()).notifyDataSetChanged();
                //mTorrentDetailsDrawerListView.setItemChecked(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES, true);
                //mTorrentDetailsDrawerListView.setSelection(DrawerListItem.DRAWER_ITEM_INDEX_CATEGORIES);

                new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_INFO, getResources()
                        .getString(R.string.drawer_info_later)).show();

                break;
        }

        // Update the title
        //setTitle(mDrawerItems[position]);

        // Close the drawer
        mTorrentDetailsDrawerLayout.closeDrawer(mTorrentDetailsDrawerListView);

    }

    private void populateTorrentDetails() {

        // Set action bar title
        setTitle(mTorrentDetails.getMainTitle());

        // Main title
        ((TextView) findViewById(R.id.torrent_details_main_title_value)).setText(mTorrentDetails.getMainTitle());

        // Details
        ((TextView) findViewById(R.id.torrent_details_name_value)).setText(mTorrentDetails.getName());
        ((TextView) findViewById(R.id.torrent_details_type_value)).setText(mTorrentDetails.getType());
        ((TextView) findViewById(R.id.torrent_details_uploaded_value)).setText(mTorrentDetails.getUploaded());
        ((TextView) findViewById(R.id.torrent_details_uploader_value)).setText(mTorrentDetails.getUploader());
        ((TextView) findViewById(R.id.torrent_details_seeders_value)).setText(mTorrentDetails.getSeeders());
        ((TextView) findViewById(R.id.torrent_details_leechers_value)).setText(mTorrentDetails.getLeechers());
        ((TextView) findViewById(R.id.torrent_details_downloaded_value)).setText(mTorrentDetails.getDownloaded());
        ((TextView) findViewById(R.id.torrent_details_speed_value)).setText(mTorrentDetails.getSpeed());
        ((TextView) findViewById(R.id.torrent_details_size_value)).setText(mTorrentDetails.getSize());
        //((TextView) findViewById(R.id.torrent_details_files_value)).setText(mTorrentDetails.getFiles());

        ((ImageView) findViewById(R.id.torrent_details_category_icon)).setImageResource(
                mTorrentDetails.getCategoryIconResId());

        if (mTorrentDetails.hasMovieProperties()) {
            ((ImageView) findViewById(R.id.torrent_details_movie_property_icon)).setImageResource(
                    mTorrentDetails.getMoviePropertyIcons().get(0));
        }

        // Movie details
        if (mTorrentDetails.hasMovieDetails()) {

            ((LinearLayout) findViewById(R.id.torrent_details_movie_layout)).setVisibility(TableLayout.VISIBLE);

            if (mTorrentDetails.getTitle() != null) {
                TextView title = ((TextView) findViewById(R.id.torrent_details_title_value));
                title.setText(mTorrentDetails.getTitle());
            }

            if (mTorrentDetails.getReleaseDate() != null) {
                TextView releaseDate = ((TextView) findViewById(R.id.torrent_details_release_date_value));
                releaseDate.setText(mTorrentDetails.getReleaseDate());
                releaseDate.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.torrent_details_release_date_row)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getDirector() != null) {
                TextView director = ((TextView) findViewById(R.id.torrent_details_director_value));
                director.setText(mTorrentDetails.getDirector());
                director.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.torrent_details_director_row)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getCast() != null) {
                TextView cast = ((TextView) findViewById(R.id.torrent_details_cast_value));
                cast.setText(mTorrentDetails.getCast());
                cast.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.torrent_details_cast_row)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getCountry() != null) {
                TextView country = ((TextView) findViewById(R.id.torrent_details_country_value));
                country.setText(mTorrentDetails.getCountry());
                country.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.torrent_details_country_row)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getDuration() != null) {
                TextView duration = ((TextView) findViewById(R.id.torrent_details_duration_value));
                duration.setText(mTorrentDetails.getDuration());
                duration.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.torrent_details_duration_row)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getLabels() != null) {
                TextView labels = ((TextView) findViewById(R.id.torrent_details_labels_value));
                labels.setText(mTorrentDetails.getLabels());
                labels.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.torrent_details_labels_row)).setVisibility(TableRow.VISIBLE);
            }

            if (mTorrentDetails.getImdb() != null) {
                TextView imdb = ((TextView) findViewById(R.id.torrent_details_imdb_value));
                imdb.setText(mTorrentDetails.getImdb());
                imdb.setVisibility(TextView.VISIBLE);
                ((TableRow) findViewById(R.id.torrent_details_imdb_row)).setVisibility(TableRow.VISIBLE);
            }

        }

    }

    private void downloadTorrent() {

        // Info toast
        mInfoToast = new InfoAlertToast(this, InfoAlertToast.TOAST_TYPE_INFO,
                TOAST_TORRENT_DOWNLOAD_IN_PROGRESS);
        mInfoToast.setDuration(Toast.LENGTH_SHORT);
        mInfoToast.show();

        /*
         * Download the torrent
         */

        // Setup DownloadTorrentTask input parameters
        DownloadTorrentTask.Param downloadTorrentTaskParam = new DownloadTorrentTask.Param(this, mTorrentId);

        // Execute the DownloadTorrentTask
        mDownloadTorrentTask = (DownloadTorrentTask) new DownloadTorrentTask().execute(
                new DownloadTorrentTask.Param[]{downloadTorrentTaskParam});

    }

    private void downloadImage(int imageId, String imageUrl) {

        // Setup DownloadImageTask input parameters
        DownloadImageTask.Param downloadImageTaskParam = new DownloadImageTask.Param(this, imageUrl, imageId, 0, 0);

        // Execute the DownloadImageTask
        DownloadImageTask downloadImageTask = (DownloadImageTask) new DownloadImageTask().execute(
                new DownloadImageTask.Param[]{downloadImageTaskParam});

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

    private void navigateToTorrentDetailsActivity(long torrentId) {

        // Navigate to the TorrentDetailsActivity
        TorrentDetailsActivity.start(this, torrentId);

    }

    private void navigateToSlideShowActivity(String title, ArrayList<Bitmap> bitmaps, ArrayList<String> bitmapUrls,
                                             int maxWidth, int maxHeight) {

        // Navigate to the SlideShowActivity
        SlideShowActivity.start(this, title, bitmaps, bitmapUrls, maxWidth, maxHeight);

    }

}
