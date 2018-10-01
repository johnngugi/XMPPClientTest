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

import java.io.IOException;

public class ConnectionService extends Service {

    private static final String TAG = ConnectionService.class.getSimpleName();

    public static CustomConnection.ConnectionState sConnectionState;
    public static CustomConnection.LoggedInState sLoggedInState;

    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to
    private CustomConnection mConnection;
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

    public void start() {
        Log.d(TAG, " Service Start() function called.");
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
            mConnection = new CustomConnection(this, "192.168.100.2");
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
}
