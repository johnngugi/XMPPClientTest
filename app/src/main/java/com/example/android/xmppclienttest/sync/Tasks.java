package com.example.android.xmppclienttest.sync;

import android.content.Context;

import com.example.android.xmppclienttest.AppExecutors;
import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.util.PreferenceUtilities;

class Tasks {

    private static final String TAG = Tasks.class.getSimpleName();

    static final String ACTION_FETCH_EVENT = "fetch-event";
    private static AppDatabase db;

    static void executeTask(Context context, String action) {
        if (ACTION_FETCH_EVENT.equals(action)) {
            MessageUtilities.fetchNewEvent(context, PreferenceUtilities.getSavedHostAddress(context));
        }
    }

    static void addEvent(Context context, final MessageEntry messageEntry) {
        db = AppDatabase.getInstance(context);
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.messageDao().insertSingleMessage(messageEntry);
            }
        });
    }
}
