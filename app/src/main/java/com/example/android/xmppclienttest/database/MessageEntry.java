package com.example.android.xmppclienttest.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "message", indices = {@Index(value = {"serverMessageId"}, unique = true)})
public class MessageEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String subject;
    private String body;
    private String filePath;
    private String serverMessageId;

    @Ignore
    public MessageEntry(String subject, String body, String serverMessageId) {
        this.subject = subject;
        this.body = body;
        this.serverMessageId = serverMessageId;
    }

    public MessageEntry(int id, String subject, String body, String serverMessageId) {
        this.id = id;
        this.subject = subject;
        this.body = body;
        this.serverMessageId = serverMessageId;
    }

    public int getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getServerMessageId() {
        return serverMessageId;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}