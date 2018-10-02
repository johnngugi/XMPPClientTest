package com.example.android.xmppclienttest.sync;

import android.content.Context;

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

    public static void executeTask(Context context, String action) {
        if (ACTION_NEW_EVENT.equals(action)) {
            alertNewEvent(context);
        }
    }

    private static void alertNewEvent(Context context) {
        NotificationUtils.alertUserAboutNewEvent(context);
    }

    public static void getEvent(Context context, XMPPTCPConnection connection) {
        final AppDatabase db = AppDatabase.getInstance(context);
        List<MessageEntry> events = NetworkUtils.retrievePublished(context, connection);

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
