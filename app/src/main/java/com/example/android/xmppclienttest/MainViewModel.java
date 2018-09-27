package com.example.android.xmppclienttest;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<MessageEntry>> messages;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
        messages = appDatabase.messageDao().loadAllMessages();
    }

    public LiveData<List<MessageEntry>> getMessages() {
        return messages;
    }
}
