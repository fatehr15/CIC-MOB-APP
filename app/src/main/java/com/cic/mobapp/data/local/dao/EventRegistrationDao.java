package com.cic.mobapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.cic.mobapp.data.local.entity.EventRegistrationEntity;
import java.util.List;

@Dao
public interface EventRegistrationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void register(EventRegistrationEntity reg);

    @Query("SELECT * FROM event_registrations WHERE eventId = :eventId ORDER BY registeredAt DESC")
    List<EventRegistrationEntity> getParticipants(String eventId);

    @Query("SELECT COUNT(*) FROM event_registrations WHERE eventId = :eventId")
    int countParticipants(String eventId);

    @Query("SELECT userId FROM event_registrations WHERE eventId = :eventId")
    List<String> getParticipantIds(String eventId);

    @Query("UPDATE event_registrations SET status = :status WHERE eventId = :eventId AND userId = :userId")
    void updateStatus(String eventId, String userId, String status);

    @Query("DELETE FROM event_registrations WHERE eventId = :eventId AND userId = :userId")
    void unregister(String eventId, String userId);

    @Query("DELETE FROM event_registrations WHERE eventId = :eventId")
    void clearEvent(String eventId);
}
