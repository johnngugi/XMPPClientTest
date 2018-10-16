package com.example.android.xmppclienttest;

import android.app.Application;
import android.content.Context;

public class ApplicationContextProvider extends Application {
    /**
     * Keeps a reference of the application context
     */
    private static Application sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    private static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }
}
