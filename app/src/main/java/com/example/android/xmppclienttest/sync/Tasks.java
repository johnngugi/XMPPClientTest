package com.example.android.xmppclienttest.sync;

import android.content.Context;

import com.example.android.xmppclienttest.AppExecutors;
import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.util.NotificationUtils;
import com.example.android.xmppclienttest.util.PreferenceUtilities;

public class Tasks {
    public static final String ACTION_NEW_EVENT = "new-event";
    public static final String ACTION_FETCH_EVENT = "fetch-event";
    private static final String TAG = Tasks.class.getSimpleName();
    private static AppDatabase db;

    public static void executeTask(Context context, String action) {
        if (ACTION_FETCH_EVENT.equals(action)) {
            MessageUtilities.fetchNewEvent(context, PreferenceUtilities.getSavedHostAddress(context));
        }
    }

    public static void addEvent(Context context, final MessageEntry messageEntry) {
        db = AppDatabase.getInstance(context);
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.messageDao().insertSingleMessage(messageEntry);
            }
        });
        NotificationUtils.alertUserAboutNewEvent(context);
    }
}
