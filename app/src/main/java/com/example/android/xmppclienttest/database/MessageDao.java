package com.example.android.xmppclienttest.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message")
    LiveData<List<MessageEntry>> loadAllMessages();

    @Query("SELECT * FROM message WHERE id = :id")
    LiveData<MessageEntry> loadMessageById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSingleMessage(MessageEntry message);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMessage(MessageEntry message);

    @Delete
    void deleteSingleMessage(MessageEntry message);

    @Query("DELETE FROM message")
    void deleteAllMessages();

}
