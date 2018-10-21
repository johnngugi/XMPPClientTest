package com.example.android.xmppclienttest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;

public class MessageDetailViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private LiveData<MessageEntry> messageEntry;

    public MessageDetailViewModel(AppDatabase database, int messageId) {
        messageEntry = database.messageDao().loadMessageById(messageId);
    }

    public LiveData<MessageEntry> getMessageEntry() {
        return messageEntry;
    }
}
