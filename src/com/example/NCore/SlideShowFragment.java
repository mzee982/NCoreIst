package com.example.NCore;

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

import java.util.HashMap;

public class SlideShowFragment extends Fragment implements DownloadImageTask.DownloadImageTaskListener {

    // Arguments
    public static final String ARGUMENT_IN_BITMAP = "ARGUMENT_IN_BITMAP";
    public static final String ARGUMENT_IN_BITMAP_URL = "ARGUMENT_IN_BITMAP_URL";
    public static final String ARGUMENT_IN_BITMAP_WIDTH = "ARGUMENT_IN_BITMAP_WIDTH";
    public static final String ARGUMENT_IN_BITMAP_HEIGHT = "ARGUMENT_IN_BITMAP_HEIGHT";

    // Members
    private Bitmap mBitmap;
    private String mBitmapUrl;
    private ImageView mSlideShowImageView;
    private ProgressBar mSlideShowImageProgress;
    private DownloadImageTask mDownloadImageTask;
    private int mLayoutWidth;
    private int mLayoutHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get input arguments
        mBitmap = (Bitmap) getArguments().getParcelable(ARGUMENT_IN_BITMAP);
        mBitmapUrl = getArguments().getString(ARGUMENT_IN_BITMAP_URL);
        mLayoutWidth = getArguments().getInt(ARGUMENT_IN_BITMAP_WIDTH, 0);
        mLayoutHeight = getArguments().getInt(ARGUMENT_IN_BITMAP_HEIGHT, 0);

        // Setup layout
        final View inflatedView = inflater.inflate(R.layout.slideshow_fragment, container, false);

        mSlideShowImageView = (ImageView) inflatedView.findViewById(R.id.SlideShowImageView);
        mSlideShowImageProgress = (ProgressBar) inflatedView.findViewById(R.id.SlideShowImageProgess);
        LinearLayout slideShowLayout = (LinearLayout) getActivity().findViewById(R.id.SlideShowLayout);

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
        if ((mDownloadImageTask != null) && (mDownloadImageTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mDownloadImageTask.cancel(true);
        }

        // Release
        mDownloadImageTask = null;

        super.onDestroy();
    }

    @Override
    public void onDownloadImageTaskResult(int aImageId, Bitmap aBitmap) {
        mSlideShowImageView.setImageBitmap(aBitmap);
        mSlideShowImageProgress.setVisibility(ProgressBar.GONE);
        mSlideShowImageView.setVisibility(ImageView.VISIBLE);
    }

    private void downloadImage(String imageUrl) {

        // Setup DownloadImageTask input parameters
        HashMap<String,Object> inputParams = new HashMap<String,Object>();

        inputParams.put(DownloadImageTask.PARAM_IN_EXECUTOR, this);
        inputParams.put(DownloadImageTask.PARAM_IN_IMAGE_URL, imageUrl);
        inputParams.put(DownloadImageTask.PARAM_IN_IMAGE_ID, 0);
        inputParams.put(DownloadImageTask.PARAM_IN_IMAGE_WIDTH, mLayoutWidth);
        inputParams.put(DownloadImageTask.PARAM_IN_IMAGE_HEIGHT, mLayoutHeight);

        HashMap<String,Object>[] inputParamsArray = new HashMap[1];
        inputParamsArray[0] = inputParams;

        // Execute the DownloadImageTask
        mDownloadImageTask = (DownloadImageTask) new DownloadImageTask().execute(inputParamsArray);

    }

}
