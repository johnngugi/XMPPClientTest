package com.example.android.xmppclienttest.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class NewEventIntentService extends IntentService {
    public NewEventIntentService() {
        super("NewEventIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        Tasks.executeTask(this, action);
    }
}
