package com.example.android.xmppclienttest.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.android.xmppclienttest.ApplicationContextProvider;
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
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jxmpp.jid.parts.Resourcepart;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CustomConnection implements ConnectionListener {

    private static final String TAG = CustomConnection.class.getSimpleName();
    private static final String DEFAULT_USER_RESOURCE = "strathmore-student";

    private static CustomConnection sInstance;
    private static XMPPTCPConnection connection;

    private InetAddress mHostAddress;
    private String mUsername;
    private String mPassword;
    private String mServiceName;

    private String mUserJidResource;

    public enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED
    }

    public enum LoggedInState {
        LOGGED_IN, LOGGED_OUT
    }

    public static CustomConnection getInstance(Context context, String hostAddress) {
        if (sInstance == null) {
            sInstance = new CustomConnection(context, hostAddress);
        }
        return sInstance;
    }

    private CustomConnection(Context context, String hostAddress) {
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
        XMPPTCPConnectionConfiguration.Builder configuration = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(mUsername, mPassword)
                .setXmppDomain("strathmore-computer")
                .setHostAddress(mHostAddress)
                .setPort(5222)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        // Check if user resource is set and use that to connect
        // If not connect and save the new user resource
        if (checkResourceNameIsSet()) {
            configuration.setResource(mUserJidResource);
            connectAndLogin(configuration);
        } else {
            connectAndLogin(configuration);
            setUserResourceName();
        }

        ReconnectionManager.setEnabledPerDefault(true);
        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        reconnectionManager.enableAutomaticReconnection();

        setUserResourceName();

        subscribe();
    }

    private void connectAndLogin(XMPPTCPConnectionConfiguration.Builder configuration)
            throws SmackException, IOException, XMPPException, InterruptedException {
        connection = new XMPPTCPConnection(configuration.build());
        connection.addConnectionListener(this);
        connection.connect();
        connection.login();
    }

    private void setUserResourceName() {
        Context context = ApplicationContextProvider.getContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                "com.example.android.xmppclienttest", Context.MODE_PRIVATE);

        Resourcepart resourcepart = connection.getUser().getResourceOrNull();

        if (resourcepart != null) {
            mUserJidResource = resourcepart.toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("user_resource_jid", mUserJidResource);
            editor.apply();
        }
    }

    private boolean checkResourceNameIsSet() {
        Context context = ApplicationContextProvider.getContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                "com.example.android.xmppclienttest", Context.MODE_PRIVATE);

        String userResourceName = sharedPref.getString(
                "user_resource_jid", DEFAULT_USER_RESOURCE);

        boolean userResourceIsSet = !DEFAULT_USER_RESOURCE.equals(userResourceName);

        if (userResourceIsSet) {
            mUserJidResource = userResourceName;
        }

        return userResourceIsSet;
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + mServiceName);
        if (connection != null) {
            connection.disconnect();
        }
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        connection = null;
    }

    private void subscribe() {
        Log.d(TAG, "subscribe()");
        try {
            if (connection != null) {
                PubSubManager pubSubManager = PubSubManager.getInstance(connection);
                LeafNode eventNode = pubSubManager.getNode("testNode");
                eventNode.addItemEventListener(new PublishItemEventListener());
                eventNode.subscribe(String.valueOf(connection.getUser()));
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
                    Tasks.addEvent(ApplicationContextProvider.getContext(), messageEntry);
                    System.out.println("Parsed message: " + messageEntry.getBody());
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
