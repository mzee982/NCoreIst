package com.mzee982.android.ncoreist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class DownloadImageTask extends NCoreAsyncTask<DownloadImageTask.DownloadImageTaskListener,Void>  {

    // Exceptions
    private static final String EXCEPTION_GET_IMAGE = "Cannot get the image. HTTP response code: %";

    // Members
    private int mImageId;

    /**
     * Interface to communicate with the task executor activity
     */
    public interface DownloadImageTaskListener {
        public void onDownloadImageTaskResult(Result result);
        public void onDownloadImageTaskException(Exception e);
    }

    /**
     * Param object of the task
     */
    public static class Param extends AbstractParam<DownloadImageTaskListener> {
        public final String imageUrl;
        public final int imageId;
        public final int imageWidth;
        public final int imageHeight;

        public Param(DownloadImageTaskListener executor, String imageUrl, int imageId, int imageWidth, int imageHeight) {
            super(executor);

            this.imageUrl = imageUrl;
            this.imageId = imageId;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }

    }

    /**
     * Result object of the task
     */
    public class Result extends AbstractResult {
        public final int imageId;
        public final Bitmap bitmap;

        public Result(int imageId, Bitmap bitmap) {
            this.imageId = imageId;
            this.bitmap = bitmap;
        }

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
    protected AbstractResult doInBackground(AbstractParam... params) {
        HttpURLConnection downloadImageConnection = null;
        InputStream imageInputStream = null;
        int responseCode = 0;
        Bitmap bitmap = null;

        super.doInBackground(params);

        // Get input parameters
        Param param = (Param) params[0];

        // Extract input parameters
        String imageUrl = param.imageUrl;
        mImageId = param.imageId;
        int imageWidth = param.imageWidth;
        int imageHeight = param.imageHeight;

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
                    return new Result(mImageId, bitmap);
                }

                else {
                    return null;
                }

            } catch (MalformedURLException e) {
                mException = e;
                e.printStackTrace();
            } catch (IOException e) {
                mException = e;
                e.printStackTrace();
            } finally {
                if (imageInputStream != null) imageInputStream.close();
                if (downloadImageConnection != null) downloadImageConnection.disconnect();
            }
        } catch (IOException e) {
            mException = e;
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onTaskResult(AbstractResult result) {
        mExecutor.onDownloadImageTaskResult((Result) result);
    }

    @Override
    protected void onTaskException(Exception e) {
        mExecutor.onDownloadImageTaskException(e);
    }

}
