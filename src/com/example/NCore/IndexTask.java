package com.example.NCore;

import android.os.AsyncTask;
import android.util.Log;

public class IndexTask extends AsyncTask<IndexTask.IndexTaskListener,Void,Void> {

    private IndexTaskListener mExecutor;

    public interface IndexTaskListener {
        public void onIndexTaskResult(Void aVoid);
    }

    @Override
    protected Void doInBackground(IndexTaskListener... listeners) {

        // Get the executor activity of the task
        mExecutor = listeners[0];

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mExecutor.onIndexTaskResult(aVoid);
    }

    @Override
    protected void onCancelled(Void aVoid) {
        Log.d("onCancelled", "Void");
    }

}
