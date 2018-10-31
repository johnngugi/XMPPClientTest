package com.example.android.xmppclienttest.sync;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.android.xmppclienttest.ApplicationContextProvider;
import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.util.CustomConnection;
import com.example.android.xmppclienttest.util.MessageParser;
import com.example.android.xmppclienttest.util.NotificationUtils;
import com.example.android.xmppclienttest.util.PreferenceUtilities;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ConnectionService extends Service {

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_RESTART_FOREGROUND_SERVICE = "ACTION_RESTART_FOREGROUND_SERVICE";
    public static final String ACTION_SERVER_NOT_FOUND = "com.example.android.xmppclienttest.servernotfound";


    private static final Object LOCK = new Object();
    private static final String TAG = ConnectionService.class.getSimpleName();

    public static CustomConnection.ConnectionState sConnectionState;
    public static CustomConnection.LoggedInState sLoggedInState;
    public static CustomConnection mConnection;

    private static String HOST_ADDRESS = null;

    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to
    //the background thread.

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

        if (intent != null) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService();
                    }
                    start();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        stopForegroundService();
                    }
                    stopSelf();
                    break;
                case ACTION_RESTART_FOREGROUND_SERVICE:
                    restart();
                    break;
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void startForegroundService() {
        Log.d(TAG, "Start foreground service.");
        Notification notification = NotificationUtils.getForegroundServiceIntent(this);
        startForeground(1, notification);
    }

    private void stopForegroundService() {
        Log.d(TAG, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        stop();
    }

    public void initConnection() {
        if (mConnection == null) {
            synchronized (LOCK) {
                mConnection = CustomConnection.getInstance(HOST_ADDRESS);
                try {
                    mConnection.connect();
                    mConnection.subscribe(new PublishItemEventListener());
                    Log.d(TAG, "Connection initalised");
                } catch (SmackException.ConnectionException e) {
                    Intent intent = new Intent(ConnectionService.ACTION_SERVER_NOT_FOUND);
                    intent.setPackage(ApplicationContextProvider.getContext().getPackageName());
                    ApplicationContextProvider.getContext().sendBroadcast(intent);
                    System.out.println("Server not found()");
                } catch (SASLErrorException e) {
                    Log.d(TAG, "Make sure the credentials are right and try again");
                } catch (IOException | InterruptedException | XMPPException | SmackException e) {
                    Log.d(TAG, "Something went wrong while connecting");
                    e.printStackTrace();
                    stopSelf();
                }
            }
        }
    }

    private void start() {
        Log.d(TAG, " Service start() function called.");
        synchronized (LOCK) {
            HOST_ADDRESS = PreferenceUtilities.getSavedHostAddress(ApplicationContextProvider.getContext());
            if (!mActive) {
                mActive = true;
                if (mThread == null || !mThread.isAlive()) {
                    mThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            mTHandler = new Handler();
                            initConnection();
                            Looper.loop();
                        }
                    });
                    mThread.start();
                }
            }
        }
    }

    public void stop() {
        Log.d(TAG, "Service stop() function called.");
        if (mTHandler != null) {
            mTHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mConnection != null) {
                        mConnection.disconnect();
                    }
                }
            });
            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mThread.interrupt();
            mThread = null;
            mActive = false;
        }
    }

    private void restart() {
        Log.d(TAG, "restart()");
        boolean greaterThanOrEqualToO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
        if (greaterThanOrEqualToO) {
            stopForegroundService();
        }
        stop();
        if (greaterThanOrEqualToO) {
            startForegroundService();
        }
        start();
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

    public static CustomConnection getConnection() {
        return mConnection;
    }

    public static Object getLock() {
        return LOCK;
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
                    Context context = ApplicationContextProvider.getContext();
                    MessageEntry messageEntry = parser.parseContent(payloadMessage);
                    Tasks.addEvent(context, messageEntry);
                    NotificationUtils.alertUserAboutNewEvent(context);
                    System.out.println("Parsed message: " + messageEntry.getBody());
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
