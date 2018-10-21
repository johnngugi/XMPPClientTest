package com.example.android.xmppclienttest.sync;

import android.content.Context;
import android.util.Log;

import com.example.android.xmppclienttest.AppExecutors;
import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.util.NetworkUtils;
import com.example.android.xmppclienttest.util.NotificationUtils;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.List;

public class Tasks {
    public static final String ACTION_NEW_EVENT = "new-event";
    public static final String ACTION_FETCH_EVENT = "fetch-event";
    private static final String TAG = Tasks.class.getSimpleName();
    private static AppDatabase db;

    public static void executeTask(Context context, String action) {
        if (ACTION_NEW_EVENT.equals(action)) {
            alertNewEvent(context);
        }
    }

    private static void alertNewEvent(Context context) {
        NotificationUtils.alertUserAboutNewEvent(context);
    }

    public static void addEvent(Context context, final MessageEntry messageEntry) {
        db = AppDatabase.getInstance(context);
        AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.messageDao().insertSingleMessage(messageEntry);
            }
        });
        NotificationUtils.alertUserAboutNewEvent(context);
    }

    public static void getEvent(Context context, XMPPTCPConnection connection) {
        Log.d(TAG, "Connection: " + connection.isConnected());
        List<MessageEntry> events = NetworkUtils.retrievePublished(context, connection);
        Log.d(TAG, "" + events.size());

        for (final MessageEntry entry : events) {
            AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    db.messageDao().insertSingleMessage(entry);
                }
            });
        }
    }
}
