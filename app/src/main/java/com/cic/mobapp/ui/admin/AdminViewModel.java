package com.cic.mobapp.ui.admin;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.cic.mobapp.data.local.AppDatabase;
import com.cic.mobapp.data.local.entity.AnnouncementEntity;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import com.cic.mobapp.data.local.entity.UserEntity;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.data.remote.dto.EventDto;
import com.cic.mobapp.data.remote.dto.ResourceDto;
import com.cic.mobapp.data.remote.dto.UserDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminViewModel extends AndroidViewModel {

    private final ApiService api;
    private final AppDatabase db;

    private final MutableLiveData<List<UserDto>>         users    = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<EventDto>>        events   = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ResourceDto>>     resources = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<String, Integer>>  stats    = new MutableLiveData<>();
    private final MutableLiveData<String>                error    = new MutableLiveData<>();
    private final MutableLiveData<String>                toast    = new MutableLiveData<>();
    private final MutableLiveData<List<AuditEntry>>      auditLog = new MutableLiveData<>(new ArrayList<>());

    // Announcements served directly from Room LiveData — updates automatically on any CRUD
    private LiveData<List<AnnouncementDto>> announcements;

    public AdminViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getApiService();
        db  = AppDatabase.getInstance(application);
        // Wire announcements to Room so any CRUD update is instantly reflected
        announcements = Transformations.map(
                db.announcementDao().getAll(),
                entities -> {
                    if (entities == null) return new ArrayList<>();
                    List<AnnouncementDto> result = new ArrayList<>();
                    for (AnnouncementEntity e : entities) result.add(annEntityToDto(e));
                    return result;
                });
        refresh();
    }

    public LiveData<List<UserDto>>         getUsers()         { return users; }
    public LiveData<List<EventDto>>        getEvents()        { return events; }
    public LiveData<List<ResourceDto>>     getResources()     { return resources; }
    public LiveData<List<AnnouncementDto>> getAnnouncements() { return announcements; }
    public LiveData<Map<String, Integer>>  getStats()         { return stats; }
    public LiveData<String>                getError()         { return error; }
    public LiveData<String>                getToast()         { return toast; }
    public LiveData<List<AuditEntry>>      getAuditLog()      { return auditLog; }

    private void audit(String action, String entity, String detail) {
        List<AuditEntry> log = new ArrayList<>();
        if (auditLog.getValue() != null) log.addAll(auditLog.getValue());
        log.add(0, new AuditEntry(action, entity, detail));
        auditLog.postValue(log);
    }

    public void auditAction(String action, String entity, String detail) {
        audit(action, entity, detail);
    }

    public void showToast(String message) {
        toast.postValue(message);
    }

    public void clearAuditLog() { auditLog.setValue(new ArrayList<>()); }

    public void refresh() {
        loadStats();
        loadUsers();
        loadEvents();
        loadResources();
        loadAnnouncements();
    }

    // â"€â"€ Stats â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€

    private void loadStats() {
        api.getAdminStats().enqueue(new Callback<Map<String, Integer>>() {
            @Override public void onResponse(Call<Map<String, Integer>> c, Response<Map<String, Integer>> r) {
                if (r.isSuccessful() && r.body() != null) stats.postValue(r.body());
                else loadStatsFromRoom();
            }
            @Override public void onFailure(Call<Map<String, Integer>> c, Throwable t) {
                loadStatsFromRoom();
            }
        });
    }

    private void loadStatsFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            java.util.HashMap<String, Integer> s = new java.util.HashMap<>();
            s.put("totalUsers",         db.userDao().getAllSync().size());
            s.put("totalEvents",        db.eventDao().getAllSync().size());
            s.put("totalResources",     db.resourceDao().getAllSync().size());
            s.put("totalAnnouncements", db.announcementDao().countAll());
            stats.postValue(s);
        });
    }

    private static AnnouncementDto annEntityToDto(AnnouncementEntity e) {
        AnnouncementDto d = new AnnouncementDto();
        d.id = e.id; d.title = e.title; d.body = e.body;
        d.type = e.type; d.priority = e.priority;
        d.isPinned = e.isPinned; d.createdAt = e.createdAt;
        return d;
    }

    // â"€â"€ Users â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€

    private void loadUsers() {
        api.getAllUsers().enqueue(new Callback<List<UserDto>>() {
            @Override public void onResponse(Call<List<UserDto>> c, Response<List<UserDto>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    users.postValue(r.body());
                } else {
                    loadUsersFromRoom();
                }
            }
            @Override public void onFailure(Call<List<UserDto>> c, Throwable t) {
                loadUsersFromRoom();
            }
        });
    }

    private void loadUsersFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UserEntity> entities = db.userDao().getAllSync();
            List<UserDto> dtos = entities.stream()
                    .map(AdminViewModel::entityToDto)
                    .collect(Collectors.toList());
            users.postValue(dtos);
        });
    }

    private static UserDto entityToDto(UserEntity e) {
        UserDto d = new UserDto();
        d.id        = e.id;
        d.username  = e.username;
        d.email     = e.email;
        d.avatarUrl = e.avatarUrl;
        d.discordId = e.discordId;
        d.role      = e.role;
        d.xp        = e.xp;
        d.level     = e.level;
        d.createdAt = e.createdAt;
        return d;
    }

    public void changeUserRole(String userId, String newRole) {
        UserDto patch = new UserDto();
        patch.role = newRole;
        api.updateUser(userId, patch).enqueue(new Callback<UserDto>() {
            @Override public void onResponse(Call<UserDto> c, Response<UserDto> r) {
                if (r.isSuccessful()) { toast.postValue("Role updated to " + newRole); audit(AuditEntry.ROLE, "User", "â†’ " + newRole); loadUsers(); loadStats(); }
                else error.postValue("Failed to update role");
            }
            @Override public void onFailure(Call<UserDto> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteUser(String userId, String username) {
        api.deleteUser(userId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    toast.postValue("User " + username + " deleted");
                    audit(AuditEntry.DELETE, "User", username);
                    loadUsers();
                    loadStats();
                } else error.postValue("Failed to delete user");
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    // â"€â"€ Events â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€

    private void loadEvents() {
        api.getEvents(null, null, null).enqueue(new Callback<List<EventDto>>() {
            @Override public void onResponse(Call<List<EventDto>> c, Response<List<EventDto>> r) {
                if (r.isSuccessful() && r.body() != null) events.postValue(r.body());
                else loadEventsFromRoom();
            }
            @Override public void onFailure(Call<List<EventDto>> c, Throwable t) {
                loadEventsFromRoom();
            }
        });
    }

    private void loadEventsFromRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<com.cic.mobapp.data.local.entity.EventEntity> entities =
                    db.eventDao().getAllSync();
            List<EventDto> dtos = entities.stream()
                    .map(AdminViewModel::entityToEventDto)
                    .collect(Collectors.toList());
            events.postValue(dtos);
        });
    }

    private static EventDto entityToEventDto(com.cic.mobapp.data.local.entity.EventEntity e) {
        EventDto d = new EventDto();
        d.id = e.id; d.title = e.title; d.description = e.description;
        d.bannerUrl = e.bannerUrl; d.location = e.location; d.date = e.date;
        d.type = e.type; d.difficulty = e.difficulty;
        d.capacity = e.capacity; d.registeredCount = e.registeredCount;
        d.isRegistered = e.isRegistered; d.organizerId = e.organizerId;
        return d;
    }

    public void duplicateEvent(EventDto original) {
        EventDto copy = new EventDto();
        copy.title          = "Copy of " + original.title;
        copy.description    = original.description;
        copy.location       = original.location;
        copy.date           = original.date;
        copy.type           = original.type;
        copy.difficulty     = original.difficulty;
        copy.capacity       = original.capacity;
        copy.bannerUrl      = original.bannerUrl;
        copy.registeredCount = 0;
        createEvent(copy);
    }

    public void createEvent(EventDto dto) {
        if (dto.id == null || dto.id.isEmpty())
            dto.id = "local_ev_" + System.currentTimeMillis();
        final String localId = dto.id;
        writeEventToRoom(dto);
        toast.postValue("Event created");
        audit(AuditEntry.CREATE, "Event", dto.title);
        loadEvents(); loadStats();
        // Best-effort API sync
        api.createEvent(dto).enqueue(new Callback<EventDto>() {
            @Override public void onResponse(Call<EventDto> c, Response<EventDto> r) {
                if (r.isSuccessful() && r.body() != null && r.body().id != null) {
                    // Replace local entry with server-assigned ID
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.eventDao().deleteById(localId);
                        db.eventDao().upsert(dtoToEventEntity(r.body()));
                    });
                    loadEvents();
                }
            }
            @Override public void onFailure(Call<EventDto> c, Throwable t) {}
        });
    }

    public void updateEvent(String id, EventDto dto) {
        dto.id = id;
        writeEventToRoom(dto);
        toast.postValue("Event updated");
        audit(AuditEntry.UPDATE, "Event", dto.title);
        loadEvents();
        api.updateEvent(id, dto).enqueue(new Callback<EventDto>() {
            @Override public void onResponse(Call<EventDto> c, Response<EventDto> r) {
                if (r.isSuccessful() && r.body() != null) writeEventToRoom(r.body());
            }
            @Override public void onFailure(Call<EventDto> c, Throwable t) {}
        });
    }

    public void deleteEvent(String eventId) {
        Executors.newSingleThreadExecutor().execute(() -> db.eventDao().deleteById(eventId));
        toast.postValue("Event deleted");
        audit(AuditEntry.DELETE, "Event", eventId);
        loadEvents(); loadStats();
        api.deleteEvent(eventId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {}
            @Override public void onFailure(Call<Void> c, Throwable t) {}
        });
    }

    private void writeEventToRoom(EventDto dto) {
        EventEntity e = dtoToEventEntity(dto);
        Executors.newSingleThreadExecutor().execute(() -> db.eventDao().upsert(e));
    }

    private static EventEntity dtoToEventEntity(EventDto d) {
        EventEntity e = new EventEntity();
        e.id = d.id; e.title = d.title; e.description = d.description;
        e.bannerUrl = d.bannerUrl; e.location = d.location; e.date = d.date;
        e.type = d.type; e.difficulty = d.difficulty;
        e.capacity = d.capacity; e.registeredCount = d.registeredCount;
        e.isRegistered = d.isRegistered; e.organizerId = d.organizerId;
        return e;
    }

    // â"€â"€ Resources â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€

    private void loadResources() {
        api.getResources(null, null, null).enqueue(new Callback<List<ResourceDto>>() {
            @Override public void onResponse(Call<List<ResourceDto>> c, Response<List<ResourceDto>> r) {
                if (r.isSuccessful() && r.body() != null) resources.postValue(r.body());
            }
            @Override public void onFailure(Call<List<ResourceDto>> c, Throwable t) {
                error.postValue("Failed to load resources: " + t.getMessage());
            }
        });
    }

    public void createResource(String title, String category, String type,
                               String difficulty, String fileUrl, String description) {
        ResourceEntity re = new ResourceEntity();
        re.id          = "local_res_" + System.currentTimeMillis();
        re.title       = title; re.category = category; re.type = type;
        re.difficulty  = difficulty; re.fileUrl = fileUrl; re.description = description;
        re.uploadedBy  = "Admin";
        re.createdAt   = java.time.LocalDate.now().toString();
        Executors.newSingleThreadExecutor().execute(() -> db.resourceDao().upsert(re));
        toast.postValue("Resource published");
        audit(AuditEntry.CREATE, "Resource", title);
        loadResources(); loadStats();
        ResourceDto dto = new ResourceDto();
        dto.title = title; dto.category = category; dto.type = type;
        dto.difficulty = difficulty; dto.fileUrl = fileUrl; dto.description = description;
        api.uploadResource(dto).enqueue(new Callback<ResourceDto>() {
            @Override public void onResponse(Call<ResourceDto> c, Response<ResourceDto> r) {
                if (r.isSuccessful() && r.body() != null && r.body().id != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.resourceDao().deleteById(re.id);
                        db.resourceDao().upsert(dtoToResourceEntity(r.body()));
                    });
                    loadResources();
                }
            }
            @Override public void onFailure(Call<ResourceDto> c, Throwable t) {}
        });
    }

    public void updateResource(String id, String title, String category, String type,
                               String difficulty, String fileUrl, String description) {
        ResourceEntity re = new ResourceEntity();
        re.id = id; re.title = title; re.category = category; re.type = type;
        re.difficulty = difficulty; re.fileUrl = fileUrl; re.description = description;
        Executors.newSingleThreadExecutor().execute(() -> db.resourceDao().upsert(re));
        toast.postValue("Resource updated");
        audit(AuditEntry.UPDATE, "Resource", title);
        loadResources();
        ResourceDto dto = new ResourceDto();
        dto.title = title; dto.category = category; dto.type = type;
        dto.difficulty = difficulty; dto.fileUrl = fileUrl; dto.description = description;
        api.updateResource(id, dto).enqueue(new Callback<ResourceDto>() {
            @Override public void onResponse(Call<ResourceDto> c, Response<ResourceDto> r) {}
            @Override public void onFailure(Call<ResourceDto> c, Throwable t) {}
        });
    }

    public void deleteResource(String resourceId) {
        Executors.newSingleThreadExecutor().execute(() -> db.resourceDao().deleteById(resourceId));
        toast.postValue("Resource deleted");
        audit(AuditEntry.DELETE, "Resource", resourceId);
        loadResources(); loadStats();
        api.deleteResource(resourceId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {}
            @Override public void onFailure(Call<Void> c, Throwable t) {}
        });
    }

    private static ResourceEntity dtoToResourceEntity(ResourceDto d) {
        ResourceEntity e = new ResourceEntity();
        e.id = d.id; e.title = d.title; e.category = d.category; e.type = d.type;
        e.difficulty = d.difficulty; e.fileUrl = d.fileUrl; e.description = d.description;
        e.uploadedBy = d.uploadedBy;
        if (d.tags != null) e.tags = String.join(",", d.tags);
        return e;
    }

    // â"€â"€ Announcements â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€â"€

    private void loadAnnouncements() {
        // Load from Room — single source of truth; API sync is best-effort
        Executors.newSingleThreadExecutor().execute(() -> {
            // Trigger a Room observation by posting the current value;
            // the LiveData in HomeViewModel observes Room directly.
        });
        api.getAnnouncements(null).enqueue(new Callback<List<AnnouncementDto>>() {
            @Override public void onResponse(Call<List<AnnouncementDto>> c, Response<List<AnnouncementDto>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    // Sync API data into Room; Room LiveData notifies all observers
                    List<AnnouncementEntity> entities = new ArrayList<>();
                    for (AnnouncementDto d : r.body()) entities.add(dtoToAnnEntity(d));
                    Executors.newSingleThreadExecutor().execute(() ->
                            db.announcementDao().upsertAll(entities));
                }
            }
            @Override public void onFailure(Call<List<AnnouncementDto>> c, Throwable t) {}
        });
    }

    public void createAnnouncement(String title, String body, String type, boolean pinned) {
        AnnouncementEntity e = new AnnouncementEntity();
        e.id        = "local_ann_" + System.currentTimeMillis();
        e.title     = title; e.body = body; e.type = type;
        e.priority  = "Normal"; e.isPinned = pinned;
        e.createdAt = java.time.LocalDate.now().toString();
        Executors.newSingleThreadExecutor().execute(() -> db.announcementDao().upsert(e));
        toast.postValue("Announcement created");
        audit(AuditEntry.CREATE, "Announcement", title);
        loadStats();
        AnnouncementDto dto = new AnnouncementDto();
        dto.title = title; dto.body = body; dto.type = type; dto.isPinned = pinned; dto.priority = "MEDIUM";
        api.createAnnouncement(dto).enqueue(new Callback<AnnouncementDto>() {
            @Override public void onResponse(Call<AnnouncementDto> c, Response<AnnouncementDto> r) {
                if (r.isSuccessful() && r.body() != null && r.body().id != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.announcementDao().deleteById(e.id);
                        db.announcementDao().upsert(dtoToAnnEntity(r.body()));
                    });
                }
            }
            @Override public void onFailure(Call<AnnouncementDto> c, Throwable t) {}
        });
    }

    public void updateAnnouncement(String id, String title, String body, String type, boolean pinned) {
        AnnouncementEntity e = new AnnouncementEntity();
        e.id = id; e.title = title; e.body = body; e.type = type; e.isPinned = pinned;
        e.priority = "Normal"; e.createdAt = java.time.LocalDate.now().toString();
        Executors.newSingleThreadExecutor().execute(() -> db.announcementDao().upsert(e));
        toast.postValue("Announcement updated");
        audit(AuditEntry.UPDATE, "Announcement", title);
        AnnouncementDto dto = new AnnouncementDto();
        dto.title = title; dto.body = body; dto.type = type; dto.isPinned = pinned;
        api.updateAnnouncement(id, dto).enqueue(new Callback<AnnouncementDto>() {
            @Override public void onResponse(Call<AnnouncementDto> c, Response<AnnouncementDto> r) {}
            @Override public void onFailure(Call<AnnouncementDto> c, Throwable t) {}
        });
    }

    public void deleteAnnouncement(String announcementId) {
        Executors.newSingleThreadExecutor().execute(() ->
                db.announcementDao().deleteById(announcementId));
        toast.postValue("Announcement deleted");
        audit(AuditEntry.DELETE, "Announcement", announcementId);
        loadStats();
        api.deleteAnnouncement(announcementId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {}
            @Override public void onFailure(Call<Void> c, Throwable t) {}
        });
    }

    private static AnnouncementEntity dtoToAnnEntity(AnnouncementDto d) {
        AnnouncementEntity e = new AnnouncementEntity();
        e.id = d.id != null ? d.id : ("ann_" + System.currentTimeMillis());
        e.title = d.title; e.body = d.body; e.type = d.type;
        e.priority = d.priority; e.isPinned = d.isPinned;
        e.createdAt = d.createdAt;
        return e;
    }

    // ── Event Participant Management ───────────────────────────────────────────

    public List<com.cic.mobapp.data.local.entity.EventRegistrationEntity> getParticipantsSync(String eventId) {
        return db.eventRegistrationDao().getParticipants(eventId);
    }

    public int getParticipantCount(String eventId) {
        return db.eventRegistrationDao().countParticipants(eventId);
    }

    public List<String> getParticipantIds(String eventId) {
        return db.eventRegistrationDao().getParticipantIds(eventId);
    }

    public void registerParticipant(String eventId, UserDto user) {
        Executors.newSingleThreadExecutor().execute(() -> {
            com.cic.mobapp.data.local.entity.EventRegistrationEntity reg =
                    new com.cic.mobapp.data.local.entity.EventRegistrationEntity();
            reg.eventId      = eventId;
            reg.userId       = user.id;
            reg.username     = user.username;
            reg.avatarUrl    = user.avatarUrl;
            reg.userRole     = user.role;
            reg.registeredAt = java.time.LocalDate.now().toString();
            reg.status       = "REGISTERED";
            db.eventRegistrationDao().register(reg);
            // Update registeredCount in Room
            updateEventRegisteredCount(eventId);
            toast.postValue(user.username + " registered");
            audit(AuditEntry.CREATE, "Registration", user.username + " -> event " + eventId);
        });
    }

    public void unregisterParticipant(String eventId, String userId, String username) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.eventRegistrationDao().unregister(eventId, userId);
            updateEventRegisteredCount(eventId);
            toast.postValue(username + " removed from event");
            audit(AuditEntry.DELETE, "Registration", username + " removed from " + eventId);
        });
    }

    public void updateParticipantStatus(String eventId, String userId, String status) {
        Executors.newSingleThreadExecutor().execute(() ->
                db.eventRegistrationDao().updateStatus(eventId, userId, status));
    }

    private void updateEventRegisteredCount(String eventId) {
        int count = db.eventRegistrationDao().countParticipants(eventId);
        com.cic.mobapp.data.local.entity.EventEntity ev =
                db.eventDao().getByIdSync(eventId);
        if (ev != null) {
            ev.registeredCount = count;
            db.eventDao().upsert(ev);
        }
    }
}
