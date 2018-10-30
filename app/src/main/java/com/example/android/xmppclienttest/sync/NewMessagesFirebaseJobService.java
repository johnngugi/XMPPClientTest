package com.example.android.xmppclienttest.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class NewMessagesFirebaseJobService extends JobService {
    private AsyncTask mBackgroundTask;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mBackgroundTask = new BackgroundAsyncTask().execute(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }

    private class BackgroundAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> {
        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            Context context = NewMessagesFirebaseJobService.this;
            Tasks.executeTask(context, Tasks.ACTION_FETCH_EVENT);
            return jobParameters[0];
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            jobFinished(jobParameters, false);
        }
    }
}
