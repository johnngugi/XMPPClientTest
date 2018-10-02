package com.example.android.xmppclienttest.sync;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.android.xmppclienttest.util.CustomConnection;

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

import java.io.IOException;
import java.util.List;

public class ConnectionService extends Service {

    private static final String TAG = ConnectionService.class.getSimpleName();

    public static CustomConnection.ConnectionState sConnectionState;
    public static CustomConnection.LoggedInState sLoggedInState;
    public static final String HOST_ADDRESS = "10.51.18.96";

    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to
    private CustomConnection mConnection;
    //the background thread.

    public static final String UI_AUTHENTICATED =
            "com.example.android.xmppclienttest.uiauthenticated";

    private PublishItemEventListener eventListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        stop();
    }

    public void start() {
        Log.d(TAG, " Service Start() function called.");
        eventListener = new PublishItemEventListener();
        if (!mActive) {
            mActive = true;
            if (mThread == null || !mThread.isAlive()) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        //THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        mTHandler = new Handler();
                        initConnection();
                        Looper.loop();

                    }
                });
                mThread.start();
            }
        }
    }

    public void stop() {
        Log.d(TAG, "stop()");
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mConnection != null) {
                    mConnection.disconnect();
                }
            }
        });
    }

    private void initConnection() {
        Log.d(TAG, "initConnection()");
        if (mConnection == null) {
            mConnection = new CustomConnection(this, HOST_ADDRESS);
        }
        try {
            mConnection.connect();
        } catch (IOException | SmackException | XMPPException | InterruptedException e) {
            Log.d(TAG, "Something went wrong while connecting, make sure the credentials are right and try again");
            e.printStackTrace();
            //Stop the service all together.
            stopSelf();
        }

    }

    private void receivePublished(XMPPTCPConnection connection) throws
            XMPPException.XMPPErrorException,
            PubSubException.NotAPubSubNodeException, SmackException.NotConnectedException,
            InterruptedException, SmackException.NoResponseException {
        // Create a pubsub manager using an existing XMPPConnection
        PubSubManager pubSubManager = PubSubManager.getInstance(connection);

        LeafNode eventNode = pubSubManager.getNode("testNode");
        eventNode.addItemEventListener(eventListener);
        List<Subscription> subscriptions = eventNode.getSubscriptions();
        if (subscriptions == null) {
            eventNode.subscribe(String.valueOf(connection.getUser()));
        }
    }

    public static CustomConnection.ConnectionState getState() {
        if (sConnectionState == null) {
            return CustomConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static CustomConnection.LoggedInState getLoggedInState() {
        if (sLoggedInState == null) {
            return CustomConnection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    private class PublishItemEventListener implements ItemEventListener {

        @Override
        public void handlePublishedItems(ItemPublishEvent items) {
            Log.d(TAG, "event published");
            for (Object obj : items.getItems()) {
                PayloadItem item = (PayloadItem) obj;
                System.out.println("Payload: " + item.getPayload().toString());
                Tasks.executeTask(getApplicationContext(), Tasks.ACTION_NEW_EVENT);
            }
        }
    }
}
