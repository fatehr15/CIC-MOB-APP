package com.cic.mobapp.ui.admin;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.data.remote.dto.EventDto;
import com.cic.mobapp.data.remote.dto.ResourceDto;
import com.cic.mobapp.data.remote.dto.UserDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminViewModel extends AndroidViewModel {

    private final ApiService api;

    private final MutableLiveData<List<UserDto>>         users         = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<EventDto>>        events        = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ResourceDto>>     resources     = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<AnnouncementDto>> announcements = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<String, Integer>>  stats         = new MutableLiveData<>();
    private final MutableLiveData<String>                error         = new MutableLiveData<>();
    private final MutableLiveData<String>                toast         = new MutableLiveData<>();
    private final MutableLiveData<List<AuditEntry>>      auditLog      = new MutableLiveData<>(new ArrayList<>());

    public AdminViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getApiService();
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

    public void clearAuditLog() { auditLog.setValue(new ArrayList<>()); }

    public void refresh() {
        loadStats();
        loadUsers();
        loadEvents();
        loadResources();
        loadAnnouncements();
    }

    // â”€â”€ Stats â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void loadStats() {
        api.getAdminStats().enqueue(new Callback<Map<String, Integer>>() {
            @Override public void onResponse(Call<Map<String, Integer>> c, Response<Map<String, Integer>> r) {
                if (r.isSuccessful() && r.body() != null) stats.postValue(r.body());
            }
            @Override public void onFailure(Call<Map<String, Integer>> c, Throwable t) {}
        });
    }

    // â”€â”€ Users â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void loadUsers() {
        api.getAllUsers().enqueue(new Callback<List<UserDto>>() {
            @Override public void onResponse(Call<List<UserDto>> c, Response<List<UserDto>> r) {
                if (r.isSuccessful() && r.body() != null) users.postValue(r.body());
            }
            @Override public void onFailure(Call<List<UserDto>> c, Throwable t) {
                error.postValue("Failed to load users: " + t.getMessage());
            }
        });
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

    // â”€â”€ Events â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void loadEvents() {
        api.getEvents(null, null, null).enqueue(new Callback<List<EventDto>>() {
            @Override public void onResponse(Call<List<EventDto>> c, Response<List<EventDto>> r) {
                if (r.isSuccessful() && r.body() != null) events.postValue(r.body());
            }
            @Override public void onFailure(Call<List<EventDto>> c, Throwable t) {
                error.postValue("Failed to load events: " + t.getMessage());
            }
        });
    }

    public void createEvent(String title, String description, String location,
                            String date, String type, String difficulty, int capacity) {
        EventDto dto = new EventDto();
        dto.title          = title;
        dto.description    = description;
        dto.location       = location;
        dto.date           = date;
        dto.type           = type;
        dto.difficulty     = difficulty;
        dto.capacity       = capacity;
        dto.registeredCount = 0;

        api.createEvent(dto).enqueue(new Callback<EventDto>() {
            @Override public void onResponse(Call<EventDto> c, Response<EventDto> r) {
                if (r.isSuccessful()) { toast.postValue("Event created"); audit(AuditEntry.CREATE, "Event", title); loadEvents(); loadStats(); }
                else error.postValue("Failed to create event");
            }
            @Override public void onFailure(Call<EventDto> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void updateEvent(String id, String title, String description, String location,
                            String date, String type, String difficulty, int capacity) {
        EventDto dto = new EventDto();
        dto.title = title; dto.description = description; dto.location = location;
        dto.date = date; dto.type = type; dto.difficulty = difficulty; dto.capacity = capacity;
        api.updateEvent(id, dto).enqueue(new Callback<EventDto>() {
            @Override public void onResponse(Call<EventDto> c, Response<EventDto> r) {
                if (r.isSuccessful()) { toast.postValue("Event updated"); audit(AuditEntry.UPDATE, "Event", title); loadEvents(); }
                else error.postValue("Failed to update event");
            }
            @Override public void onFailure(Call<EventDto> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteEvent(String eventId) {
        api.deleteEvent(eventId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) { toast.postValue("Event deleted"); audit(AuditEntry.DELETE, "Event", eventId); loadEvents(); loadStats(); }
                else error.postValue("Failed to delete event");
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    // â”€â”€ Resources â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
        ResourceDto dto = new ResourceDto();
        dto.title       = title;
        dto.category    = category;
        dto.type        = type;
        dto.difficulty  = difficulty;
        dto.fileUrl     = fileUrl;
        dto.description = description;

        api.uploadResource(dto).enqueue(new Callback<ResourceDto>() {
            @Override public void onResponse(Call<ResourceDto> c, Response<ResourceDto> r) {
                if (r.isSuccessful()) { toast.postValue("Resource published"); audit(AuditEntry.CREATE, "Resource", title); loadResources(); loadStats(); }
                else error.postValue("Failed to publish resource");
            }
            @Override public void onFailure(Call<ResourceDto> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void updateResource(String id, String title, String category, String type,
                               String difficulty, String fileUrl, String description) {
        ResourceDto dto = new ResourceDto();
        dto.title = title; dto.category = category; dto.type = type;
        dto.difficulty = difficulty; dto.fileUrl = fileUrl; dto.description = description;
        api.updateResource(id, dto).enqueue(new Callback<ResourceDto>() {
            @Override public void onResponse(Call<ResourceDto> c, Response<ResourceDto> r) {
                if (r.isSuccessful()) { toast.postValue("Resource updated"); audit(AuditEntry.UPDATE, "Resource", title); loadResources(); }
                else error.postValue("Failed to update resource");
            }
            @Override public void onFailure(Call<ResourceDto> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteResource(String resourceId) {
        api.deleteResource(resourceId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) { toast.postValue("Resource deleted"); audit(AuditEntry.DELETE, "Resource", resourceId); loadResources(); loadStats(); }
                else error.postValue("Failed to delete resource");
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    // â”€â”€ Announcements â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void loadAnnouncements() {
        api.getAnnouncements(null).enqueue(new Callback<List<AnnouncementDto>>() {
            @Override public void onResponse(Call<List<AnnouncementDto>> c, Response<List<AnnouncementDto>> r) {
                if (r.isSuccessful() && r.body() != null) announcements.postValue(r.body());
            }
            @Override public void onFailure(Call<List<AnnouncementDto>> c, Throwable t) {
                error.postValue("Failed to load announcements: " + t.getMessage());
            }
        });
    }

    public void createAnnouncement(String title, String body, String type, boolean pinned) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.title    = title;
        dto.body     = body;
        dto.type     = type;
        dto.priority = "MEDIUM";
        dto.isPinned = pinned;

        api.createAnnouncement(dto).enqueue(new Callback<AnnouncementDto>() {
            @Override public void onResponse(Call<AnnouncementDto> c, Response<AnnouncementDto> r) {
                if (r.isSuccessful()) { toast.postValue("Announcement created"); audit(AuditEntry.CREATE, "Announcement", title); loadAnnouncements(); loadStats(); }
                else error.postValue("Failed to create announcement");
            }
            @Override public void onFailure(Call<AnnouncementDto> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void updateAnnouncement(String id, String title, String body, String type, boolean pinned) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.title = title; dto.body = body; dto.type = type; dto.isPinned = pinned;
        api.updateAnnouncement(id, dto).enqueue(new Callback<AnnouncementDto>() {
            @Override public void onResponse(Call<AnnouncementDto> c, Response<AnnouncementDto> r) {
                if (r.isSuccessful()) { toast.postValue("Announcement updated"); audit(AuditEntry.UPDATE, "Announcement", title); loadAnnouncements(); }
                else error.postValue("Failed to update announcement");
            }
            @Override public void onFailure(Call<AnnouncementDto> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteAnnouncement(String announcementId) {
        api.deleteAnnouncement(announcementId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) { toast.postValue("Announcement deleted"); audit(AuditEntry.DELETE, "Announcement", announcementId); loadAnnouncements(); loadStats(); }
                else error.postValue("Failed to delete announcement");
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }
}
