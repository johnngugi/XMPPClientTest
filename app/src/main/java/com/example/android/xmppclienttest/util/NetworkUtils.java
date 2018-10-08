package com.example.android.xmppclienttest.util;

import android.content.Context;
import android.util.Log;

import com.example.android.xmppclienttest.database.MessageEntry;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubException;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

import java.util.ArrayList;
import java.util.List;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static List<MessageEntry> retrievePublished(Context context,
                                                       XMPPTCPConnection connection) {
        final List<MessageEntry> events = new ArrayList<>();
        // Create a pubsub manager using an existing XMPPConnection
        PubSubManager pubSubManager = PubSubManager.getInstance(connection);

        LeafNode eventNode = null;
        try {
            eventNode = pubSubManager.getNode("testNode");
            eventNode.addItemEventListener(new ItemEventListener() {
                @Override
                public void handlePublishedItems(ItemPublishEvent items) {
                    Log.d(TAG, "event published");
                    for (Object obj : items.getItems()) {
                        PayloadItem item = (PayloadItem) obj;
                        String payloadTitle = item.getId();
                        String payloadMessage = item.getPayload().toString();
                        events.add(new MessageEntry(payloadTitle, payloadMessage));
                    }
                }
            });
            List<Subscription> subscriptions = eventNode.getSubscriptions();
            if (subscriptions == null) {
                eventNode.subscribe(String.valueOf(connection.getUser()));
            }
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException |
                SmackException.NotConnectedException | InterruptedException |
                PubSubException.NotAPubSubNodeException e) {
            e.printStackTrace();
        }
        return events;
    }

}
