package com.cic.mobapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.cic.mobapp.data.local.entity.EventEntity;
import java.util.List;

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<EventEntity> events);

    @Query("SELECT * FROM events ORDER BY date ASC")
    LiveData<List<EventEntity>> getAll();

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    LiveData<EventEntity> getById(String id);

    @Query("DELETE FROM events")
    void deleteAll();
}
