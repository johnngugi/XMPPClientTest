package com.example.android.xmppclienttest.util;

import android.content.Context;
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
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jxmpp.jid.parts.Localpart;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CustomConnection implements ConnectionListener {

    private static final String DEFAULT_USER_NAME = "student1";

    private static final String TAG = CustomConnection.class.getSimpleName();
    private static final String DEFAULT_USER_RESOURCE = "strathmore-student";
    private static final String DEFAULT_USER_SUBSCRIPTION = "important";

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static String uniqueID = null;

    private static CustomConnection sInstance;
    private static XMPPTCPConnection connection;
    private static String DEFAULT_REMOTE_HOST_ADDRESS = "10.50.4.141";

    private InetAddress mHostAddress;
    private String mUsername;
    private String mPassword;
    private String mServiceName;

    private String mUserJidResource;

    private XMPPTCPConnectionConfiguration.Builder connectionConfiguration;

    public static String getDefaultRemoteHostAddress() {
        return DEFAULT_REMOTE_HOST_ADDRESS;
    }

    public static String getDefaultUserName() {
        return DEFAULT_USER_NAME;
    }

    public static String getDefaultUserSubscription() {
        return DEFAULT_USER_SUBSCRIPTION;
    }

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
        Context context = ApplicationContextProvider.getContext();

        boolean accountCreated = PreferenceUtilities.checkAccountCreated(context);

        // TODO: Remove debug when testing is finished
        SmackConfiguration.DEBUG = true;
        connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain("strathmore-computer")
                .setHostAddress(mHostAddress)
                .setPort(5222)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        if (!accountCreated) {
            connectionConfiguration.setUsernameAndPassword(DEFAULT_USER_NAME, mPassword);
            createAccount(connectionConfiguration, context);
        } else {
            boolean isUserNameSet = checkAndSetUsername(context);
            if (checkAndSetResourceName(context) && isUserNameSet) {
                connectionConfiguration.setUsernameAndPassword(mUsername, mPassword);
                connectionConfiguration.setResource(mUserJidResource);
                connectAndLogin(connectionConfiguration);
            } else {
                mUserJidResource = PreferenceUtilities.setUserResourceName(context, connection);
                connectionConfiguration.setUsernameAndPassword(mUsername, mPassword);
                connectionConfiguration.setResource(mUserJidResource);
                connectAndLogin(connectionConfiguration);
            }
        }
    }

    private boolean checkAndSetUsername(Context context) {
        boolean isUserNameSet = PreferenceUtilities.checkUserNameIsSet(context);
        if (isUserNameSet) {
            mUsername = PreferenceUtilities.getUniqueId(context);
        }
        return isUserNameSet;
    }

    private void createAccount(XMPPTCPConnectionConfiguration.Builder configuration, Context context)
            throws InterruptedException, XMPPException, SmackException, IOException {
        connectAndLogin(configuration);
        uniqueID = PreferenceUtilities.getUniqueId(context);
        mUsername = uniqueID;
        mUserJidResource = PreferenceUtilities.setUserResourceName(context, connection);

        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        accountManager.createAccount(Localpart.from(mUsername), mPassword);

        disconnectThenAttemptReconnect();
    }

    private void disconnectThenAttemptReconnect() {
        Log.d(TAG, "Disconnecting from server, attempting reconnect " + mServiceName);
        disconnect();
        try {
            connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(mUsername, mPassword)
                    .setXmppDomain("strathmore-computer")
                    .setHostAddress(mHostAddress)
                    .setPort(5222)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setResource(mUserJidResource);
            connectAndLogin(connectionConfiguration);
        } catch (InterruptedException | XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        }
    }

    private void connectAndLogin(XMPPTCPConnectionConfiguration.Builder configuration)
            throws SmackException, IOException, XMPPException, InterruptedException {
        connection = new XMPPTCPConnection(configuration.build());
        connection.addConnectionListener(this);

        try {
            connection.connect();
        } catch (SmackException.ConnectionException e) {
            e.getFailedAddresses();
        }
        connection.login();

        ReconnectionManager.setEnabledPerDefault(true);
        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        reconnectionManager.enableAutomaticReconnection();

        subscribe();
    }

    private boolean checkAndSetResourceName(Context context) {
        String userResourceName = PreferenceUtilities.getResourceName(context);
        boolean isUserResourceSet = !DEFAULT_USER_NAME.equals(userResourceName);

        if (isUserResourceSet) {
            mUserJidResource = userResourceName;
        }

        return isUserResourceSet;
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
        Context context = ApplicationContextProvider.getContext();
        try {
            if (connection != null) {
                PublishItemEventListener eventListener = new PublishItemEventListener();
                PubSubManager pubSubManager = PubSubManager.getInstance(connection);
                LeafNode eventNode = pubSubManager.getNode("testNode");
                eventNode.addItemEventListener(eventListener);
                String user = connection.getUser().getLocalpart().asUnescapedString();

                if (!PreferenceUtilities.checkSubIdIsSet(context)) {
                    System.out.println("Sub id not set");
                    if (!DEFAULT_USER_NAME.equals(user)) {
                        Subscription subscription =
                                eventNode.subscribe(String.valueOf(connection.getUser()));
                        PreferenceUtilities.saveSubscriptionId(subscription.getId());
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
                    MessageEntry messageEntry = parser.parseContent(payloadMessage);
                    Tasks.addEvent(ApplicationContextProvider.getContext(), messageEntry);
                    System.out.println("Parsed message: " + messageEntry.getBody());
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
