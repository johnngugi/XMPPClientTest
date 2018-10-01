package com.example.android.xmppclienttest.sync;

import android.content.Context;

import com.example.android.xmppclienttest.util.NotificationUtils;

public class Tasks {
    public static final String ACTION_NEW_EVENT = "new-event";

    public static void executeTask(Context context, String action) {
        if (ACTION_NEW_EVENT.equals(action)) {
            alertNewEvent(context);
        }
    }

    private static void alertNewEvent(Context context) {
        NotificationUtils.alertUserAboutNewEvent(context);
    }
}
