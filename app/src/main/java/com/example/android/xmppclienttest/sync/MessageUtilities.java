package com.example.android.xmppclienttest.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.util.CustomConnection;
import com.example.android.xmppclienttest.util.MessageParser;
import com.example.android.xmppclienttest.util.NotificationUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageUtilities {

    private static final int REMINDER_INTERVAL_MINUTES = 1;
    private static final int REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));
    private static final int SYNC_FLEXTIME_SECONDS = REMINDER_INTERVAL_SECONDS;

    private static final String MESSAGE_JOB_TAG = "check_new_messages_tag";
    private static final String TAG = MessageUtilities.class.getSimpleName();

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
                        REMINDER_INTERVAL_SECONDS,
                        REMINDER_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintNewMessagesJob);

        sInitialised = true;
    }

    synchronized static void fetchNewEvent(Context context, String hostAddress) {
        MessageParser parser = new MessageParser();
        CustomConnection connection = CustomConnection.getInstance(hostAddress);
        try {
            Log.d(TAG, "Background service connecting");
            connection.connect();
            connection.subscribe(null);
            LeafNode node = connection.getNode();
            List<PayloadItem> items = node.getItems(2);
            if (items.size() > 0) {
                for (PayloadItem item : items) {
                    String payloadMessage = parser.retreiveXmlString(item.getPayload().toString());
                    MessageEntry messageEntry = parser.parseContent(payloadMessage);
                    Tasks.addEvent(context, messageEntry);
                    NotificationUtils.alertUserAboutNewEvent(context);
                }
            }
            Log.d(TAG, "Background service disconnecting");
            connection.disconnect();
        } catch (SmackException.ConnectionException e) {
            Log.e(TAG, "Server not found()");
            e.printStackTrace();
        } catch (SASLErrorException e) {
            Log.d(TAG, "Make sure the credentials are right and try again");
            e.printStackTrace();
        } catch (IOException | InterruptedException | XMPPException | SmackException e) {
            Log.d(TAG, "Something went wrong while connecting");
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.d(TAG, "Something went wrong while parsing the receive message");
            e.printStackTrace();
        }
    }
}
