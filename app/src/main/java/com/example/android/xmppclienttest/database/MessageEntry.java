package com.example.android.xmppclienttest.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "message")
public class MessageEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String subject;
    private String body;
    private String filePath;

    @Ignore
    public MessageEntry(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public MessageEntry(int id, String subject, String body) {
        this.id = id;
        this.subject = subject;
        this.body = body;
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

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}