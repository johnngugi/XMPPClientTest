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
import org.jxmpp.jid.EntityBareJid;

import java.io.IOException;

public class ConnectionService extends Service {

    private static final String TAG = ConnectionService.class.getSimpleName();
    private static final Object LOCK = new Object();

    public static CustomConnection.ConnectionState sConnectionState;
    public static CustomConnection.LoggedInState sLoggedInState;
    public static CustomConnection mConnection;

    public static final String HOST_ADDRESS = "10.55.41.4";

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
        start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        stop();
    }

    private void start() {
        Log.d(TAG, " Service Start() function called.");
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

    public void initConnection() {
        if (mConnection == null) {
            synchronized (LOCK) {
                mConnection = CustomConnection.getInstance(this, HOST_ADDRESS);
                try {
                    mConnection.connect();
                } catch (IOException | InterruptedException | XMPPException | SmackException e) {
                    Log.d(TAG,
                            "Something went wrong while connecting, " +
                                    "make sure the credentials are right and try again");
                    e.printStackTrace();
                    stopSelf();
                }
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
}
