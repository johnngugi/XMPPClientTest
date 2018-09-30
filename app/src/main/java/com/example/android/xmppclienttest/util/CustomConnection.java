package com.example.android.xmppclienttest.util;

import android.content.Context;
import android.util.Log;

import com.example.android.xmppclienttest.sync.ConnectionService;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CustomConnection implements ConnectionListener {

    private static final String TAG = CustomConnection.class.getSimpleName();
    InetAddress mHostAddress;
    private XMPPTCPConnection mConnection;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private String mResourceName;

    public enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED
    }

    public enum LoggedInState {
        LOGGED_IN, LOGGED_OUT
    }

    public CustomConnection(Context context, String hostAddress) {
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
        XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(mUsername, mPassword)
                .setXmppDomain("strathmore-computer")
                .setHostAddress(mHostAddress)
                .setPort(5222)
                .setResource(mResourceName)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        //Set up the ui thread broadcast message receiver.
        //setupUiThreadBroadCastMessageReceiver();

        mConnection = new XMPPTCPConnection(configuration);
        mConnection.addConnectionListener(this);
        mConnection.connect();
        mConnection.login();

        ReconnectionManager.setEnabledPerDefault(true);
        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.enableAutomaticReconnection();
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + mServiceName);
        if (mConnection != null) {
            mConnection.disconnect();
        }
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        mConnection = null;
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
}
