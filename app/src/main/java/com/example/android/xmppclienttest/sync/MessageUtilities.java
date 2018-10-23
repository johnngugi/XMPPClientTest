package com.example.android.xmppclienttest.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.xmppclienttest.util.CustomConnection;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MessageUtilities {

    private static final int REMINDER_INTERVAL_MINUTES = 2;
    private static final int REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));
    private static final int SYNC_FLEXTIME_SECONDS = REMINDER_INTERVAL_SECONDS;

    private static final String MESSAGE_JOB_TAG = "check_new_messages_tag";

    private static boolean sInitialised;

    synchronized public static void scheduleRetrieveNewMessages(@NonNull final Context context) {
        if (sInitialised) return;

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job constraintNewMessagesJob = dispatcher.newJobBuilder()
                /* The Service that will be used to write to preferences */
                .setService(NewMessagesFirebaseJobService.class)
                .setTag(MESSAGE_JOB_TAG)
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        REMINDER_INTERVAL_MINUTES,
                        REMINDER_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintNewMessagesJob);

        sInitialised = true;
    }

    synchronized public static void fetchNewEvent(Context context, String hostAddress) {
        CustomConnection connection = CustomConnection.getInstance(context, hostAddress);
        try {
            connection.connect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
