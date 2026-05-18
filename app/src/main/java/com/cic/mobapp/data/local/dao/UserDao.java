package com.cic.mobapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.cic.mobapp.data.local.entity.UserEntity;
import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserEntity user);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void upsertAll(List<UserEntity> users);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<UserEntity> getById(String id);

    @Query("SELECT COUNT(*) FROM users WHERE id = :id")
    int countById(String id);

    @Query("SELECT * FROM users ORDER BY username ASC")
    List<UserEntity> getAllSync();

    @Query("DELETE FROM users")
    void deleteAll();
}
