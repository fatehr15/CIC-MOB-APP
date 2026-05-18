package com.cic.mobapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.cic.mobapp.data.local.entity.AnnouncementEntity;
import java.util.List;

@Dao
public interface AnnouncementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<AnnouncementEntity> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AnnouncementEntity ann);

    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, createdAt DESC")
    LiveData<List<AnnouncementEntity>> getAll();

    @Query("SELECT COUNT(*) FROM announcements WHERE id = :id")
    int countById(String id);

    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, createdAt DESC")
    List<AnnouncementEntity> getAllSync();

    @Query("SELECT COUNT(*) FROM announcements")
    int countAll();

    @Query("DELETE FROM announcements WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM announcements")
    void deleteAll();
}
