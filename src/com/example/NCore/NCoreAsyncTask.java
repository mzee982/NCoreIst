package com.example.NCore;

import android.os.AsyncTask;

public abstract class NCoreAsyncTask<TaskListener,Progress> extends
        AsyncTask<NCoreAsyncTask.AbstractParam,Progress,NCoreAsyncTask.AbstractResult> {

    // Members
    protected TaskListener mExecutor;
    protected Exception mException;

    /**
     * Param object of the task
     */
    public static abstract class AbstractParam<TaskListener> {
        public final TaskListener executor;

        public AbstractParam(TaskListener executor) {
            this.executor = executor;
        }

    }

    /**
     * Result object of the task
     */
    public static abstract class AbstractResult {
        public AbstractResult() {}
    }

    @Override
    protected AbstractResult doInBackground(AbstractParam... params) {

        // Get input parameters
        AbstractParam<TaskListener> param = params[0];

        // Get the executor activity of the task
        mExecutor = param.executor;

        //
        return null;

    }

    @Override
    protected void onPostExecute(AbstractResult result) {

        // Success
        if (mException == null)
            onTaskResult(result);

        // Failure
        else
            onTaskException(mException);

    }

    protected abstract void onTaskResult(AbstractResult result);

    protected abstract void onTaskException(Exception e);

}
