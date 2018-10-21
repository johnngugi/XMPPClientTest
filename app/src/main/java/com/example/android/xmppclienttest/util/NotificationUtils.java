package com.example.android.xmppclienttest.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.android.xmppclienttest.MainActivity;
import com.example.android.xmppclienttest.R;

public class NotificationUtils {

    private static final int NEW_EVENT_NOTIFICATION_ID = 1138;
    private static final int NEW_EVENT_PENDING_INTENT_ID = 3417;

    private static final String NEW_EVENT_NOTIFICATION_CHANNEL_ID = "event_notification_channel";

    private static final String TAG = NotificationUtils.class.getSimpleName();

    public static void alertUserAboutNewEvent(Context context) {
        Log.d(TAG, "Notification called");
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    NEW_EVENT_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context, NEW_EVENT_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_phonelink_ring)
                .setContentTitle(context.getString(R.string.new_event_notification_title))
                .setContentText(context.getString(R.string.new_event_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.new_event_notification_body)))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(NEW_EVENT_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                NEW_EVENT_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
