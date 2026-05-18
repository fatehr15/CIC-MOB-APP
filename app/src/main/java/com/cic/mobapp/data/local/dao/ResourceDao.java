package com.cic.mobapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import java.util.List;

@Dao
public interface ResourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ResourceEntity> resources);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ResourceEntity resource);

    @Query("DELETE FROM resources WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM resources ORDER BY createdAt DESC")
    LiveData<List<ResourceEntity>> getAll();

    @Query("SELECT * FROM resources ORDER BY createdAt DESC")
    List<ResourceEntity> getAllSync();

    @Query("SELECT * FROM resources WHERE category = :category ORDER BY createdAt DESC")
    LiveData<List<ResourceEntity>> getByCategory(String category);

    @Query("SELECT COUNT(*) FROM resources WHERE id = :id")
    int countById(String id);

    @Query("DELETE FROM resources")
    void deleteAll();
}
