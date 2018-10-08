package com.example.android.xmppclienttest.sync;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;

import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.util.CustomConnection;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventSyncUtils {

    private static final String SYNC_TAG = "event-sync";

    private static final int SYNC_INTERVAL_MINUTES = 2;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.MINUTES.toSeconds(SYNC_INTERVAL_MINUTES);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 2;
    private static boolean sInitialized;

    static void scheduleEventJobDispatcherSchedule(Context context, CustomConnection connection) {
        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

        Job syncJob = firebaseJobDispatcher.newJobBuilder()
                .setService(EventsFirebaseJobService.class)
                .setTag(SYNC_TAG)
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        firebaseJobDispatcher.schedule(syncJob);
    }

    synchronized public static void initialize(final Context context, CustomConnection connection) {
        if (sInitialized) return;

        sInitialized = true;

        scheduleEventJobDispatcherSchedule(context, connection);

        final AppDatabase db = AppDatabase.getInstance(context);

        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                LiveData<List<MessageEntry>> entries = db.messageDao().loadAllMessages();
                List<MessageEntry> messageEntries = entries.getValue();
                if (messageEntries == null || messageEntries.size() == 0) {
                    startImmediateSync(context);
                }
            }
        });
        checkForEmpty.start();
    }

    public static void startImmediateSync(Context context) {
        Intent intent = new Intent(context, NewEventIntentService.class);
        intent.setAction(Tasks.ACTION_NEW_EVENT);
        context.startService(intent);
    }

}
