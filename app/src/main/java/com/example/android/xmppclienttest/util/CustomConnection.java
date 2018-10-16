package com.example.android.xmppclienttest.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.sync.ConnectionService;
import com.example.android.xmppclienttest.sync.Tasks;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class CustomConnection implements ConnectionListener {

    private static final String TAG = CustomConnection.class.getSimpleName();
    private InetAddress mHostAddress;
    public static XMPPTCPConnection connection;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private String mResourceName;

    private BroadcastReceiver uiThreadMessageReceiver;
    private Context mApplicationContext;

    public enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED
    }

    public enum LoggedInState {
        LOGGED_IN, LOGGED_OUT
    }

    public CustomConnection(Context context, String hostAddress) {
        mApplicationContext = context.getApplicationContext();
        mResourceName = "Strathmore-broadcast";
        // TODO (1) Change this, use the constructor instead
        mUsername = "student1";
        mPassword = "password";
        mServiceName = "Testing";
        try {
            mHostAddress = InetAddress.getByName(hostAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e(TAG, "error getting host address", e);
        }
    }

    public void connect() throws InterruptedException, XMPPException, SmackException, IOException {
        Log.d(TAG, "Connecting to server " + mServiceName);
        // TODO: Remove debug when testing is finished
        SmackConfiguration.DEBUG = true;
        XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(mUsername, mPassword)
                .setXmppDomain("strathmore-computer")
                .setHostAddress(mHostAddress)
                .setPort(5222)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        //Set up the ui thread broadcast message receiver.
//        setupUiThreadBroadCastMessageReceiver();

        connection = new XMPPTCPConnection(configuration);
        connection.addConnectionListener(this);
        connection.connect();
        connection.login();

        subscribe();

        ReconnectionManager.setEnabledPerDefault(true);
        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        reconnectionManager.enableAutomaticReconnection();
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + mServiceName);
        if (connection != null) {
            connection.disconnect();
        }
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        connection = null;
    }

    private void setupUiThreadBroadCastMessageReceiver() {
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if (action.equals(ConnectionService.SEND_EVENT)) {
                    //SENDS THE ACTUAL MESSAGE TO THE SERVER
                    sendMessage(intent.getStringExtra(ConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(ConnectionService.BUNDLE_TO));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectionService.SEND_EVENT);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver, filter);

    }

    private void sendMessage(String stringExtra, String stringExtra1) {

    }

    private void subscribe() {
        Log.d(TAG, "subscribe()");
        try {
            if (connection != null) {
                PubSubManager pubSubManager = PubSubManager.getInstance(connection);
                LeafNode eventNode = pubSubManager.getNode("testNode");
                eventNode.addItemEventListener(new PublishItemEventListener());
                List<Subscription> subscriptions = eventNode.getSubscriptions();
                if (subscriptions.size() == 0) {
                    eventNode.subscribe(String.valueOf(connection.getUser()));
                } else {
                    for (Subscription subscription : subscriptions) {
                        System.out.println(subscription.toXML(""));
                        Jid jid = subscription.getJid();
                        EntityFullJid userJid = connection.getUser();
                        Subscription.State state = subscription.getState();
                        if (userJid == jid) {
                            if (state != Subscription.State.subscribed) {
                                eventNode.subscribe(String.valueOf(userJid));
                            }
                        } else {
                            eventNode.subscribe(String.valueOf(userJid));
                        }
                    }
                }
            }
        } catch (InterruptedException | XMPPException | SmackException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(XMPPConnection connection) {
        ConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "Connected Successfully");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        ConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "Authenticated Successfully");
    }

    @Override
    public void connectionClosed() {
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "Connection closed()");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "ConnectionClosedOnError, error " + e.toString());
    }

    public XMPPTCPConnection getXmppTcpConnection() {
        return connection;
    }

    private class PublishItemEventListener implements ItemEventListener {
        MessageParser parser = new MessageParser();

        @Override
        public void handlePublishedItems(ItemPublishEvent items) {
            Log.d(TAG, "event published");
            for (Object obj : items.getItems()) {
                PayloadItem item = (PayloadItem) obj;
                String payloadMessage = parser.retreiveXmlString(item.getPayload().toString());
                System.out.println("Payload message: " + payloadMessage);
                try {
                    MessageEntry messageEntry = parser.parseTest(payloadMessage);
                    Tasks.addEvent(mApplicationContext, messageEntry);
                    System.out.println("Parsed message: " + messageEntry.getBody());
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
