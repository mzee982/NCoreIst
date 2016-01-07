package com.mzee982.android.ncoreist;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class SlideShowFragment extends Fragment implements DownloadImageTask.DownloadImageTaskListener {

    // Arguments
    private static final String ARGUMENT_IN_BITMAP = "ARGUMENT_IN_BITMAP";
    private static final String ARGUMENT_IN_BITMAP_URL = "ARGUMENT_IN_BITMAP_URL";
    private static final String ARGUMENT_IN_BITMAP_WIDTH = "ARGUMENT_IN_BITMAP_WIDTH";
    private static final String ARGUMENT_IN_BITMAP_HEIGHT = "ARGUMENT_IN_BITMAP_HEIGHT";

    // Members
    private DownloadImageTask mDownloadImageTask;
    private SlideShowFragmentListener mListener;
    private Bitmap mBitmap;
    private String mBitmapUrl;
    private ImageView mSlideShowImageView;
    private ProgressBar mSlideShowImageProgress;
    private int mLayoutWidth;
    private int mLayoutHeight;

    /**
     * SlideShow fragment listener
     */
    public interface SlideShowFragmentListener {
        public void addBitmapToMemoryCache(String key, Bitmap bitmap);
    }

    public static SlideShowFragment create(Bitmap bitmap, String bitmapUrl, int mLayoutWidth, int mLayoutHeight) {
        SlideShowFragment slideShowFragment = new SlideShowFragment();
        Bundle fragmentArguments = new Bundle();

        // Set input arguments
        fragmentArguments.putParcelable(ARGUMENT_IN_BITMAP, bitmap);
        fragmentArguments.putString(ARGUMENT_IN_BITMAP_URL, bitmapUrl);
        fragmentArguments.putInt(ARGUMENT_IN_BITMAP_WIDTH, mLayoutWidth);
        fragmentArguments.putInt(ARGUMENT_IN_BITMAP_HEIGHT, mLayoutHeight);

        slideShowFragment.setArguments(fragmentArguments);

        return slideShowFragment;
    }

    private void getInputArguments(Bundle args) {

        if (args != null) {
            mBitmap = (Bitmap) getArguments().getParcelable(ARGUMENT_IN_BITMAP);
            mBitmapUrl = getArguments().getString(ARGUMENT_IN_BITMAP_URL);
            mLayoutWidth = getArguments().getInt(ARGUMENT_IN_BITMAP_WIDTH, 0);
            mLayoutHeight = getArguments().getInt(ARGUMENT_IN_BITMAP_HEIGHT, 0);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (SlideShowFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SlideShowFragmentListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get input arguments
        getInputArguments(getArguments());

        // Setup layout
        final View inflatedView = inflater.inflate(R.layout.slideshow_fragment, container, false);

        mSlideShowImageView = (ImageView) inflatedView.findViewById(R.id.slideshow_image);
        mSlideShowImageProgress = (ProgressBar) inflatedView.findViewById(R.id.slideshow_progress);
        LinearLayout slideShowLayout = (LinearLayout) getActivity().findViewById(R.id.slideshow_layout);

        // Load image
        if (mBitmap != null) {
            mSlideShowImageView.setImageBitmap(mBitmap);
            mSlideShowImageProgress.setVisibility(ProgressBar.GONE);
            mSlideShowImageView.setVisibility(ImageView.VISIBLE);
        }

        // Download image
        else if (mBitmapUrl != null) {
            downloadImage(mBitmapUrl);
        }

        return inflatedView;
    }

    @Override
    public void onDestroy() {

        // Cancel the running tasks
        if ((mDownloadImageTask != null) && (mDownloadImageTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mDownloadImageTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public void onDownloadImageTaskResult(DownloadImageTask.Result result) {
        mSlideShowImageView.setImageBitmap(result.bitmap);
        mSlideShowImageProgress.setVisibility(ProgressBar.GONE);
        mSlideShowImageView.setVisibility(ImageView.VISIBLE);

        // Cache image in memory
        mListener.addBitmapToMemoryCache(mBitmapUrl, result.bitmap);

    }

    @Override
    public void onDownloadImageTaskException(Exception e) {

        // Alert toast
        new InfoAlertToast(getActivity(), InfoAlertToast.TOAST_TYPE_ALERT, e.getMessage()).show();

    }

    private void downloadImage(String imageUrl) {

        // Setup DownloadImageTask input parameters
        DownloadImageTask.Param param = new DownloadImageTask.Param(this, imageUrl, 0, mLayoutWidth, mLayoutHeight);

        // Execute the DownloadImageTask
        mDownloadImageTask = (DownloadImageTask) new DownloadImageTask().execute(
                new DownloadImageTask.Param[]{param});

    }

}
