package com.example.android.xmppclienttest.util;

import android.content.Context;
import android.util.Log;

import com.example.android.xmppclienttest.ApplicationContextProvider;
import com.example.android.xmppclienttest.R;
import com.example.android.xmppclienttest.sync.ConnectionService;

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
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CustomConnection implements ConnectionListener {

    private static final String TAG = CustomConnection.class.getSimpleName();

    private static final String DEFAULT_USER_NAME = "student1";
    private static final String DEFAULT_USER_RESOURCE = "strathmore-student";
    private static final String DEFAULT_USER_SUBSCRIPTION = "important";

    private static CustomConnection sInstance;
    private static XMPPTCPConnection connection;

    private InetAddress mHostAddress;
    private String mUsername;
    private String mPassword;
    private String mServiceName;

    private String mUserJidResource;

    private XMPPTCPConnectionConfiguration.Builder connectionConfiguration;
    private LeafNode mEventNode;

    static String getDefaultRemoteHostAddress() {
        return ApplicationContextProvider.getContext().getResources()
                .getString(R.string.settings_server_ip_addr_default);
    }

    static String getDefaultUserName() {
        return DEFAULT_USER_NAME;
    }

    static String getDefaultUserSubscription() {
        return DEFAULT_USER_SUBSCRIPTION;
    }

    static String getDefaultUserResource() {
        return DEFAULT_USER_RESOURCE;
    }

    public enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED
    }

    public enum LoggedInState {
        LOGGED_IN, LOGGED_OUT
    }

    public static CustomConnection getInstance(String hostAddress) {
        if (sInstance == null) {
            sInstance = new CustomConnection(hostAddress);
        }
        return sInstance;
    }

    private CustomConnection(String hostAddress) {
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

    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + mServiceName);
        if (connection != null) {
            connection.instantShutdown();
        }
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        connection = null;
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
        mUsername = PreferenceUtilities.getUniqueId(context);
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
        connection.setReplyTimeout(10000);

        connection.connect();
        connection.login();

        ReconnectionManager.setEnabledPerDefault(true);
        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        reconnectionManager.enableAutomaticReconnection();

//        subscribe();
    }

    private boolean checkAndSetResourceName(Context context) {
        String userResourceName = PreferenceUtilities.getResourceName(context);
        boolean isUserResourceSet = !DEFAULT_USER_NAME.equals(userResourceName);

        if (isUserResourceSet) {
            mUserJidResource = userResourceName;
        }

        return isUserResourceSet;
    }

    public void subscribe(ItemEventListener eventListener) {
        Log.d(TAG, "subscribe()");
        Context context = ApplicationContextProvider.getContext();
        try {
            if (connection != null) {
//                PublishItemEventListener eventListener = new PublishItemEventListener();
                PubSubManager pubSubManager = PubSubManager.getInstance(connection);
                mEventNode = pubSubManager.getNode("testNode");
                if (eventListener != null) {
                    mEventNode.addItemEventListener(eventListener);
                }
                String user = connection.getUser().getLocalpart().asUnescapedString();

                if (!PreferenceUtilities.checkSubIdIsSet(context)) {
                    System.out.println("Sub id not set");
                    if (!DEFAULT_USER_NAME.equals(user)) {
                        Subscription subscription =
                                mEventNode.subscribe(String.valueOf(connection.getUser()));
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

    public LeafNode getNode() {
        return mEventNode;
    }
}
