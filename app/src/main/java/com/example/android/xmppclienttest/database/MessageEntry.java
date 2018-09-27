package com.example.android.xmppclienttest.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "message")
public class MessageEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;

    @Ignore
    public MessageEntry(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public MessageEntry(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}