//package com.example.android.xmppclienttest.sync;
//
//import android.content.Context;
//import android.os.AsyncTask;
//
//import com.example.android.xmppclienttest.util.CustomConnection;
//import com.firebase.jobdispatcher.JobParameters;
//import com.firebase.jobdispatcher.JobService;
//
//import org.jivesoftware.smack.tcp.XMPPTCPConnection;
//
//public class EventsFirebaseJobService extends JobService {
//
//    private AsyncTask<XMPPTCPConnection, Void, Void> mFetchEventsTask;
//
//    @Override
//    public boolean onStartJob(final JobParameters job) {
//
//        mFetchEventsTask = new AsyncTask<XMPPTCPConnection, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(XMPPTCPConnection... customConnections) {
//                Context context = getApplicationContext();
//                Tasks.getEvent(context, customConnections[0]);
//                jobFinished(job, false);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                jobFinished(job, false);
//            }
//        };
//
//        mFetchEventsTask.execute(CustomConnection.connection);
//        return true;
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters job) {
//        if (mFetchEventsTask != null) {
//            mFetchEventsTask.cancel(true);
//        }
//        return true;
//    }
//}
