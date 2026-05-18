# CIC Mobile Application

> **Cyber Innovators Club** — National Higher School of Cybersecurity (ENSS), Algeria  
> The digital infrastructure of a technical community.

---

## Table of Contents

1. [Problem & Vision](#1-problem--vision)
2. [System Architecture](#2-system-architecture)
3. [OOP Modeling](#3-oop-modeling)
4. [Design Patterns](#4-design-patterns)
5. [SOLID Principles](#5-solid-principles)
6. [UML Diagrams](#6-uml-diagrams)
7. [Android Engineering](#7-android-engineering)
8. [Data Model](#8-data-model)
9. [API Contract](#9-api-contract)
10. [Security Engineering](#10-security-engineering)
11. [Offline-First Architecture](#11-offline-first-architecture)
12. [Admin Panel Engineering](#12-admin-panel-engineering)
13. [Scalability & Performance](#13-scalability--performance)
14. [UI/UX Engineering](#14-uiux-engineering)
15. [Seed Data Catalog](#15-seed-data-catalog)
16. [Mock API Server](#16-mock-api-server)
17. [Future Extensibility](#17-future-extensibility)
18. [Setup & Build](#18-setup--build)
19. [Project File Structure](#19-project-file-structure)

---

## 1. Problem & Vision

### 1.1 Context

The **Cyber Innovators Club (CIC)** operates at the National Higher School of Cybersecurity (ENSS) in Algeria. It brings together students across multiple departments — Ethical Hacking, IT, Design, Multimedia, Human Resources, Finance, and Logistics — around a shared mission: building technical excellence in cybersecurity.

The club has been active since late 2024 with a growing membership of 54 students. In its first two semesters it delivered **21 workshops and events** covering topics from Graphic Design and Public Speaking to Binary Exploitation, Wireless Hacking, and Digital Forensics.

### 1.2 The Problem

Despite the club's technical ambitions, its operational infrastructure is entirely informal:

| Domain | Current State | Consequence |
|---|---|---|
| **Communication** | Discord channels | Announcements are lost in message history within 24–48 hours |
| **Events** | Manually pinned Discord messages | No registration, no waitlist, no attendance record |
| **Resources** | Shared Google Drive folders | No discoverability, no categorization, no search |
| **Members** | Spreadsheet | No profile, no XP, no history, no identity |
| **Administration** | Verbal coordination | No audit trail, no accountability, no tooling |
| **Participation** | Memory-based | No way to know who attended which event |

When the club hosts events like **CICONIX** (a 55-hour multi-track event open to all ENSS students), coordinating 100+ participants through Discord alone becomes genuinely painful.

### 1.3 The Solution

A **native Android application** that acts as the single source of truth for everything CIC-related, built offline-first so it works with or without server connectivity.

```
Before:  Discord messages + Drive folders + spreadsheets
After:   One app. Everything in one place. Works offline.
```

| Feature | Before | After |
|---|---|---|
| Events | Pinned Discord message | Browsable feed, filter by type/difficulty, register with one tap |
| Resources | Google Drive folder link | Searchable hub, 20 resources, category chips, type badges |
| Announcements | Lost in chat | Persistent, prioritized (5 levels), pinnable, type-tagged |
| Members | Spreadsheet row | Full profile: avatar, XP bar, role badge, Discord link |
| Administration | Verbal | Admin panel with 5 modules, audit log with severity levels |
| Registration | "React with ✅" | In-app toggle with offline support, persisted to Room |

### 1.4 Vision

The objective is not merely to create a mobile application. The objective is to create:

> The digital infrastructure of the Cyber Innovators Club.

Long-term, the platform evolves into:

- A **cybersecurity learning ecosystem** with structured learning paths
- A **competitive CTF infrastructure** with team formation and scoreboarding
- A **mentorship system** connecting senior members with newcomers
- A **community identity layer** where every member has a verifiable technical profile
- Potentially a **university-wide platform** open to all ENSS students

---

## 2. System Architecture

### 2.1 The Three-Tier Architecture

```
╔════════════════════════════════════════════════════════════════╗
║                    PRESENTATION LAYER                         ║
║                                                               ║
║   SplashActivity → LoginActivity → MainActivity               ║
║         │                               │                     ║
║         │                    ┌──────────┴──────────┐          ║
║         │                    │                     │          ║
║         │              HomeFragment          ProfileFragment   ║
║         │              (events, resources,   (identity, XP,   ║
║         │               announcements)        history)        ║
║         │                                                     ║
║         │              AdminActivity                          ║
║         │              (Dashboard | Events | Users |          ║
║         │               Announcements | Audit)                ║
╚═══════════════════════════════╦════════════════════════════════╝
                                ║  observe / call
╔═══════════════════════════════╩════════════════════════════════╗
║                     VIEWMODEL LAYER                           ║
║                                                               ║
║   LoginViewModel  HomeViewModel  EventDetailViewModel          ║
║   AdminViewModel  ProfileViewModel                            ║
║                                                               ║
║   Hold: LiveData<T>  |  Call: repository.doX()               ║
║   Survive: screen rotation  |  No: Android imports           ║
╚═══════════════════════════════╦════════════════════════════════╝
                                ║  read / write
╔═══════════════════════════════╩════════════════════════════════╗
║                     DATA LAYER                                ║
║                                                               ║
║  ┌─────────────────────────┐   ┌──────────────────────────┐  ║
║  │     ROOM (Local DB)      │   │    RETROFIT (Remote API)  │  ║
║  │                         │   │                          │  ║
║  │  cic_database (v3)      │   │  ApiService interface    │  ║
║  │  ├── users              │   │  RetrofitClient          │  ║
║  │  ├── events             │   │  (OkHttp + Gson)         │  ║
║  │  ├── resources          │   │                          │  ║
║  │  ├── announcements      │   │  Base URL:               │  ║
║  │  └── event_registrations│   │  192.168.121.1:3000/api/ │  ║
║  └─────────────────────────┘   └──────────────────────────┘  ║
╚════════════════════════════════════════════════════════════════╝
```

### 2.2 Data Flow — Read Path

```
Fragment observes LiveData
        │
        │  (registered in onViewCreated)
        ▼
ViewModel exposes LiveData<T>
        │
        │  (backed by Room or Transformations.map)
        ▼
Repository returns eventDao.getAll()
        │
        │  Room query returns LiveData<List<EventEntity>>
        ▼
SQLite fires change notification
        │
        │  whenever any row in `events` table changes
        ▼
Fragment receives the updated list → adapter.submitList(list)
```

No manual refresh. No explicit "reload" button needed. The Fragment always shows whatever is in Room.

### 2.3 Data Flow — Write Path

```
User taps "Register" button
        │
        ▼
EventDetailActivity.btnRegister.setOnClickListener
        │
        ▼
EventDetailViewModel.toggleRegistration(event)
        │
        ▼
EventRepository.registerForEvent(id, onSuccess, onError)
        │
        ├──► Room write (immediate, background thread)
        │         │
        │         └──► eventDao.upsert(ev)   ← sets isRegistered=true, count++
        │                   │
        │                   └──► Room fires LiveData notification
        │                             │
        │                             └──► Fragment sees updated event
        │                                  Button changes to "Unregister"
        │
        └──► Retrofit call (async, best-effort)
                  ├── Success: Room already has it, nothing extra needed
                  └── Failure: Room still has it, user sees no error
```

### 2.4 Why This Architecture?

| Decision | Alternatives Considered | Reason for Choice |
|---|---|---|
| MVVM | MVC, MVP | ViewModel survives rotation; LiveData auto-manages lifecycle |
| Room over raw SQLite | SQLite directly | Type-safe queries, auto-generated DAO boilerplate, LiveData integration |
| Retrofit over OkHttp directly | Volley, HttpURLConnection | Declarative endpoints, automatic JSON parsing, easy mock |
| Offline-first over online-first | API-only, no local DB | App must work without backend; seed data makes it useful on day 1 |
| Single-thread executor | AsyncTask (deprecated), RxJava | Simple, predictable, no dependency overhead |
| ExecutorService over Coroutines | Kotlin Coroutines | Project is in Java; coroutines require Kotlin |

---

## 3. OOP Modeling

### 3.1 Abstraction

**Definition:** Hide implementation details, expose only what the caller needs to know.

In this project, abstraction operates at three levels:

**Level 1 — DAO abstracts SQL:**

```java
// The caller never writes SQL. They call a method.
@Dao
public interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC")
    LiveData<List<EventEntity>> getAll();

    @Query("SELECT * FROM events WHERE id = :id")
    EventEntity getByIdSync(String id);

    @Upsert
    void upsert(EventEntity event);
}
// Room generates the SQL implementation at compile time.
// The caller never sees: "UPDATE events SET isRegistered = 1 WHERE id = ?"
```

**Level 2 — Repository abstracts the data source:**

```java
// ViewModel doesn't know if data is from Room or network.
public class EventDetailViewModel extends AndroidViewModel {
    private final EventRepository repository;

    public LiveData<EventEntity> getEvent(String id) {
        if (event == null) {
            event = repository.getEvent(id);        // abstracted — could be Room, cache, network
            repository.refreshFromApi(null, null);  // abstracted — Retrofit call, background thread
        }
        return event;
    }
}
```

**Level 3 — ViewModel abstracts business logic from View:**

```java
// Fragment doesn't know what "toggle" means. It just calls toggle.
btnRegister.setOnClickListener(v -> {
    if (currentEvent != null) viewModel.toggleRegistration(currentEvent);
});

// The ViewModel knows the logic:
public void toggleRegistration(EventEntity e) {
    if (e.isRegistered) {
        repository.unregisterFromEvent(e.id, ...);
    } else {
        repository.registerForEvent(e.id, ...);
    }
}
```

### 3.2 Encapsulation

**Definition:** Bundle data and the methods that operate on it. Protect internal state.

The `EventRepository` fully encapsulates the write path for registration:

```java
public class EventRepository {

    // All internal infrastructure is private — no caller can touch it
    private final EventDao        eventDao;
    private final ApiService      apiService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Public interface: what callers are allowed to express
    public void registerForEvent(String eventId, Runnable onSuccess, Runnable onError) {
        apiService.registerForEvent(eventId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    applyLocalRegistration(eventId, true);  // private
                    onSuccess.run();
                } else {
                    onError.run();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                applyLocalRegistration(eventId, true);      // private — caller doesn't see this
                onSuccess.run();  // caller gets success regardless of API availability
            }
        });
    }

    // Private — internal mechanism, invisible to callers
    private void applyLocalRegistration(String eventId, boolean register) {
        executor.execute(() -> {
            EventEntity ev = eventDao.getByIdSync(eventId);
            if (ev == null) return;
            if (register && !ev.isRegistered) {
                ev.isRegistered    = true;
                ev.registeredCount = ev.registeredCount + 1;
            } else if (!register && ev.isRegistered) {
                ev.isRegistered    = false;
                ev.registeredCount = Math.max(0, ev.registeredCount - 1);  // floor at 0
            }
            eventDao.upsert(ev);
        });
    }
}
```

The `AuditEntry` class encapsulates severity derivation — callers provide an action, the class decides the severity:

```java
public class AuditEntry {
    public final String action;
    public final String severity;  // derived, not settable by caller

    public AuditEntry(String action, String entity, String detail) {
        this.action   = action;
        this.severity = deriveSeverity(action);  // encapsulated rule
        // timestamp also auto-generated — caller can't forge it
        Date now  = new Date();
        this.time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now);
        this.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
    }

    // Private: the mapping rule is owned by this class, not the caller
    private static String deriveSeverity(String action) {
        switch (action != null ? action : "") {
            case BAN:
            case MUTE:
            case SUSPEND: return CRITICAL;
            case DELETE:
            case WARN:
            case CANCEL:
            case ROLE:    return WARNING;
            default:      return INFO;
        }
    }
}
```

### 3.3 Inheritance

**Definition:** A class acquires properties and behavior from a parent class, extending or specializing them.

**Framework inheritance — required by Android:**

```java
// CICApplication extends Application to intercept the app lifecycle
public class CICApplication extends Application {
    private static CICApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();           // must call super — framework requires it
        instance = this;
        RetrofitClient.init(this);  // added behavior: init Retrofit on launch
        seedIfEmpty();              // added behavior: seed Room on first launch
    }

    // Static accessor — added behavior not in Application
    public static CICApplication getInstance() { return instance; }
}
```

```java
// ViewModels extend AndroidViewModel to get Application context
public class EventDetailViewModel extends AndroidViewModel {
    // Inherited: getApplication(), onCleared(), hasActiveObservers()
    // Added: event, toast, registered LiveData fields and business methods

    public EventDetailViewModel(@NonNull Application app) {
        super(app);   // AndroidViewModel stores the Application reference
        repository = new EventRepository(app);
    }
}
```

**Adapter inheritance — specializing RecyclerView.Adapter:**

```java
// ParticipantAdapter extends RecyclerView.Adapter<VH>
// Inherited contract: getItemCount(), onCreateViewHolder(), onBindViewHolder()
// Added: participant-specific bind logic, status cycle, remove callback
public class ParticipantAdapter
        extends RecyclerView.Adapter<ParticipantAdapter.VH> {

    static class VH extends RecyclerView.ViewHolder {
        // Inherited: itemView
        // Added: typed references to the specific layout's views
        ImageView  avatar;
        TextView   name, role, statusBadge;
        ImageButton attend, remove;
        VH(View v) { super(v); /* bind typed views */ }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                   .inflate(R.layout.item_participant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {
        // Specialized binding for EventRegistrationEntity data
    }
}
```

**Database inheritance — Room requires abstract class:**

```java
public abstract class AppDatabase extends RoomDatabase {
    // RoomDatabase provides: open(), close(), query(), beginTransaction()
    // We add: the 5 DAO accessors
    public abstract EventDao             eventDao();
    public abstract UserDao              userDao();
    public abstract ResourceDao          resourceDao();
    public abstract AnnouncementDao      announcementDao();
    public abstract EventRegistrationDao eventRegistrationDao();
    // Room generates implementations of all 5 methods at compile time
}
```

### 3.4 Polymorphism

**Definition:** The same interface, method name, or code handles different types at runtime.

**Callback polymorphism — same pattern, different behaviors:**

Every async repository operation uses the same two-callback signature. The caller defines the behavior; the repository calls whichever is appropriate:

```java
// Same signature for every async operation
repository.registerForEvent(id,
    () -> toast.postValue("Registered for " + e.title + "!"),   // onSuccess
    () -> toast.postValue("Registration failed — try again"));   // onError

repository.unregisterFromEvent(id,
    () -> toast.postValue("Unregistered from " + e.title),
    () -> toast.postValue("Failed to unregister — try again"));

repository.deleteEvent(id,
    () -> { /* remove from list */ },
    () -> showError("Could not delete event"));
```

**LiveData observation — same pattern for every entity:**

```java
// The Observer<T> interface is polymorphic over T
viewModel.getEvents().observe(this,
    events -> eventAdapter.submitList(events));   // events = List<EventDto>

viewModel.getUsers().observe(this,
    users -> userAdapter.submitList(users));       // users = List<UserDto>

viewModel.getAnnouncements().observe(this,
    ann -> announcementAdapter.submitList(ann));   // ann = List<AnnouncementDto>

// The .observe() call is identical in all three cases.
// Java generics + the Observer<T> interface make this work.
```

**Status cycle polymorphism in participant management:**

```java
// The same toggle method produces different next states depending on current state
private String nextStatus(String current) {
    switch (current) {
        case "REGISTERED": return "ATTENDING";
        case "ATTENDING":  return "ABSENT";
        case "ABSENT":     return "REGISTERED";
        default:           return "REGISTERED";
    }
}
// Called with different inputs → returns different outputs
// This is ad-hoc (switch-based) polymorphism
```

### 3.5 Modularity

The project is organized into packages with clear boundaries and one-way dependencies:

```
com.cic.mobapp                         (package root)
│
├── data/                              ← Data layer. No Android UI imports.
│   ├── local/
│   │   ├── AppDatabase.java           ← Singleton Room instance
│   │   ├── SeedData.java              ← Static factory for initial data
│   │   ├── dao/                       ← 5 DAO interfaces (Room generates SQL)
│   │   │   ├── EventDao.java
│   │   │   ├── UserDao.java
│   │   │   ├── ResourceDao.java
│   │   │   ├── AnnouncementDao.java
│   │   │   └── EventRegistrationDao.java
│   │   └── entity/                    ← 5 Room entities (SQLite table definitions)
│   │       ├── EventEntity.java
│   │       ├── UserEntity.java
│   │       ├── ResourceEntity.java
│   │       ├── AnnouncementEntity.java
│   │       └── EventRegistrationEntity.java
│   ├── remote/
│   │   ├── ApiService.java            ← 30 Retrofit endpoint declarations
│   │   ├── RetrofitClient.java        ← Singleton OkHttp + Retrofit builder
│   │   └── dto/                       ← 5 DTOs (API response/request shapes)
│   │       ├── EventDto.java
│   │       ├── UserDto.java
│   │       ├── ResourceDto.java
│   │       ├── AnnouncementDto.java
│   │       └── AuthDto.java
│   └── repository/
│       ├── EventRepository.java       ← Mediates Room ↔ API for events
│       ├── UserRepository.java
│       ├── ResourceRepository.java
│       └── AnnouncementRepository.java
│
├── ui/                                ← Presentation layer. Depends on data/. Never the reverse.
│   ├── auth/                          ← Login, Registration
│   │   ├── LoginActivity.java
│   │   ├── LoginFragment.java
│   │   ├── RegisterFragment.java
│   │   └── LoginViewModel.java
│   ├── home/                          ← Home feed (events + resources + announcements)
│   │   ├── HomeFragment.java
│   │   └── HomeViewModel.java
│   ├── events/                        ← Event list + detail
│   │   ├── EventDetailActivity.java
│   │   └── EventDetailViewModel.java
│   ├── profile/                       ← User profile
│   │   ├── ProfileFragment.java
│   │   └── ProfileViewModel.java
│   └── admin/                         ← Admin panel (5 tabs)
│       ├── AdminActivity.java
│       ├── AdminViewModel.java        ← Shared ViewModel for all admin tabs
│       ├── AdminDashboardFragment.java
│       ├── AdminEventsFragment.java
│       ├── AdminUsersFragment.java
│       ├── AdminAnnouncementsFragment.java
│       ├── AdminAuditFragment.java
│       └── AuditEntry.java
│
├── util/
│   ├── TokenManager.java              ← Secure token storage + retrieval
│   └── ReadTracker.java               ← Track which announcements the user has read
│
└── CICApplication.java                ← App entry point, seeds Room
```

**Dependency rule:** `ui/` imports from `data/`. `data/` never imports from `ui/`. Circular dependencies are architecturally impossible.

### 3.6 Reusability

Components written once and reused across the entire application:

| Component | Used By |
|---|---|
| `AppDatabase.getInstance()` | All 4 repositories + AdminViewModel + HomeViewModel |
| `RetrofitClient.getApiService()` | All 4 repositories + LoginViewModel |
| `TokenManager` | LoginViewModel, ProfileViewModel, MainActivity, AdminViewModel |
| `SeedData` | CICApplication (one-time seed on launch) |
| `AuditEntry` | Every admin action in AdminEventsFragment, AdminUsersFragment, AdminAnnouncementsFragment |
| `EventEntity` | EventDao, EventRepository, EventDetailViewModel, AdminViewModel, SeedData |
| `Transformations.map()` | HomeViewModel (announcements), AdminViewModel (announcements) |
| `item_event_card.xml` | HomeFragment carousel + EventsFragment list |
| `item_participant.xml` | ParticipantAdapter in AdminEventsFragment |

### 3.7 Maintainability

**Single point of change for seed data:**

All 21 events, 54 users, 20 resources, and 6 announcements are in one file: `SeedData.java`. When the club schedule changes, one file changes.

**Schema evolution is safe:**

```java
// AppDatabase.java
.fallbackToDestructiveMigration()
// During development, schema changes are common.
// This setting drops and recreates the DB instead of crashing.
// In production, real migrations would replace this.
```

**LiveData eliminates manual refresh:**

```java
// Wrong (imperative, requires manual refresh):
List<Event> events = repository.getEventsSync();
adapter.setData(events);
// Later: something changes → you must remember to refresh

// Correct (reactive, auto-refreshes):
repository.getEventsLiveData().observe(this, events -> adapter.submitList(events));
// Room changes → LiveData fires → UI updates. No manual refresh ever needed.
```

**Callback-based async is always explicit:**

Every async operation has a named success and failure path. There are no silent failures:

```java
repository.deleteEvent(id,
    () -> {                                    // success: explicit
        viewModel.loadEventsFromRoom();
        viewModel.auditAction(AuditEntry.DELETE, "EVENT", title);
    },
    () -> viewModel.showToast("Delete failed") // failure: explicit
);
```

---

## 4. Design Patterns

### 4.1 MVVM (Model-View-ViewModel)

MVVM is the primary architectural pattern, enforced by Android's Architecture Components.

**The full triangle:**

```
┌─────────────────────────────────────────────────────────────┐
│ View (Fragment / Activity)                                   │
│  • Renders LiveData                                         │
│  • Forwards user events to ViewModel                        │
│  • Zero business logic                                      │
└───────────────────┬─────────────────────────────────────────┘
    observe()       │      calls (toggle, delete, refresh...)
                    ▼
┌─────────────────────────────────────────────────────────────┐
│ ViewModel                                                   │
│  • Holds LiveData<T> for every piece of UI state           │
│  • Survives screen rotation (stored by ViewModelStore)     │
│  • No Android View imports (testable in isolation)         │
│  • Calls Repository for data operations                    │
└───────────────────┬─────────────────────────────────────────┘
    getAll()        │      upsert(), deleteById(), ...
                    ▼
┌─────────────────────────────────────────────────────────────┐
│ Model (Repository + Room + Retrofit)                        │
│  • Room: persistent local state, drives LiveData           │
│  • Retrofit: best-effort remote sync                       │
│  • Repository: coordinates the two                        │
└─────────────────────────────────────────────────────────────┘
```

**Concrete example — event detail screen:**

```java
// View (EventDetailActivity): purely reactive
viewModel.getEvent(eventId).observe(this, event -> {
    if (event == null) return;
    currentEvent = event;
    tvTitle.setText(event.title);
    tvDate.setText(event.date);
    tvLocation.setText(event.location);
    tvDifficulty.setText(event.difficulty);
    tvCapacity.setText(event.registeredCount + " / " + event.capacity);
    btnRegister.setText(event.isRegistered ? "Unregister" : "Register");
    Glide.with(this).load(event.bannerUrl).into(imgBanner);
});

viewModel.getToast().observe(this, msg -> {
    if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
});

btnRegister.setOnClickListener(v -> {
    if (currentEvent != null) viewModel.toggleRegistration(currentEvent);
    // No UI logic here — ViewModel decides what happens
});
```

```java
// ViewModel: holds state, defines behavior
public class EventDetailViewModel extends AndroidViewModel {
    private final EventRepository      repository;
    private       LiveData<EventEntity> event;
    private final MutableLiveData<String>  toast      = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registered = new MutableLiveData<>(false);

    public LiveData<EventEntity> getEvent(String id) {
        if (event == null) {
            event = repository.getEvent(id);
            repository.refreshFromApi(null, null);
        }
        return event;
    }

    public void toggleRegistration(EventEntity e) {
        if (e.isRegistered) {
            repository.unregisterFromEvent(e.id,
                    () -> toast.postValue("Unregistered from " + e.title),
                    () -> toast.postValue("Failed to unregister — try again"));
        } else {
            repository.registerForEvent(e.id,
                    () -> toast.postValue("Registered for " + e.title + "!"),
                    () -> toast.postValue("Registration failed — try again"));
        }
    }
}
```

### 4.2 Repository Pattern

A dedicated class mediates between the ViewModel and all data sources. The ViewModel expresses intent; the Repository decides how to fulfill it.

```
ViewModel.getEvents()
    │
    └── EventRepository
            │
            ├── return eventDao.getAll()      ← always Room for LiveData
            │
            └── apiService.getEvents().enqueue(...)  ← background sync
                    ├── success: eventDao.upsertAll(entities)
                    └── failure: keep what Room already has
```

**Why separate the Repository from the ViewModel?**

1. **Testability**: The ViewModel can be tested with a mock Repository
2. **Reusability**: HomeFragment, EventDetailActivity, and AdminViewModel all use the same `EventRepository` — no duplicate network code
3. **Separation of concerns**: Retry logic, caching, and sync logic belong in the Repository, not in 5 different ViewModels

### 4.3 Observer Pattern

Implemented via Android's `LiveData`. The View registers interest in data. When the data changes (from any source — API refresh, admin edit, user action), all observers are notified automatically.

```java
// Registration: Fragment tells LiveData "notify me when data changes"
viewModel.getAnnouncements().observe(getViewLifecycleOwner(), announcements -> {
    announcementAdapter.submitList(announcements);
    tvAnnouncementCount.setText(String.valueOf(announcements.size()));
});

// Notification happens automatically:
// 1. Admin pins an announcement → announcementDao.upsert(entity)
// 2. Room detects the table change
// 3. LiveData fires onChange()
// 4. All active observers are called with the new list
// 5. HomeFragment and AdminAnnouncementsFragment both update — simultaneously
```

**Key advantage:** There is no publish/subscribe management code in this project. No `eventBus.post()`, no `notifyListeners()`, no manual list of observers. Room + LiveData handle it automatically.

### 4.4 Singleton Pattern

`AppDatabase` and `RetrofitClient` are singletons — created once, reused everywhere.

```java
// AppDatabase — thread-safe double-checked locking
public static AppDatabase getInstance(Context context) {
    if (INSTANCE == null) {
        synchronized (AppDatabase.class) {
            // Second null check inside synchronized block:
            // Thread A and Thread B both pass the first check simultaneously.
            // Thread A acquires the lock, creates the instance, releases the lock.
            // Thread B acquires the lock, sees INSTANCE != null, skips creation.
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),  // Application context — never Activity
                        AppDatabase.class,
                        "cic_database"
                ).fallbackToDestructiveMigration().build();
            }
        }
    }
    return INSTANCE;
}
```

`volatile` ensures that the write to `INSTANCE` is immediately visible to all threads. Without `volatile`, a thread might see a partially-constructed object due to CPU instruction reordering.

**CICApplication as application-scoped singleton:**

```java
public class CICApplication extends Application {
    private static CICApplication instance;

    @Override public void onCreate() {
        super.onCreate();
        instance = this;  // set once, on application start
    }

    public static CICApplication getInstance() { return instance; }
}
```

### 4.5 Factory Method Pattern (SeedData)

`SeedData` is a static factory that produces fully-initialized domain objects. It centralizes object construction and hides the verbosity of field-by-field initialization.

```java
public final class SeedData {
    private SeedData() {}  // non-instantiable — pure factory

    // Factory method for EventEntity
    private static EventEntity e(String id, String title, String description,
                                  String location, String date, String type,
                                  String difficulty, int capacity, String bannerUrl) {
        EventEntity ev = new EventEntity();
        ev.id          = id;
        ev.title       = title;
        ev.description = description;
        ev.location    = location;
        ev.date        = date;
        ev.type        = type;
        ev.difficulty  = difficulty;
        ev.capacity    = capacity;
        ev.bannerUrl   = bannerUrl;
        return ev;
    }

    // Factory method for UserEntity with auto-derived fields
    private static UserEntity u(String id, String username, String email,
                                 String discordId, String role, int xp, String joined) {
        UserEntity e = new UserEntity();
        e.id        = id;
        e.username  = username;
        e.email     = email.isEmpty() ? null : email;
        e.discordId = discordId.isEmpty() ? null : discordId;
        e.role      = role;
        e.xp        = xp;
        e.level     = xp / 1000 + 1;          // derived field — factory computes it
        e.createdAt = joined;
        // Avatar URL auto-generated from first name — no manual URL management
        String firstName = username.split(" ")[0];
        e.avatarUrl = "https://api.dicebear.com/7.x/pixel-art/png?seed="
                    + firstName + "&size=128";
        return e;
    }
}
```

Callers never see `new UserEntity(); e.field = value; ...` for 54 users. They see a declarative list.

### 4.6 Strategy Pattern (Sort / Filter)

Admin lists implement sort and filter as interchangeable strategies applied to an in-memory list. The sorting algorithm is selected at runtime based on the user's chip selection:

```java
// Sort strategy selected at runtime
private List<EventDto> applySortAndFilter(List<EventDto> all) {
    List<EventDto> filtered = applyFilter(all);
    switch (currentSort) {
        case DATE_ASC:    filtered.sort((a, b) -> a.date.compareTo(b.date));       break;
        case DATE_DESC:   filtered.sort((a, b) -> b.date.compareTo(a.date));       break;
        case NAME_AZ:     filtered.sort((a, b) -> a.title.compareTo(b.title));     break;
        case FILL_RATE:   filtered.sort((a, b) -> fillRate(b) - fillRate(a));      break;
        case CAPACITY:    filtered.sort((a, b) -> b.capacity - a.capacity);        break;
    }
    return filtered;
}
```

Each case is a different strategy. Swapping sort modes requires only changing `currentSort` — no structural changes.

---

## 5. SOLID Principles

### S — Single Responsibility Principle

*A class should have only one reason to change.*

| Class | Its Single Reason to Change |
|---|---|
| `EventDao` | The SQL schema for `events` changes |
| `EventRepository` | The coordination logic between Room and API changes |
| `EventDetailViewModel` | The business logic of the event detail screen changes |
| `EventDetailActivity` | The UI layout of the event detail screen changes |
| `SeedData` | The club's initial data changes |
| `AuditEntry` | The definition of an audit record changes |
| `TokenManager` | How tokens are stored or validated changes |
| `RetrofitClient` | How the HTTP client is configured changes |
| `CICApplication` | What happens at application startup changes |

Contrast with a violation: if `EventDetailActivity` contained the registration logic (network call, Room write, error handling), it would have multiple reasons to change: UI change, network logic change, and DB write change. MVVM + Repository prevents this.

### O — Open/Closed Principle

*Open for extension, closed for modification.*

**AuditEntry action constants** are open for extension. Adding a new action type (`PROMOTE`, `DEMOTE`, `RESET`) requires adding a constant and a case in `deriveSeverity()`. No existing calling code changes:

```java
// Existing calls — unchanged
audit(AuditEntry.BAN, "USER", userName);
audit(AuditEntry.DELETE, "EVENT", eventTitle);
audit(AuditEntry.CREATE, "ANNOUNCEMENT", annTitle);

// Extension: add new action
public static final String PROMOTE = "ROLE PROMOTED";
// Add one case to deriveSeverity — nothing else changes:
case PROMOTE: return WARNING;
```

**SeedData** is open for extension — add new events to the list without touching any other code.

**ApiService** is open for extension — add new endpoints as new method declarations. Retrofit generates implementations. Existing endpoints are unchanged.

### L — Liskov Substitution Principle

*Subtypes must be substitutable for their base types.*

Every `RecyclerView.Adapter<VH>` subclass honors the full adapter contract. They all implement `getItemCount()`, `onCreateViewHolder()`, and `onBindViewHolder()` correctly. A `RecyclerView` configured with any of them works correctly:

```java
// This code works identically regardless of which adapter subclass is assigned:
recyclerView.setAdapter(new EventAdapter(clickListener));
recyclerView.setAdapter(new ResourceAdapter(clickListener));
recyclerView.setAdapter(new ParticipantAdapter(eventId, admViewModel));
// All respect the RecyclerView.Adapter contract fully
```

`AndroidViewModel` subclasses all honor the ViewModel lifecycle contract: they survive rotation, they release resources in `onCleared()`, they don't hold Activity references.

### I — Interface Segregation Principle

*Clients should not be forced to depend on methods they do not use.*

Room DAOs are narrow, purpose-specific interfaces. `EventRegistrationDao` doesn't expose user queries. `AnnouncementDao` doesn't expose event queries. Each DAO is as small as possible:

```java
// EventRegistrationDao — only registration-relevant operations
@Dao public interface EventRegistrationDao {
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
// 7 methods. All registration-specific. Nothing else.
```

Compare: if this were one giant `DatabaseDao` with 50 methods, every class that needs to query registrations would depend on the entire interface, including user-management and event-creation methods it never calls.

### D — Dependency Inversion Principle

*High-level modules should not depend on low-level modules. Both should depend on abstractions.*

```
High-level: AdminViewModel (orchestrates admin operations)
    │
    │  depends on
    ▼
Abstraction: AppDatabase (abstract class with abstract DAO methods)
             ApiService (interface — Retrofit implements it)
    │
    │  not on
    ▼
Low-level: SQLiteOpenHelper, OkHttpClient, specific SQL strings
```

`AdminViewModel` calls `db.eventDao().upsertAll(entities)`. It does not call `db.openHelper.getWritableDatabase().execSQL(...)`. The high-level module speaks to an abstraction; the low-level SQLite details are hidden inside Room.

Similarly, `EventRepository` calls `apiService.registerForEvent(id)` — it does not construct an HTTP request. Retrofit implements the `ApiService` interface; the ViewModel is insulated from transport-level details.

---

## 6. UML Diagrams

### 6.1 Entity-Relationship Diagram (Complete)

```
┌──────────────────────────────────┐
│         users                    │
├──────────────────────────────────┤
│ id (PK)         VARCHAR          │
│ username        VARCHAR          │
│ email           VARCHAR NULLABLE │
│ discordId       VARCHAR NULLABLE │
│ role            VARCHAR          │ ◄── Guest|Member|Mentor|Organizer|Administrator
│ xp              INTEGER          │
│ level           INTEGER          │ ◄── derived: xp/1000+1
│ avatarUrl       VARCHAR          │
│ createdAt       VARCHAR          │
│ isBanned        INTEGER (0/1)    │
└──────────────────┬───────────────┘
                   │ 1
                   │ registers
                   │ N
┌──────────────────▼───────────────┐      ┌──────────────────────────┐
│     event_registrations          │      │         events            │
├──────────────────────────────────┤      ├──────────────────────────┤
│ id (PK, autoGen) INTEGER         │      │ id (PK)       VARCHAR    │
│ userId  (FK→users.id)  VARCHAR   │      │ title         VARCHAR    │
│ eventId (FK→events.id) VARCHAR   │◄─────│ description   VARCHAR    │
│ username          VARCHAR        │      │ bannerUrl     VARCHAR    │
│ avatarUrl         VARCHAR        │      │ location      VARCHAR    │
│ userRole          VARCHAR        │      │ date          VARCHAR    │
│ registeredAt      VARCHAR        │      │ type          VARCHAR    │ ◄── Workshop|CTF|Event
│ status            VARCHAR        │      │ difficulty    VARCHAR    │ ◄── Beginner|Intermediate|Advanced
│                                  │      │ capacity      INTEGER    │
│ UNIQUE INDEX (eventId, userId)   │      │ registeredCount INTEGER  │
└──────────────────────────────────┘      │ isRegistered  INTEGER   │
                                          │ organizerId   VARCHAR    │
                                          └──────────────────────────┘

┌──────────────────────────────────┐      ┌──────────────────────────┐
│         resources                │      │      announcements        │
├──────────────────────────────────┤      ├──────────────────────────┤
│ id (PK)         VARCHAR          │      │ id (PK)       VARCHAR    │
│ title           VARCHAR          │      │ title         VARCHAR    │
│ description     VARCHAR          │      │ body          VARCHAR    │
│ category        VARCHAR          │      │ type          VARCHAR    │ ◄── 9 types
│ type            VARCHAR          │ ◄──  │ priority      VARCHAR    │ ◄── Low|Normal|Important|Critical|Emergency
│ difficulty      VARCHAR          │  Lab │ isPinned      INTEGER    │
│ url             VARCHAR          │  PDF │ createdAt     VARCHAR    │
│ uploaderId      VARCHAR          │  Vid └──────────────────────────┘
└──────────────────────────────────┘  Sli
                                      Doc
```

### 6.2 Class Diagram — Data Layer

```
«abstract»
RoomDatabase
    └── AppDatabase                         «singleton»
            │ getInstance(ctx): AppDatabase
            ├── eventDao():             EventDao
            ├── userDao():              UserDao
            ├── resourceDao():          ResourceDao
            ├── announcementDao():      AnnouncementDao
            └── eventRegistrationDao(): EventRegistrationDao

«interface»               «interface»
EventDao                  EventRegistrationDao
  getAll(): LiveData        register(reg)
  getById(): LiveData       getParticipants(eventId): List
  getByIdSync(): Entity     countParticipants(eventId): int
  upsertAll(list)           getParticipantIds(eventId): List
  upsert(entity)            updateStatus(eventId, userId, status)
  deleteById(id)            unregister(eventId, userId)
  countAll(): int           clearEvent(eventId)
  countById(): int

EventRepository
  - eventDao: EventDao
  - apiService: ApiService
  - executor: ExecutorService
  + getEventsLiveData(): LiveData<List<EventEntity>>
  + getEvent(id): LiveData<EventEntity>
  + refreshFromApi(onSuccess, onError)
  + registerForEvent(id, onSuccess, onError)
  + unregisterFromEvent(id, onSuccess, onError)
  - applyLocalRegistration(id, register)
  - toEntity(dto): EventEntity
```

### 6.3 Class Diagram — ViewModel Layer

```
AndroidViewModel
├── EventDetailViewModel
│     - repository: EventRepository
│     - event: LiveData<EventEntity>
│     - toast: MutableLiveData<String>
│     - registered: MutableLiveData<Boolean>
│     + getEvent(id): LiveData<EventEntity>
│     + toggleRegistration(e: EventEntity)
│     + getToast(): LiveData<String>
│
├── HomeViewModel
│     - eventRepo: EventRepository
│     - resourceRepo: ResourceRepository
│     - db: AppDatabase
│     + getEvents(): LiveData<List<EventDto>>
│     + getResources(): LiveData<List<ResourceDto>>
│     + getAnnouncements(): LiveData<List<AnnouncementDto>>
│
├── LoginViewModel
│     - tokenManager: TokenManager
│     - apiService: ApiService
│     + loginWithEmail(email, password)
│     + register(username, email, password, confirm)
│     + handleDiscordCallback(code)
│     - startOfflineSession(hint)
│     + isAlreadyLoggedIn(): boolean
│
└── AdminViewModel
      - api: ApiService
      - db: AppDatabase
      - users: MutableLiveData<List<UserDto>>
      - events: MutableLiveData<List<EventDto>>
      - resources: MutableLiveData<List<ResourceDto>>
      - announcements: LiveData<List<AnnouncementDto>>   ← Transformations.map
      - auditLog: MutableLiveData<List<AuditEntry>>
      + refresh()
      + createEvent(dto) / updateEvent(dto) / deleteEvent(id, ...)
      + createAnnouncement(dto) / deleteAnnouncement(id, ...)
      + auditAction(action, entity, detail)
      + getParticipantsSync(eventId): List<EventRegistrationEntity>
      + registerParticipant(eventId, user)
      + unregisterParticipant(eventId, userId)
      + updateParticipantStatus(eventId, userId, status)
```

### 6.4 Sequence Diagram — Registration Flow (Full)

```
User      EventDetailActivity  EventDetailViewModel  EventRepository   EventDao     ApiService
 │               │                    │                    │              │              │
 │ tap Register  │                    │                    │              │              │
 │──────────────►│                    │                    │              │              │
 │               │ toggleRegistration │                    │              │              │
 │               │   (currentEvent)   │                    │              │              │
 │               │───────────────────►│                    │              │              │
 │               │                    │  registerForEvent  │              │              │
 │               │                    │  (id, ok, fail)    │              │              │
 │               │                    │───────────────────►│              │              │
 │               │                    │                    │  enqueue()   │              │
 │               │                    │                    │─────────────────────────────►│
 │               │                    │                    │              │              │
 │               │                    │                    │ applyLocal() │              │
 │               │                    │                    │──────────────►│              │
 │               │                    │                    │              │ upsert(ev)   │
 │               │                    │                    │              │─────         │
 │               │                    │                    │              │    │Room     │
 │               │                    │                    │              │◄────writes   │
 │               │                    │                    │              │              │
 │               │                    │  LiveData fires    │              │              │
 │               │◄───────────────────│◄───────────────────│◄─────────────│              │
 │               │ UI updates button  │                    │              │              │
 │◄──────────────│ to "Unregister"    │                    │              │   response   │
 │               │                    │                    │◄─────────────────────────────│
 │               │                    │                    │ (Room already has it — noop) │
```

### 6.5 Sequence Diagram — Offline Login

```
User      LoginActivity    LoginViewModel    ApiService    TokenManager    Room
 │               │                │               │              │          │
 │  enter email  │                │               │              │          │
 │  + password   │                │               │              │          │
 │──────────────►│                │               │              │          │
 │               │ loginWithEmail │               │              │          │
 │               │───────────────►│               │              │          │
 │               │                │  POST /auth   │              │          │
 │               │                │  /login       │              │          │
 │               │                │──────────────►│              │          │
 │               │                │  onFailure    │              │          │
 │               │                │  (no network) │              │          │
 │               │                │◄──────────────│              │          │
 │               │                │               │              │          │
 │               │                │ startOffline  │              │          │
 │               │                │  Session()    │              │          │
 │               │                │──────────────────────────────►│          │
 │               │                │               │ saveTokens(  │          │
 │               │                │               │ "offline",   │          │
 │               │                │               │ "refresh",   │          │
 │               │                │               │ "mem_036")   │          │
 │               │                │               │              │──writes──►│
 │               │                │ loginSuccess  │              │          │
 │               │                │ .postValue    │              │          │
 │               │                │    (true)     │              │          │
 │               │◄───────────────│               │              │          │
 │               │  navigate to   │               │              │          │
 │               │  MainActivity  │               │              │          │
 │◄──────────────│                │               │              │          │
 │ app opens     │                │               │              │          │
 │ (all 54 users,│                │               │              │          │
 │  21 events    │                │               │              │          │
 │  loaded from  │                │               │              │          │
 │  Room seed)   │                │               │              │          │
```

### 6.6 State Machine — Participant Status

```
         ┌─────────────────────────────────────────┐
         │            (admin toggles attend)        │
         ▼                                          │
    ┌──────────┐   attend tap   ┌──────────────┐    │
    │REGISTERED│───────────────►│  ATTENDING   │    │
    └──────────┘                └──────────────┘    │
         ▲                           │              │
         │                           │ attend tap   │
         │      attend tap           ▼              │
         │                     ┌──────────┐         │
         └─────────────────────│  ABSENT  │─────────┘
                               └──────────┘
                                    │
                               remove tap
                                    │
                                    ▼
                              (removed from
                               event_registrations)
```

---

## 7. Android Engineering

### 7.1 Technology Stack

| Layer | Technology | Version | Purpose |
| --- | --- | --- | --- |
| Language | Java | 17 | Primary application language |
| Minimum SDK | Android 8.0 (API 26) | — | `java.time.*` availability |
| Target SDK | Android 14 (API 34) | — | Latest APIs and security model |
| Architecture | MVVM | — | Lifecycle-aware state management |
| Local DB | Room | 2.6.x | Type-safe SQLite with LiveData |
| HTTP client | Retrofit 2 + OkHttp 4 | 2.9 / 4.x | Declarative REST client |
| JSON | Gson | 2.10 | DTO serialization / deserialization |
| Images | Glide | 4.16 | Async image loading, asset URI support |
| UI | Material Components | 1.11 | Dark theme, chips, cards, dialogs |
| Async | ExecutorService | JDK | Background thread management |

### 7.2 Room — Five Entities, Five DAOs

Room provides compile-time verification of SQL queries. If a query references a column that doesn't exist, the build fails — not the app at runtime.

```java
// AppDatabase — the single source of truth for all local persistence
@Database(
    entities = {
        UserEntity.class,
        EventEntity.class,
        ResourceEntity.class,
        AnnouncementEntity.class,
        EventRegistrationEntity.class
    },
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao              userDao();
    public abstract EventDao             eventDao();
    public abstract ResourceDao          resourceDao();
    public abstract AnnouncementDao      announcementDao();
    public abstract EventRegistrationDao eventRegistrationDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class, "cic_database")
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}
```

**Why version 3?** The database evolved three times during development:

- v1: Users, Events, Resources
- v2: Added Announcements
- v3: Added EventRegistrations (participant tracking)

### 7.3 Retrofit — 30 Declared Endpoints

The `ApiService` interface declares every backend endpoint in plain Java. Retrofit generates the HTTP implementation at runtime using reflection and `@Annotations`.

```java
public interface ApiService {

    // ── Authentication ─────────────────────────────────────────────────────
    @POST("auth/login")
    Call<AuthDto.AuthResponse> loginWithEmail(@Body AuthDto.EmailLoginRequest req);

    @POST("auth/register")
    Call<AuthDto.AuthResponse> register(@Body AuthDto.RegisterRequest req);

    @POST("auth/discord")
    Call<AuthDto.AuthResponse> loginWithDiscord(@Body AuthDto.DiscordLoginRequest req);

    @POST("auth/refresh")
    Call<AuthDto.AuthResponse> refreshToken(@Body AuthDto.RefreshRequest req);

    @POST("auth/logout")
    Call<Void> logout();

    // ── Users ───────────────────────────────────────────────────────────────
    @GET("users/me")         Call<UserDto> getMe();
    @GET("users/{id}")       Call<UserDto> getUser(@Path("id") String id);
    @PATCH("users/me")       Call<UserDto> updateMe(@Body UserDto user);
    @GET("users")            Call<List<UserDto>> getAllUsers();
    @PATCH("users/{id}")     Call<UserDto> updateUser(@Path("id") String id, @Body UserDto u);
    @DELETE("users/{id}")    Call<Void> deleteUser(@Path("id") String id);

    // ── Events ──────────────────────────────────────────────────────────────
    @GET("events")           Call<List<EventDto>> getEvents(
                                 @Query("type") String type,
                                 @Query("from") String from,
                                 @Query("to")   String to);
    @GET("events/{id}")      Call<EventDto> getEvent(@Path("id") String id);
    @POST("events")          Call<EventDto> createEvent(@Body EventDto event);
    @PATCH("events/{id}")    Call<EventDto> updateEvent(@Path("id") String id, @Body EventDto e);
    @DELETE("events/{id}")   Call<Void> deleteEvent(@Path("id") String id);
    @POST("events/{id}/register")   Call<Void> registerForEvent(@Path("id") String id);
    @POST("events/{id}/unregister") Call<Void> unregisterFromEvent(@Path("id") String id);

    // ── Resources ───────────────────────────────────────────────────────────
    @GET("resources")        Call<List<ResourceDto>> getResources(
                                 @Query("category")   String category,
                                 @Query("difficulty") String difficulty,
                                 @Query("q")          String query);
    @POST("resources")       Call<ResourceDto> uploadResource(@Body ResourceDto r);
    @PATCH("resources/{id}") Call<ResourceDto> updateResource(@Path("id") String id, @Body ResourceDto r);
    @DELETE("resources/{id}")Call<Void> deleteResource(@Path("id") String id);

    // ── Announcements ───────────────────────────────────────────────────────
    @GET("announcements")       Call<List<AnnouncementDto>> getAnnouncements(@Query("pinned") Boolean pinned);
    @POST("announcements")      Call<AnnouncementDto> createAnnouncement(@Body AnnouncementDto a);
    @PATCH("announcements/{id}")Call<AnnouncementDto> updateAnnouncement(@Path("id") String id, @Body AnnouncementDto a);
    @DELETE("announcements/{id}")Call<Void> deleteAnnouncement(@Path("id") String id);

    // ── Admin ───────────────────────────────────────────────────────────────
    @GET("admin/stats")      Call<Map<String, Integer>> getAdminStats();
}
```

### 7.4 Glide — Images from Three Sources

The app loads images from three different URI schemes, all handled by the same Glide call:

```java
Glide.with(context).load(url).into(imageView);
```

| URI Scheme | Example | Source |
|---|---|---|
| `file:///android_asset/...` | `file:///android_asset/event_banners/dsc01412.jpg` | App assets bundle |
| `https://api.dicebear.com/...` | `https://api.dicebear.com/7.x/pixel-art/png?seed=Fateh` | DiceBear API (avatar) |
| `https://...` | Any event banner URL from the API | Remote HTTPS |

Glide transparently handles all three cases with disk caching.

### 7.5 Lifecycle-Safe Image Picker

Android's `ActivityResultLauncher` must be registered before `onStart()`. For admin image picking in a dialog, it is registered as a field:

```java
// Registered at fragment creation time — before any dialog opens
private final ActivityResultLauncher<String> imagePicker =
    registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                pendingBannerUri = uri;
                // Preview the selected image immediately
                if (pendingBannerPreview != null)
                    Glide.with(this).load(uri).into(pendingBannerPreview);
            }
        }
    );

// Called later when admin taps the banner area in a dialog
btnPickBanner.setOnClickListener(v -> imagePicker.launch("image/*"));
```

### 7.6 `Transformations.map` — Entity-to-DTO Conversion

When Room returns entities and the ViewModel needs DTOs (or vice versa), `Transformations.map` converts the entire LiveData stream without breaking the reactive chain:

```java
// AdminViewModel — announcements come from Room as entities,
// but the UI expects DTOs. Transformations.map bridges the gap.
announcements = Transformations.map(
    db.announcementDao().getAll(),       // LiveData<List<AnnouncementEntity>>
    entities -> {
        if (entities == null) return new ArrayList<>();
        List<AnnouncementDto> result = new ArrayList<>();
        for (AnnouncementEntity e : entities) result.add(annEntityToDto(e));
        return result;
    }
);
// announcements is now LiveData<List<AnnouncementDto>>
// It re-runs the mapping function every time Room's announcements table changes.
```

---

## 8. Data Model

### 8.1 EventEntity — The Central Entity

```java
@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey @NonNull
    public String id;              // "seed_001" — "seed_021"

    public String title;           // e.g., "Binary Exploitation & Reverse Engineering"
    public String description;     // trainer info, description
    public String bannerUrl;       // "file:///android_asset/event_banners/dsc01516.jpg"
    public String location;        // "Lecture Hall 2"
    public String date;            // "2025-12-16T20:00:00Z"
    public String type;            // "Workshop" | "CTF" | "Event"
    public String difficulty;      // "Beginner" | "Intermediate" | "Advanced"
    public int    capacity;        // max participants
    public int    registeredCount; // current registrations (kept in sync by applyLocalRegistration)
    public boolean isRegistered;   // current user's registration state
    public String organizerId;     // FK to users
}
```

### 8.2 EventRegistrationEntity — Many-to-Many Bridge

```java
@Entity(
    tableName = "event_registrations",
    indices   = {@Index(value = {"eventId", "userId"}, unique = true)}
)
public class EventRegistrationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String eventId;      // FK to events.id
    @NonNull public String userId;       // FK to users.id
             public String username;     // denormalized for display (avoids JOIN)
             public String avatarUrl;    // denormalized for display
             public String userRole;     // denormalized for display
             public String registeredAt; // ISO timestamp
             public String status;       // "REGISTERED" | "ATTENDING" | "ABSENT"
}
```

**Why denormalization?** The participant list in the admin panel shows name, avatar, and role for each participant. Storing them in `event_registrations` avoids a JOIN at read time. Room does not support LiveData-backed JOINs as conveniently as a single-table query.

The `UNIQUE INDEX (eventId, userId)` prevents duplicate registrations at the database level. Combined with `@Insert(onConflict = OnConflictStrategy.IGNORE)`, attempting to register the same user twice is silently a no-op.

### 8.3 UserEntity — Member Profile

```java
@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey @NonNull
    public String id;           // "mem_001" — "mem_054"
    public String username;     // Full name
    public String email;        // Optional
    public String discordId;    // Discord username (optional)
    public String role;         // Guest|Member|Mentor|Organizer|Administrator
    public int    xp;           // 60 — 2500 in seed data
    public int    level;        // derived: xp / 1000 + 1
    public String avatarUrl;    // DiceBear pixel-art URL
    public String createdAt;    // join date "2024-09-01"
    public boolean isBanned;    // admin can set
}
```

### 8.4 AnnouncementEntity

```java
@Entity(tableName = "announcements")
public class AnnouncementEntity {
    @PrimaryKey @NonNull
    public String id;
    public String title;
    public String body;
    public String type;      // General|Technical|Workshop|CTF|Emergency|Security|Administrative|HR|Financial
    public String priority;  // Low|Normal|Important|Critical|Emergency
    public boolean isPinned;
    public String createdAt;
}
```

---

## 9. API Contract

### 9.1 Authentication

```http
POST /api/auth/login
Content-Type: application/json

{ "email": "fateh@cic.dz", "password": "Fateh" }

200 OK
{ "accessToken": "jwt...", "refreshToken": "jwt...", "user": { "id": "mem_036", "role": "Administrator", ... } }

401 Unauthorized
{ "error": "Invalid credentials" }
```

```http
POST /api/auth/discord
{ "code": "discord_oauth_code", "redirectUri": "cic://auth/callback" }

200 OK → same AuthResponse shape
```

### 9.2 Events

```http
GET /api/events?type=Workshop&from=2025-01-01&to=2026-12-31
Authorization: Bearer <token>

200 OK
[
  {
    "id": "seed_003",
    "title": "Introduction to Ethical Hacking",
    "date": "2025-12-15T19:45:00Z",
    "type": "Workshop",
    "difficulty": "Beginner",
    "capacity": 50,
    "registeredCount": 23,
    "isRegistered": false,
    "bannerUrl": "file:///android_asset/event_banners/dsc01470.jpg"
  },
  ...
]
```

```http
POST /api/events/{id}/register
Authorization: Bearer <token>

204 No Content    ← success
409 Conflict      ← already registered
```

### 9.3 Error Handling

All Retrofit failures follow the same pattern. The app never crashes on network errors:

```java
.enqueue(new Callback<T>() {
    @Override
    public void onResponse(Call<T> call, Response<T> r) {
        if (r.isSuccessful() && r.body() != null) {
            // Process response
        } else {
            // HTTP error (4xx, 5xx) — show error or fall back to Room
            if (onError != null) onError.accept("Server error " + r.code());
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        // Network unreachable, DNS failure, timeout
        // Fall back to Room — app is still fully functional
        if (onError != null) onError.accept("Network unavailable — showing cached data");
    }
});
```

---

## 10. Security Engineering

### 10.1 Threat Model

| Threat Vector | Attack Description | Mitigation in This App |
|---|---|---|
| **Token theft** | Attacker reads SharedPreferences from a rooted device | Tokens stored in private-mode SharedPreferences; `TokenManager` wraps all access |
| **Session hijacking** | Intercept JWT token in transit | HTTPS enforced for all API calls; no HTTP fallback |
| **Offline privilege escalation** | Attacker modifies offline session to claim admin role | Offline session hardcodes `mem_036` (known admin); role comes from Room seed data, not the session claim |
| **Broken object level authorization (BOLA/IDOR)** | User queries another user's data by ID | All admin endpoints require server-side role validation |
| **Admin action without audit** | Admin deletes events with no record | Every admin mutation calls `audit(action, entity, detail)` before or after the write |
| **Accidental destructive action** | Admin accidentally bans a user | Ban requires typing "BAN" in a confirmation dialog; dismiss returns to list |
| **APK reverse engineering** | Attacker decompiles APK to extract API secrets | No secrets hardcoded; Discord redirect URI is the only build-time constant |
| **Replay attacks** | Reuse an old valid token | Server enforces token expiry; client calls `POST /auth/refresh` on 401 |

### 10.2 Token Management

`TokenManager` centralizes all token read/write operations. No other class touches SharedPreferences directly for auth state:

```java
public class TokenManager {
    private static final String PREF_NAME     = "cic_auth";
    private static final String KEY_TOKEN     = "access_token";
    private static final String KEY_REFRESH   = "refresh_token";
    private static final String KEY_USER_ID   = "user_id";

    private final SharedPreferences prefs;

    public TokenManager(Context ctx) {
        // MODE_PRIVATE: only this app can read this file
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveTokens(String access, String refresh, String userId) {
        prefs.edit()
            .putString(KEY_TOKEN,   access)
            .putString(KEY_REFRESH, refresh)
            .putString(KEY_USER_ID, userId)
            .apply();  // async write — non-blocking
    }

    public boolean isLoggedIn() {
        return prefs.getString(KEY_TOKEN, null) != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
```

### 10.3 Role-Based Access Control

Five roles with escalating permissions, enforced at both the UI layer (what's visible) and the server layer (what's allowed):

```
┌──────────────────────────────────────────────────────────────────┐
│  Role         │ Register │ Resources │ Create   │ Admin Panel     │
│               │ Events   │           │ Events   │                 │
├───────────────┼──────────┼───────────┼──────────┼─────────────────┤
│  Guest        │    ✗     │     ✗     │    ✗     │      ✗          │
│  Member       │    ✓     │     ✓     │    ✗     │      ✗          │
│  Mentor       │    ✓     │  ✓+Upload │    ✗     │      ✗          │
│  Organizer    │    ✓     │  ✓+Upload │    ✓     │  Events only    │
│  Administrator│    ✓     │  ✓+Upload │    ✓     │   Full access   │
└──────────────────────────────────────────────────────────────────┘
```

### 10.4 Audit Log — Immutability Guarantee

The audit log is an in-memory `MutableLiveData<List<AuditEntry>>` that only ever grows. Entries are prepended (newest first). No entry is ever removed during a session:

```java
private void audit(String action, String entity, String detail) {
    List<AuditEntry> log = new ArrayList<>();
    if (auditLog.getValue() != null) log.addAll(auditLog.getValue()); // copy existing
    log.add(0, new AuditEntry(action, entity, detail));               // prepend new entry
    auditLog.postValue(log);
}
```

Entries are timestamped with `yyyy-MM-dd HH:mm:ss`. Severity is auto-derived — an admin cannot create an audit entry with an inflated or deflated severity.

### 10.5 Input Validation at Boundaries

```java
// LoginViewModel — validates before any network call
public void loginWithEmail(String email, String password) {
    if (email.isEmpty() || password.isEmpty()) {
        error.setValue("Please fill in all fields.");
        return;   // ← early return before any network call
    }
    // ...
}

public void register(String username, String email, String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
        error.setValue("Passwords do not match.");
        return;
    }
    if (password.length() < 8) {
        error.setValue("Password must be at least 8 characters.");
        return;
    }
    // ...
}
```

```java
// EventRepository — floor at zero, prevents negative counts
ev.registeredCount = Math.max(0, ev.registeredCount - 1);
```

---

## 11. Offline-First Architecture

### 11.1 The Philosophy

The app must be useful even when:

- The backend server is not running
- The device has no internet connection
- The user is in an area with poor mobile coverage (common in university buildings)

**Every feature works offline:**

| Feature | Offline Behavior |
|---|---|
| Login | Auto-creates offline session (admin account) |
| View events | Reads from Room — 21 seeded events always available |
| Register for event | Writes to Room immediately; syncs to API when connection returns |
| View announcements | Reads from Room — 6 seeded announcements always available |
| View resources | Reads from Room — 20 seeded resources always available |
| Admin: create event | Writes to Room; best-effort API sync |
| Admin: view participants | Reads from Room — local registration state |

### 11.2 Offline Session Flow

```java
// LoginViewModel — all three login methods call this on network failure
private void startOfflineSession(String hint) {
    String userId = "mem_036"; // Fateh Hassani — Administrator, seed ID
    tokenManager.saveTokens("offline_token", "offline_refresh", userId);
    error.postValue(null);
    loginSuccess.postValue(true);
    // The app opens. All Room data (21 events, 54 users, etc.) is immediately available.
}
```

Three login paths all degrade gracefully:

- `loginWithEmail()` → `onFailure` → `startOfflineSession(email)`
- `register()` → `onFailure` → `startOfflineSession(email)`
- `handleDiscordCallback()` → `onFailure` → `startOfflineSession("discord")`

### 11.3 Seed Data Seeding Strategy

```java
// CICApplication.java — runs once per install, on a background thread
private void seedIfEmpty() {
    Executors.newSingleThreadExecutor().execute(() -> {
        AppDatabase db = AppDatabase.getInstance(this);
        // Guard check per entity type — idempotent, safe to call multiple times
        if (db.eventDao().countById("seed_001") == 0)
            db.eventDao().upsertAll(SeedData.events());        // 21 events
        if (db.userDao().countById("mem_036") == 0)
            db.userDao().upsertAll(SeedData.users());          // 54 users
        if (db.resourceDao().countById("res_001") == 0)
            db.resourceDao().upsertAll(SeedData.resources());  // 20 resources
        if (db.announcementDao().countById("ann_001") == 0)
            db.announcementDao().upsertAll(SeedData.announcements()); // 6 announcements
    });
}
```

The guard `countById("seed_001") == 0` checks for the presence of a known anchor entity. If it exists, seeding is skipped. This makes the seeding operation **idempotent** — safe to call on every app start without creating duplicates.

---

## 12. Admin Panel Engineering

### 12.1 Architecture

The admin panel is a single `AdminActivity` hosting five fragments via `ViewPager2 + TabLayout`. All five fragments share one `AdminViewModel` — scoped to the Activity, not the Fragment:

```java
// Scoped to Activity — shared across all 5 fragments
viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
```

This means the audit log, user list, event list, and announcement list are all in memory once — not re-loaded when switching tabs.

### 12.2 Dashboard — Live Room Counts

The dashboard previously called `GET /admin/stats` which failed silently when the API was unavailable. It now observes Room LiveData directly:

```java
// AdminDashboardFragment — counts come from Room, always accurate
viewModel.getUsers().observe(this, users -> {
    tvUserCount.setText(String.valueOf(users.size()));
    tvMemberCount.setText(String.valueOf(
        (int) users.stream().filter(u -> "Member".equals(u.role)).count()
    ));
});

viewModel.getEvents().observe(this, events -> {
    tvEventCount.setText(String.valueOf(events.size()));
    // Also compute: upcoming events, past events
});
```

### 12.3 Events Tab — Participant Management

Each event card in the admin events tab shows a "Members (N)" button. Tapping it opens a bottom sheet with:

```
┌────────────────────────────────────┐
│  12 REGISTERED │  50 CAPACITY │ 4 ATTENDED │
│ ████████░░░░░░░░░░░░░░░░░░░░░░░ 24% │
│                                    │
│  [ + Add Member ]                  │
│                                    │
│  ● Fateh Hassani     Admin  [REG]  │ ✓ 🗑
│  ● Cerine Labdi      Mentor [ATT]  │ ✓ 🗑
│  ● Belkacem Medjitna Mentor [ABS]  │ ✓ 🗑
│  ...                               │
└────────────────────────────────────┘
```

The attend toggle cycles: `REGISTERED → ATTENDING → ABSENT → REGISTERED`. Each tap writes to `EventRegistrationDao.updateStatus()` and then updates `EventEntity.registeredCount` in Room. The stats bar and progress bar react immediately.

### 12.4 Users Tab — Moderation Actions

```java
// Quick actions popup per user card
popup.setOnMenuItemClickListener(item -> {
    int id = item.getItemId();
    if      (id == R.id.action_view)      showUserDetail(user);
    else if (id == R.id.action_role)      showRoleDialog(user);
    else if (id == R.id.action_warn)      issueWarning(user);
    else if (id == R.id.action_mute)      muteUser(user);
    else if (id == R.id.action_suspend)   suspendUser(user);
    else if (id == R.id.action_ban)       showBanConfirmation(user);
    return true;
});

// Ban requires explicit confirmation — prevents fat-finger bans
private void showBanConfirmation(UserDto user) {
    EditText input = new EditText(getContext());
    input.setHint("Type BAN to confirm");

    new MaterialAlertDialogBuilder(requireContext())
        .setTitle("Ban " + user.username + "?")
        .setView(input)
        .setPositiveButton("Confirm", (d, w) -> {
            if ("BAN".equals(input.getText().toString().trim())) {
                executeBan(user);       // actually bans
            } else {
                showToast("Type BAN exactly to confirm");
            }
        })
        .setNegativeButton("Cancel", null)
        .show();
}
```

### 12.5 Announcements Tab — Priority System

Five priority levels with distinct visual treatment:

| Priority | Left Bar Color | Use Case |
|---|---|---|
| Low | Gray | Internal notes, minor updates |
| Normal | Cyan | Regular club announcements |
| Important | Amber | Deadlines, decisions |
| Critical | Orange | Event cancellations, urgent changes |
| Emergency | Red | Safety notices, immediate action required |

### 12.6 Audit Tab — Severity Classification

```java
// AuditEntry — severity auto-derived from action
private static String deriveSeverity(String action) {
    switch (action != null ? action : "") {
        case "BANNED":
        case "USER MUTED":
        case "SUSPENDED":    return "CRITICAL";  // red
        case "DELETED":
        case "WARNING ISSUED":
        case "CANCELED":
        case "ROLE CHANGE":  return "WARNING";   // amber
        default:             return "INFO";       // cyan
    }
}
```

The audit tab provides:

- Full-text search across action, entity, and detail fields
- Filter by severity (INFO / WARNING / CRITICAL / SECURITY)
- Stats bar (Total / INFO count / WARNING count / CRITICAL count)
- Timestamp on every entry (`yyyy-MM-dd HH:mm:ss`)

---

## 13. Scalability & Performance

### 13.1 Database Performance

**Indexed queries:**

`EventRegistrationEntity` has a composite unique index on `(eventId, userId)`:

```java
@Entity(
    tableName = "event_registrations",
    indices   = {@Index(value = {"eventId", "userId"}, unique = true)}
)
```

This means:

- `SELECT * FROM event_registrations WHERE eventId = ?` is an indexed scan — O(log n) instead of O(n)
- `INSERT OR IGNORE` (used by `@Insert(onConflict = IGNORE)`) checks the index — O(log n) duplicate check

**Background thread enforcement:**

Room throws `IllegalStateException` if a query returning a blocking result (not LiveData) is called on the main thread. This is enforced at build time for Room 2.1+. All `*Sync()` methods in the app are always called inside `executor.execute(() -> {...})`.

**Single-thread executor prevents write conflicts:**

```java
private final ExecutorService executor = Executors.newSingleThreadExecutor();
// All Room writes are serialized through this one thread.
// Two simultaneous registerForEvent() calls cannot corrupt the count.
```

### 13.2 LiveData Efficiency

Room's LiveData implementation uses a `FileObserver` on Android (or `InvalidationTracker` on newer APIs) to detect table-level changes. It does **not** poll. When a table changes, Room schedules a re-query on a background thread. The UI thread receives the new list via `postValue()`.

```
Room table write
    │
    └── InvalidationTracker fires (background)
            │
            └── LiveData.postValue(newList)
                    │
                    └── Main thread: observers are called
                            │
                            └── adapter.submitList(newList)
                                    │
                                    └── DiffUtil computes changes
                                            │
                                            └── RecyclerView animates only changed rows
```

`adapter.submitList()` uses `DiffUtil` internally — it computes the diff on a background thread and animates only the changed items, not the entire list.

### 13.3 Image Loading Performance

Glide caches at three levels:

1. **Active resources**: Images currently displayed — kept in memory, no disk read
2. **Memory cache**: LRU cache of recent images — fast RAM access
3. **Disk cache**: Decoded bitmaps on disk — avoids re-downloading and re-decoding

Asset images (`file:///android_asset/...`) are read from the APK — no disk cache needed on first read, but Glide caches the decoded bitmap in memory.

### 13.4 Memory Management

ViewModels store LiveData, not Fragments or Activities. When the Fragment is destroyed (rotation, back-press), the LiveData removes the dead observer automatically. No memory leaks:

```java
// Lifecycle-aware observation — observer removed when Fragment DESTROYED
viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
    // 'getViewLifecycleOwner()' is the Fragment's view lifecycle,
    // not the Fragment lifecycle — correct for fragments in ViewPager
    adapter.submitList(events);
});
```

### 13.5 Seed Data Load Time

Seeding 54 users + 21 events + 20 resources + 6 announcements on a background thread:

- Uses `@Upsert` (batch SQL `INSERT OR REPLACE`)
- All four seed calls happen in one `Executors.newSingleThreadExecutor()` thread
- Typical time: 80–200ms on device (not on the main thread — no UI impact)
- After first seed, the guard check (`countById == 0`) returns in <1ms — effectively free

---

## 14. UI/UX Engineering

### 14.1 Design Language

The app uses a **dark cybersecurity aesthetic** inspired by Hack The Box, Discord, Linear, and GitHub:

```xml
<!-- colors.xml — the complete design token set -->
<color name="bg_primary">    #0D0D0D</color>  <!-- near-black main background -->
<color name="bg_surface">    #141414</color>  <!-- card surfaces -->
<color name="bg_elevated">   #1C1C1C</color>  <!-- elevated elements (headers, tabs) -->
<color name="bg_card">       #1A1A1A</color>  <!-- card background -->

<color name="accent_cyan">   #00D4FF</color>  <!-- primary accent -->
<color name="matrix_green">  #00FF88</color>  <!-- success, capacity bars -->
<color name="accent_purple"> #8B5CF6</color>  <!-- secondary accent -->
<color name="accent_orange"> #FF6B35</color>  <!-- warnings -->

<color name="text_primary">  #FFFFFF</color>  <!-- main text -->
<color name="text_secondary">#8A8A8A</color>  <!-- supporting text -->
<color name="text_disabled"> #555555</color>  <!-- labels, hints -->

<color name="status_success">#00C853</color>  <!-- online, attending -->
<color name="status_warning">#FF9800</color>  <!-- partial, intermediate -->
<color name="status_error">  #FF4444</color>  <!-- error, banned -->

<color name="divider">       #2A2A2A</color>  <!-- 1dp separators -->
```

### 14.2 Navigation Architecture

```
SplashActivity (2s logo fade-in)
    │
    ▼
LoginActivity
    ├── LoginFragment    (email + password)
    └── RegisterFragment (username + email + password)
    │
    ▼ (on loginSuccess = true)
MainActivity
    ├── HomeFragment     ← Tab 1: everything a member needs
    └── ProfileFragment  ← Tab 2: identity and XP

    └── (if role == Administrator)
        AdminActivity
            ├── AdminDashboardFragment     ← Tab 1
            ├── AdminEventsFragment        ← Tab 2
            ├── AdminUsersFragment         ← Tab 3
            ├── AdminAnnouncementsFragment ← Tab 4
            └── AdminAuditFragment         ← Tab 5
```

### 14.3 Home Screen Layout

```
┌──────────────────────────────────────┐
│  Hey, Fateh         🔔               │
│  Level 3 · 2500 XP  ████████░░ 75%  │
├──────────────────────────────────────┤
│  UPCOMING EVENTS                     │
│  ┌────────────┐ ┌────────────┐       │
│  │[banner img]│ │[banner img]│       │
│  │ WORKSHOP   │ │ CTF        │       │
│  │ Intro to   │ │ Binary     │       │
│  │ Eth. Hack  │ │ Exploit    │       │
│  │ Dec 15     │ │ Dec 16     │       │
│  │ 50 seats   │ │ 50 seats   │       │
│  └────────────┘ └────────────┘  ───► │
├──────────────────────────────────────┤
│  ANNOUNCEMENTS                       │
│  ● [PINNED] CICONIX Registration    │
│  ● Workshop Schedule Published       │
│  ● Weekly CTF Challenge             │
├──────────────────────────────────────┤
│  RESOURCES                           │
│  📄 TryHackMe Beginner Path         │
│  🎥 Network+ Study Guide            │
│  🔬 OWASP Top 10 Labs              │
│  ...                                 │
└──────────────────────────────────────┘
```

### 14.4 Event Card Visual Anatomy

```
┌─────────────────────────────────────────────┐
│ [WORKSHOP]                    [STARTING SOON] │  ← type + status badge
│                                               │
│  ┌───────────────────────────────────────┐   │
│  │          event banner image           │   │  ← Glide loaded
│  └───────────────────────────────────────┘   │
│                                               │
│  Binary Exploitation & Reverse Engineering    │  ← title
│  Dec 16, 2025 · 20:00                        │  ← date
│  Lecture Hall 2                              │  ← location
│                                               │
│  [ADVANCED]     ████████████░░░░ 38/50 seats │  ← difficulty + capacity bar
│                                               │
│          [ Register for this event ]          │  ← CTA button
└─────────────────────────────────────────────┘
```

### 14.5 Admin User Card Anatomy

```
┌─────────────────────────────────────────────────────┐
│  ●  [avatar]  Belkacem Medjitna     [MENTOR]  [⋮]  │
│               EH · Discord: kassem101              │
│               ████████████████░░░░ 950 XP  Lv.1   │
│               Active 45 days ago                   │
└─────────────────────────────────────────────────────┘
  │                                        │
  └── Status dot:                          └── Quick actions popup:
       Green  = XP > 200 (active)              View Profile
       Amber  = XP 50-200 (moderate)           Change Role
       Gray   = XP < 50  (inactive)            Issue Warning
                                               Mute
                                               Suspend
                                               Ban
```

### 14.6 Announcement Card Priority Bar

```
│ Priority │ Left border color │ Example use               │
│──────────│───────────────────│───────────────────────────│
│ Low      │ #555555 gray      │ "Office hours moved"      │
│ Normal   │ #00D4FF cyan      │ "New resource uploaded"   │
│ Important│ #FF9800 amber     │ "Registration deadline"   │
│ Critical │ #FF6B35 orange    │ "Event rescheduled"       │
│ Emergency│ #FF4444 red       │ "Event cancelled — today" │
```

### 14.7 Accessibility Considerations

- All interactive elements have `android:contentDescription` for screen readers
- Touch targets are minimum 44dp × 44dp (Material guideline)
- Text contrast ratios: primary text on `#0D0D0D` background exceeds WCAG AA (4.5:1)
- `android:ellipsize="end"` + `android:maxLines` prevents layout overflow on long text
- `nestedScrollingEnabled="false"` on inner RecyclerViews prevents scroll conflicts
- All icon buttons have fallback text descriptions

---

## 15. Seed Data Catalog

### 15.1 Event Catalog — 21 Events

| ID | Title | Type | Difficulty | Date | Trainer |
| --- | --- | --- | --- | --- | --- |
| seed_001 | Open Day | Event | Beginner | 2025-12-06 | Club |
| seed_002 | Intro to Graphic Design | Workshop | Beginner | 2025-12-14 | Labdi Cerine |
| seed_003 | Intro to Ethical Hacking | Workshop | Beginner | 2025-12-15 | Gahlouz T. + Harnane S. |
| seed_004 | Intro to Public Speaking | Workshop | Beginner | 2025-12-16 | Labdi C. + Zeraouia C. |
| seed_005 | Binary Exploitation & RE | Workshop | Advanced | 2025-12-16 | Cherfaoui + Zengla |
| seed_006 | Intro to Frontend | Workshop | Beginner | 2025-12-20 | Belayadi Ritedj |
| seed_007 | Python Backend Dev | Workshop | Beginner | 2026-01-26 | Yakoubi Moncef |
| seed_008 | Intro to Game Dev | Workshop | Beginner | 2026-01-27 | Bouagual N. |
| seed_009 | Design | Workshop | Beginner | 2026-01-27 | Labdi Cerine |
| seed_010 | Frontend Fundamentals | Workshop | Beginner | 2026-01-25 | Belayadi Ritaj |
| seed_011 | Game Dev (repeat) | Workshop | Beginner | 2026-02-01 | Bouagual N. |
| seed_012 | Design (repeat) | Workshop | Beginner | 2026-02-01 | Labdi Cerine |
| seed_013 | Web Ethical Hacking | Workshop | Intermediate | 2026-02-02 | Medjitna Belkacem |
| seed_014 | Cryptography & Password | Workshop | Intermediate | 2026-02-03 | Berrahia + Ouchen |
| seed_015 | Career Paths in CS | Workshop | Beginner | 2026-02-03 | Mr. Berghout |
| seed_016 | All of AI | Workshop | Beginner | 2026-02-08 | Manaa Mohamed |
| seed_017 | Digital Forensics & EH | Workshop | Intermediate | 2026-02-08 | Harnane Samy |
| seed_018 | Wireless Hacking | Workshop | Intermediate | 2026-02-09 | Gahlouz Tinhinane |
| seed_019 | OOP | Workshop | Beginner | 2026-02-10 | Yakoubi Ahmed Moncef |
| seed_020 | Intro to Project Mgmt | Workshop | Beginner | 2026-02-10 | Aissaoui Amine |
| seed_021 | CICONIX | Event | Intermediate | 2026-02-12 | Full club — 55hr event |

### 15.2 Member Roster — 54 Members

**By Department:**

| Department | Count | Notable Members |
|---|---|---|
| HR | 8 | Amine Aissou, Manar Oukili, Meriem Belkadi |
| ER | 7 | Rabeh Haddadi (300 XP), Yasmine Ouchen (Mentor, 580 XP) |
| Design | 3 | Cerine Labdi (Mentor, 850 XP), Amir Mekroud |
| Multi-media | 6 | Ritadj Belayadi (Mentor, 720 XP), Mohamed Kahoul |
| IT | 18 | Fateh Hassani (Admin, 2500 XP), Sami Harnane (Mentor, 780 XP) |
| EH | 7 | Belkacem Medjitna (Mentor, 950 XP), Moncef Yaakoubi (Mentor, 890 XP) |
| Finance | 1 | Roudjina Assil Kaabeche |
| Logistics | 1 | Alaa Siafa |

**By Role:**

| Role | Count |
|---|---|
| Administrator | 1 (Fateh Hassani — mem_036) |
| Mentor | 10 |
| Member | 43 |

**XP Range:** 60 (Abdenour Gheribi) — 2500 (Fateh Hassani)

### 15.3 Resource Catalog — 20 Resources

| ID | Title | Category | Type | Difficulty |
| --- | --- | --- | --- | --- |
| res_001 | TryHackMe Beginner Path | Web Security | Lab | Beginner |
| res_002 | OWASP Top 10 (2021) | Web Security | Documentation | Intermediate |
| res_003 | HackTheBox Starting Point | Web Security | Lab | Beginner |
| res_004 | Reverse Engineering 101 | Reverse Engineering | Slides | Intermediate |
| res_005 | Ghidra User Manual | Reverse Engineering | Documentation | Advanced |
| res_006 | CTF Field Guide | Reverse Engineering | PDF | Intermediate |
| res_007 | RSA from Scratch | Cryptography | PDF | Intermediate |
| res_008 | CryptoHack Platform | Cryptography | Lab | Beginner |
| res_009 | Applied Cryptography (Schneier) | Cryptography | Documentation | Advanced |
| res_010 | Python for Pentesters | Programming | Video | Beginner |
| res_011 | Bash Scripting for Security | Programming | PDF | Intermediate |
| res_012 | Linux Privilege Escalation | Linux | Lab | Advanced |
| res_013 | Linux Fundamentals | Linux | Documentation | Beginner |
| res_014 | Networking Fundamentals (CompTIA) | Networking | Video | Beginner |
| res_015 | Wireshark Labs | Networking | Lab | Intermediate |
| res_016 | Metasploit Unleashed | Web Security | Documentation | Advanced |
| res_017 | Social Engineering Techniques | Web Security | Slides | Intermediate |
| res_018 | Memory Forensics with Volatility | Reverse Engineering | Lab | Advanced |
| res_019 | AWS Security Fundamentals | Cloud Security | Video | Beginner |
| res_020 | Docker Security Essentials | Cloud Security | Documentation | Intermediate |

---

## 16. Mock API Server

### 16.1 Architecture

A lightweight Express.js server that mirrors the production API contract. Used for local development and BlueStacks testing.

```javascript
// mock-api/server.js — Express.js, in-memory state
const express = require('express');
const app = express();
app.use(express.json());

// In-memory stores — reset on server restart
let users         = [...seedUsers];
let events        = [...seedEvents];
let resources     = [...seedResources];
let announcements = [...seedAnnouncements];

// All 30 endpoints match the production contract exactly
app.post('/api/auth/login',         (req, res) => { /* ... */ });
app.get('/api/events',              (req, res) => res.json(events));
app.post('/api/events/:id/register',(req, res) => { /* ... */ });
// ...

app.listen(3000, '0.0.0.0', () => {
    console.log('CIC Mock API running on 0.0.0.0:3000');
});
```

### 16.2 Network Topology (BlueStacks)

```
┌──────────────────────────────────────────────┐
│  Host Machine (Windows 11)                   │
│                                              │
│  node mock-api/server.js                    │
│  Listening: 0.0.0.0:3000                    │
│                                              │
│  IP on BlueStacks virtual network:           │
│  192.168.121.1  (host-to-BlueStacks bridge) │
└──────────────────────┬───────────────────────┘
                       │  HTTP
                       │  192.168.121.1:3000
                       ▼
┌──────────────────────────────────────────────┐
│  BlueStacks (Android Emulator)               │
│                                              │
│  RetrofitClient BASE_URL:                   │
│  "http://192.168.121.1:3000/api/"           │
│                                              │
│  ADB over TCP: adb connect 127.0.0.1:5555  │
└──────────────────────────────────────────────┘
```

### 16.3 Starting the Server

```bash
cd mock-api
node server.js
# Output: CIC Mock API running on 0.0.0.0:3000

# Test it:
curl http://localhost:3000/api/events
# Returns: JSON array of 21 events
```

---

## 17. Future Extensibility

### 17.1 Production Backend — Spring Boot

The mock server exposes identical endpoints to what the Spring Boot production backend will implement. The migration path:

1. Implement the same 30 endpoints in Spring Boot + PostgreSQL
2. Deploy to a server accessible from student devices
3. Change one line in `RetrofitClient.java`:

   ```java
   private static final String BASE_URL = "https://api.cic.dz/api/";
   ```

4. All features — including offline mode — continue working unchanged

### 17.2 Discord OAuth2

The `LoginActivity` already has a "Login with Discord" button. The server flow:

```
User taps "Login with Discord"
    │
    ▼
App opens Discord OAuth2 URL in browser/custom tab
    │
    ▼
User authorizes → Discord redirects to cic://auth/callback
    │
    ▼
LoginActivity.handleDiscordCallback(code)
    │
    ▼
POST /api/auth/discord { "code": "...", "redirectUri": "cic://auth/callback" }
    │
    ▼
Server exchanges code for Discord token → fetches user info → creates/finds CIC user
    │
    ▼
Returns JWT → app opens with real user profile
```

No client-side changes needed beyond configuring the Discord Application's redirect URI.

### 17.3 Firebase Cloud Messaging (Push Notifications)

`AnnouncementEntity` already has `type` and `priority` fields. The FCM integration:

1. Add Firebase to the project (`google-services.json`)
2. Implement `MyFirebaseMessagingService extends FirebaseMessagingService`
3. Filter notifications by priority: Emergency + Critical → high-priority channel; others → default channel
4. The server sends FCM payloads when an admin creates a Critical or Emergency announcement

### 17.4 QR Code Check-in

`EventRegistrationEntity.status` already supports `ATTENDING`. The QR flow:

1. Add `zxing` barcode scanner library
2. Each event generates a QR code containing its `eventId`
3. Organizer scans a member's QR code → calls `eventRegistrationDao.updateStatus(eventId, userId, "ATTENDING")`
4. Admin participant sheet updates in real time (Room LiveData fires)

### 17.5 Gamification System

`UserEntity` already has `xp` and `level` fields. The XP system:

- Register for event: +10 XP
- Attend event (status = ATTENDING): +50 XP
- Complete workshop resource: +20 XP
- Publish a resource (as Mentor): +100 XP

Implementation: a `XpService` class that calls `userDao.addXp(userId, delta)` after each action. The profile screen's XP bar is already wired to LiveData — it updates automatically.

### 17.6 Dependency Injection with Hilt

The constructor injection pattern (`new EventRepository(app)`) is already DI-compatible. Migrating to Hilt:

```java
// Step 1: Annotate the module
@Module @InstallIn(SingletonComponent.class)
public class DataModule {
    @Provides @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context ctx) {
        return AppDatabase.getInstance(ctx);
    }

    @Provides @Singleton
    public EventRepository provideEventRepo(AppDatabase db, ApiService api) {
        return new EventRepository(db.eventDao(), api);
    }
}

// Step 2: Inject instead of construct
@HiltViewModel
public class EventDetailViewModel extends AndroidViewModel {
    @Inject EventRepository repository;  // Hilt injects this
}
```

No Repository or DAO code changes. Only ViewModels and Application class need annotations.

### 17.7 Multi-Module Gradle Build

As the project grows past 50k lines, split into Gradle modules for faster incremental builds:

```
:app                   ← Shell — just the Application class and Activities
:feature:home          ← HomeFragment, HomeViewModel
:feature:events        ← EventDetailActivity, EventDetailViewModel
:feature:admin         ← All 5 admin fragments + AdminViewModel
:feature:auth          ← LoginActivity, LoginViewModel
:data:local            ← Room entities, DAOs, AppDatabase
:data:remote           ← Retrofit, ApiService, DTOs
:data:repository       ← Repository implementations
:core:design           ← Colors, themes, shared layouts
:core:util             ← TokenManager, ReadTracker
```

Each feature module sees only `data:local` and `data:remote` — not other feature modules. Parallel compilation of all feature modules during `./gradlew assembleDebug`.

### 17.8 Testing Strategy

```
Unit Tests (JUnit 4)
├── EventRepositoryTest    — mock ApiService, in-memory Room, verify write logic
├── EventDetailViewModelTest — mock Repository, verify LiveData emissions
├── AuditEntryTest         — verify severity derivation for all action types
└── SeedDataTest           — verify all 21 events + 54 users parse without errors

Integration Tests (AndroidJUnit4 + Room in-memory)
├── EventDaoTest           — verify upsert, getById, countAll
├── EventRegistrationDaoTest — verify unique index enforcement
└── AnnouncementDaoTest    — verify pin/unpin, getAll ordering

UI Tests (Espresso)
├── LoginFlowTest          — email login success + offline fallback
├── EventRegistrationTest  — tap Register → Room state verified
└── AdminAuditTest         — admin deletes event → audit entry appears
```

---

## 18. Setup & Build

### 18.1 Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| Android Studio | Hedgehog (2023.1.1) or later | Recommended: Ladybug (2024.2.1) |
| JDK | 17 | Bundled with recent Android Studio |
| Android SDK | API 34 (Android 14) | Install via SDK Manager |
| Node.js | 18 LTS or later | For mock API server only |
| ADB | Platform tools 34+ | For BlueStacks / device install |

### 18.2 Clone and Open

```bash
git clone <repository-url> CICMobApp
# In Android Studio: File → Open → navigate to CICMobApp folder
# Wait for Gradle sync (first sync downloads dependencies — ~2–5 min)
```

### 18.3 Configure the API URL

Edit [RetrofitClient.java](app/src/main/java/com/cic/mobapp/data/remote/RetrofitClient.java):

```java
// For BlueStacks on the same machine as the server:
private static final String BASE_URL = "http://192.168.121.1:3000/api/";

// For a physical Android device on the same Wi-Fi network:
private static final String BASE_URL = "http://192.168.1.<your-pc-ip>:3000/api/";

// For Android Studio emulator (x86):
private static final String BASE_URL = "http://10.0.2.2:3000/api/";

// For production:
private static final String BASE_URL = "https://api.cic.dz/api/";
```

### 18.4 Start the Mock Server

```bash
cd mock-api
npm install        # first time only — installs express
node server.js

# Expected output:
# CIC Mock API running on 0.0.0.0:3000

# Verify:
curl http://localhost:3000/api/events | head -c 200
```

### 18.5 Build and Install

```bash
# Build debug APK
./gradlew assembleDebug

# APK output:
# app/build/outputs/apk/debug/app-debug.apk

# Install on BlueStacks
adb connect 127.0.0.1:5555
adb install app/build/outputs/apk/debug/app-debug.apk

# Install on physical device (USB debugging enabled)
adb devices
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 18.6 Default Credentials

**Online mode (mock server running):**

| Field | Value |
|---|---|
| Email | Any email from the seed list |
| Password | Same as the username (first name) |
| Example | `fateh@cic.dz` / `Fateh` |

**Offline mode (no server):**

The app automatically logs in as the administrator (Fateh Hassani, mem_036) when the API is unreachable. All 21 events, 54 users, 20 resources, and 6 announcements are loaded from Room seed data.

### 18.7 Running the Admin Panel

After login as an Administrator:

1. Bottom navigation shows a third tab: **Admin**
2. Tap it to open the Admin Panel
3. Five tabs: Dashboard, Events, Users, Announcements, Audit

To test as a non-admin: seed another user into Room or change `mem_036`'s role to Member.

---

## 19. Project File Structure

```
CICMobApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── assets/
│   │       │   └── event_banners/          ← 17 real CIC photos (1280×720 JPG)
│   │       │       ├── dsc01412.jpg        ← Dec 2025 workshop photos
│   │       │       ├── dsc01451.jpg
│   │       │       ├── dsc01470.jpg        ← Ethical Hacking workshop
│   │       │       ├── dsc01511.jpg        ← Public Speaking workshop
│   │       │       ├── dsc01516.jpg        ← Binary Exploitation workshop
│   │       │       ├── dsc01539.jpg        ← Frontend workshop
│   │       │       ├── dsc01598.jpg
│   │       │       ├── dsc01670.jpg        ← Python workshop
│   │       │       ├── dsc01681.jpg        ← Game Dev workshop
│   │       │       ├── dsc01804.jpg
│   │       │       ├── dsc01886.jpg        ← Open Day
│   │       │       ├── dsc01914.jpg
│   │       │       ├── dsc01919.jpg        ← Web Security workshop
│   │       │       ├── dsc01933.jpg        ← Career Paths talk
│   │       │       ├── dsc01956.jpg        ← Cryptography workshop
│   │       │       ├── dsc02040.jpg        ← AI workshop
│   │       │       └── dsc02327.jpg        ← Digital Forensics workshop
│   │       │
│   │       ├── java/com/cic/mobapp/
│   │       │   ├── CICApplication.java      ← App entry, seeds Room on first launch
│   │       │   ├── data/
│   │       │   │   ├── local/
│   │       │   │   │   ├── AppDatabase.java ← Singleton Room v3
│   │       │   │   │   ├── SeedData.java    ← 21 events, 54 users, 20 res, 6 ann
│   │       │   │   │   ├── dao/
│   │       │   │   │   │   ├── EventDao.java
│   │       │   │   │   │   ├── UserDao.java
│   │       │   │   │   │   ├── ResourceDao.java
│   │       │   │   │   │   ├── AnnouncementDao.java
│   │       │   │   │   │   └── EventRegistrationDao.java
│   │       │   │   │   └── entity/
│   │       │   │   │       ├── EventEntity.java
│   │       │   │   │       ├── UserEntity.java
│   │       │   │   │       ├── ResourceEntity.java
│   │       │   │   │       ├── AnnouncementEntity.java
│   │       │   │   │       └── EventRegistrationEntity.java
│   │       │   │   ├── remote/
│   │       │   │   │   ├── ApiService.java   ← 30 Retrofit endpoint declarations
│   │       │   │   │   ├── RetrofitClient.java
│   │       │   │   │   └── dto/
│   │       │   │   │       ├── EventDto.java
│   │       │   │   │       ├── UserDto.java
│   │       │   │   │       ├── ResourceDto.java
│   │       │   │   │       ├── AnnouncementDto.java
│   │       │   │   │       └── AuthDto.java
│   │       │   │   └── repository/
│   │       │   │       ├── EventRepository.java
│   │       │   │       ├── UserRepository.java
│   │       │   │       ├── ResourceRepository.java
│   │       │   │       └── AnnouncementRepository.java
│   │       │   ├── ui/
│   │       │   │   ├── auth/
│   │       │   │   │   ├── LoginActivity.java
│   │       │   │   │   ├── LoginFragment.java
│   │       │   │   │   ├── RegisterFragment.java
│   │       │   │   │   └── LoginViewModel.java
│   │       │   │   ├── home/
│   │       │   │   │   ├── HomeFragment.java
│   │       │   │   │   └── HomeViewModel.java
│   │       │   │   ├── events/
│   │       │   │   │   ├── EventDetailActivity.java
│   │       │   │   │   └── EventDetailViewModel.java
│   │       │   │   ├── profile/
│   │       │   │   │   ├── ProfileFragment.java
│   │       │   │   │   └── ProfileViewModel.java
│   │       │   │   └── admin/
│   │       │   │       ├── AdminActivity.java
│   │       │   │       ├── AdminViewModel.java
│   │       │   │       ├── AdminDashboardFragment.java
│   │       │   │       ├── AdminEventsFragment.java
│   │       │   │       ├── AdminUsersFragment.java
│   │       │   │       ├── AdminAnnouncementsFragment.java
│   │       │   │       ├── AdminAuditFragment.java
│   │       │   │       └── AuditEntry.java
│   │       │   └── util/
│   │       │       ├── TokenManager.java
│   │       │       └── ReadTracker.java
│   │       │
│   │       └── res/
│   │           ├── layout/
│   │           │   ├── activity_splash.xml
│   │           │   ├── activity_login.xml
│   │           │   ├── activity_main.xml
│   │           │   ├── activity_event_detail.xml
│   │           │   ├── activity_admin.xml
│   │           │   ├── fragment_home.xml
│   │           │   ├── fragment_profile.xml
│   │           │   ├── fragment_admin_dashboard.xml
│   │           │   ├── fragment_admin_events.xml
│   │           │   ├── fragment_admin_users.xml
│   │           │   ├── fragment_admin_announcements.xml
│   │           │   ├── fragment_admin_audit.xml
│   │           │   ├── item_event_card.xml
│   │           │   ├── item_resource.xml
│   │           │   ├── item_announcement.xml
│   │           │   ├── item_user_card.xml
│   │           │   ├── item_participant.xml
│   │           │   ├── item_audit_entry.xml
│   │           │   └── layout_event_participants.xml
│   │           ├── drawable/
│   │           │   ├── ic_cic_logo.png       ← CIC shield logo (512×512)
│   │           │   ├── bg_avatar_circle.xml
│   │           │   ├── bg_badge.xml
│   │           │   └── placeholder_event.xml
│   │           ├── mipmap-mdpi/ic_launcher.png     ← 48×48
│   │           ├── mipmap-hdpi/ic_launcher.png     ← 72×72
│   │           ├── mipmap-xhdpi/ic_launcher.png    ← 96×96
│   │           ├── mipmap-xxhdpi/ic_launcher.png   ← 144×144
│   │           ├── mipmap-xxxhdpi/ic_launcher.png  ← 192×192
│   │           └── values/
│   │               ├── colors.xml
│   │               ├── strings.xml
│   │               ├── themes.xml
│   │               └── dimens.xml
│   │
│   └── build.gradle  ← Room, Retrofit, Glide, Material dependencies
│
├── mock-api/
│   ├── server.js     ← Express.js mock backend (30 endpoints)
│   ├── package.json
│   └── node_modules/
│
├── Project.md        ← Full technical + UI/UX specification
└── README.md         ← This file
```

---

*Built by Fateh Hassani for the Cyber Innovators Club — ENSS Algeria.*  
*Architecture: MVVM · Room · Retrofit · Offline-First · Material 3 Dark*  
*Language: Java · Android API 26–34 · Room v3 · Retrofit 2.9*
