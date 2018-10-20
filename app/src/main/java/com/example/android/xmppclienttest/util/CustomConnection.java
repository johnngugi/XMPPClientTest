package com.example.android.xmppclienttest.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.android.xmppclienttest.ApplicationContextProvider;
import com.example.android.xmppclienttest.R;
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
import org.jxmpp.jid.parts.Resourcepart;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class CustomConnection implements ConnectionListener {

    private static final String TAG = CustomConnection.class.getSimpleName();
    private static final String DEFAULT_USER_RESOURCE = "strathmore-student";
    private static final String DEFAULT_USER_SUBSCRIPTION = "important";

    private static final String DEFAULT_USER_NAME = "student1";
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static String uniqueID = null;

    private static CustomConnection sInstance;
    private static XMPPTCPConnection connection;

    private InetAddress mHostAddress;
    private String mUsername;
    private String mPassword;
    private String mServiceName;

    private String mUserJidResource;

    private XMPPTCPConnectionConfiguration.Builder connectionConfiguration;

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

        boolean accountCreated = checkAccountCreated(context);

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
            boolean isUserNameSet = checkUserNameIsSet(context);
            if (checkResourceNameIsSet(context) && isUserNameSet) {
                connectionConfiguration.setUsernameAndPassword(mUsername, mPassword);
                connectionConfiguration.setResource(mUserJidResource);
                connectAndLogin(connectionConfiguration);
            } else {
                setUserResourceName(context);
                connectionConfiguration.setUsernameAndPassword(mUsername, mPassword);
                connectionConfiguration.setResource(mUserJidResource);
                connectAndLogin(connectionConfiguration);
            }
        }

//        // Check if user resource is set and use that to connect
//        // If not connect and save the new user resource
//        if (checkResourceNameIsSet(context)) {
//            connectionConfiguration.setResource(mUserJidResource);
//            connectAndLogin(connectionConfiguration);
//        } else {
//            connectAndLogin(connectionConfiguration);
//            setUserResourceName(context);
//            disconnectThenAttemptReconnect();
//        }

        ReconnectionManager.setEnabledPerDefault(true);
        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        reconnectionManager.enableAutomaticReconnection();

        subscribe();
    }

    private boolean checkUserNameIsSet(Context context) {
        String sharedPrefsFile = context.getResources().getString(
                R.string.shared_preference_file);

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                sharedPrefsFile, Context.MODE_PRIVATE);

        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

        boolean isUserNameSet = uniqueID != null && !uniqueID.equals(DEFAULT_USER_NAME);

        if (isUserNameSet) {
            mUsername = uniqueID;
        }
        return isUserNameSet;
    }

    private void createAccount(XMPPTCPConnectionConfiguration.Builder configuration, Context context)
            throws InterruptedException, XMPPException, SmackException, IOException {
        connectAndLogin(configuration);
        uniqueID = getUniqueId(context);
        mUsername = uniqueID;
        setUserResourceName(context);

        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        accountManager.createAccount(Localpart.from(mUsername), mPassword);

        disconnectThenAttemptReconnect();
    }

    private String getUniqueId(Context context) {
        String sharedPrefsFile = context.getResources().getString(
                R.string.shared_preference_file);

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                sharedPrefsFile, Context.MODE_PRIVATE);

        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_UNIQUE_ID, uniqueID);
            editor.apply();
        }
        return uniqueID;
    }

    private boolean checkAccountCreated(Context context) {
        String sharedPrefsFile = context.getResources().getString(
                R.string.shared_preference_file);

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                sharedPrefsFile, Context.MODE_PRIVATE);

        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

        return uniqueID != null && !uniqueID.equals(DEFAULT_USER_NAME);
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
        connection.connect();
        connection.login();
    }

    private void setUserResourceName(Context context) {
        String resourceNameKey = context.getResources().getString(
                R.string.shared_preference_file);
        String userResourceJidKey = context.getResources().getString(R.string.user_resource_jid_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                resourceNameKey, Context.MODE_PRIVATE);

        Resourcepart resourcepart = connection.getUser().getResourceOrNull();

        if (resourcepart != null) {
            mUserJidResource = resourcepart.toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(userResourceJidKey, mUserJidResource);
            editor.apply();
        }
    }

    private boolean checkResourceNameIsSet(Context context) {
        String resourceNameKey = context.getResources().getString(
                R.string.shared_preference_file);
        String userResourceJidKey = context.getResources().getString(R.string.user_resource_jid_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                resourceNameKey, Context.MODE_PRIVATE);

        String userResourceName = sharedPref.getString(userResourceJidKey, DEFAULT_USER_RESOURCE);

        boolean isUserResourceSet = !DEFAULT_USER_RESOURCE.equals(userResourceName);

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

    private boolean checkSubIdIsSet(Context context) {
        String resourceName = context.getResources().getString(
                R.string.shared_preference_file);
        String broadcastSubIdKey = context.getResources().getString(R.string.broadcast_sub_id_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                resourceName, Context.MODE_PRIVATE);

        String userSubId = sharedPref.getString(broadcastSubIdKey, DEFAULT_USER_SUBSCRIPTION);

        return !DEFAULT_USER_SUBSCRIPTION.equals(userSubId);
    }

    private void saveSubscriptionId(String subId) {
        Context context = ApplicationContextProvider.getContext();

        String resourceName = context.getResources().getString(
                R.string.shared_preference_file);
        String broadcastSubIdKey = context.getResources().getString(R.string.broadcast_sub_id_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                resourceName, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(broadcastSubIdKey, subId);
        editor.apply();
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

                if (!checkSubIdIsSet(context)) {
                    System.out.println("Sub id not set");
                    if (!DEFAULT_USER_NAME.equals(user)) {
                        Subscription subscription =
                                eventNode.subscribe(String.valueOf(connection.getUser()));
                        saveSubscriptionId(subscription.getId());
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
                    Tasks.addEvent(ApplicationContextProvider.getContext(), messageEntry);
                    System.out.println("Parsed message: " + messageEntry.getBody());
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
