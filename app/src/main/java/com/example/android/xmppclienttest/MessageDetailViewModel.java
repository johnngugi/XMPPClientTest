package com.example.android.xmppclienttest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;

class MessageDetailViewModel extends ViewModel {
    private LiveData<MessageEntry> messageEntry;

    MessageDetailViewModel(AppDatabase database, int messageId) {
        messageEntry = database.messageDao().loadMessageById(messageId);
    }

    LiveData<MessageEntry> getMessageEntry() {
        return messageEntry;
    }
}
