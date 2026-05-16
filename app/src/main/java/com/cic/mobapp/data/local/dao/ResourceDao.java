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

    @Query("SELECT * FROM resources ORDER BY createdAt DESC")
    LiveData<List<ResourceEntity>> getAll();

    @Query("SELECT * FROM resources WHERE category = :category ORDER BY createdAt DESC")
    LiveData<List<ResourceEntity>> getByCategory(String category);

    @Query("DELETE FROM resources")
    void deleteAll();
}
