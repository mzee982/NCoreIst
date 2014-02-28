package com.example.NCore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Map;

public class DownloadImageTask extends AsyncTask<Map<String,Object>,Void,Bitmap>  {

    // Exceptions
    private static final String EXCEPTION_GET_IMAGE = "Cannot get the image. HTTP response code: %";

    // Parameters
    public static final String PARAM_IN_EXECUTOR = "PARAM_IN_EXECUTOR";
    public static final String PARAM_IN_IMAGE_URL = "PARAM_IN_IMAGE_URL";
    public static final String PARAM_IN_IMAGE_ID = "PARAM_IN_IMAGE_ID";
    public static final String PARAM_IN_IMAGE_WIDTH = "PARAM_IN_IMAGE_WIDTH";
    public static final String PARAM_IN_IMAGE_HEIGHT = "PARAM_IN_IMAGE_HEIGHT";

    // Members
    private DownloadImageTaskListener mExecutor;
    private int mImageId;

    /**
     * Interface to communicate with the task executor activity
     */
    public interface DownloadImageTaskListener {
        public void onDownloadImageTaskResult(int aImageId, Bitmap aBitmap);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2
            // *** and keeps both height and width larger than the requested height and width. ***
            while ((halfHeight / inSampleSize) > reqHeight || (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    protected Bitmap doInBackground(Map<String,Object>... maps) {
        HttpURLConnection downloadImageConnection = null;
        InputStream imageInputStream = null;
        int responseCode = 0;
        Bitmap bitmap = null;

        // Get input parameters
        Map<String,Object> inputParams = maps[0];

        // Get the executor activity of the task
        mExecutor = (DownloadImageTaskListener) inputParams.get(PARAM_IN_EXECUTOR);

        // Extract input parameters
        String imageUrl = (String) inputParams.get(PARAM_IN_IMAGE_URL);
        mImageId = (Integer) inputParams.get(PARAM_IN_IMAGE_ID);
        int imageWidth =
                inputParams.get(PARAM_IN_IMAGE_WIDTH) == null ? 0 : (Integer) inputParams.get(PARAM_IN_IMAGE_WIDTH);
        int imageHeight =
                inputParams.get(PARAM_IN_IMAGE_HEIGHT) == null ? 0 : (Integer) inputParams.get(PARAM_IN_IMAGE_HEIGHT);

        //
        try {
            try {

                /*
                 * Get the image
                 */

                if (!isCancelled()) {

                    // Connection
                    downloadImageConnection = NCoreConnectionManager.openGetConnection(imageUrl);
                    downloadImageConnection.connect();

                    // Response
                    responseCode = downloadImageConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        imageInputStream = new BufferedInputStream(downloadImageConnection.getInputStream());
                    }
                    else {
                        throw new IOException(EXCEPTION_GET_IMAGE.replace("%", String.valueOf(responseCode)));
                    }

                }

                /*
                 * Download the image
                 */

                if (!isCancelled() && (imageInputStream != null)) {

                    // Input stream to byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read = 0;
                    while ((read = imageInputStream.read(buffer, 0, buffer.length)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    baos.flush();
                    byte[] imageByteArray = baos.toByteArray();

                    //
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    // Downsampling if necessary
                    if ((imageWidth > 0) && (imageHeight > 0)) {

                        // Decode bounds
                        bitmapOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length, bitmapOptions);

                        // Calculate sample size
                        bitmapOptions.inSampleSize = calculateInSampleSize(bitmapOptions, imageWidth, imageHeight);

                    }

                    // Decode image
                    bitmapOptions.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length, bitmapOptions);

                }

                else if (imageInputStream == null) {
                    throw new IOException(EXCEPTION_GET_IMAGE.replace("%", String.valueOf(responseCode)));
                }


                /*
                 * Results
                 */

                if (!isCancelled() && (bitmap != null)) {
                    return bitmap;
                }

                else {
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (imageInputStream != null) imageInputStream.close();
                if (downloadImageConnection != null) downloadImageConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap aBitmap) {
        mExecutor.onDownloadImageTaskResult(mImageId, aBitmap);
    }

    @Override
    protected void onCancelled(Bitmap aBitmap) {
        Log.d("onCancelled", "");
    }

}
