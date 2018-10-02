package com.example.android.xmppclienttest.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

public class EventsFirebaseJobService extends JobService {

    private AsyncTask<XMPPTCPConnection, Void, Void> mFetchEventsTask;

    @Override
    public boolean onStartJob(final JobParameters job) {
        final XMPPTCPConnection connection = (XMPPTCPConnection) job.getExtras().get("Connection");

        mFetchEventsTask = new AsyncTask<XMPPTCPConnection, Void, Void>() {

            @Override
            protected Void doInBackground(XMPPTCPConnection... xmpptcpConnections) {
                Context context = getApplicationContext();
                Tasks.getEvent(context, xmpptcpConnections[0]);
                jobFinished(job, false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job, false);
            }
        };

        mFetchEventsTask.execute(connection);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mFetchEventsTask != null) {
            mFetchEventsTask.cancel(true);
        }
        return true;
    }
}
