package com.cic.mobapp.ui.events;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.repository.EventRepository;

public class EventDetailViewModel extends AndroidViewModel {

    private final EventRepository      repository;
    private       LiveData<EventEntity> event;
    private final MutableLiveData<String>  toast   = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registered = new MutableLiveData<>(false);

    public EventDetailViewModel(@NonNull Application app) {
        super(app);
        repository = new EventRepository(app);
    }

    public LiveData<EventEntity> getEvent(String id) {
        if (event == null) {
            event = repository.getEvent(id);
            repository.refreshFromApi(null, null);
        }
        return event;
    }

    public LiveData<String>  getToast()      { return toast; }
    public LiveData<Boolean> isRegistered()  { return registered; }

    public void toggleRegistration(EventEntity e) {
        if (e.isRegistered) {
            repository.registerForEvent(e.id,
                    () -> { toast.postValue("Unregistered from " + e.title); registered.postValue(false); },
                    () -> toast.postValue("Failed to unregister")
            );
        } else {
            repository.registerForEvent(e.id,
                    () -> { toast.postValue("Registered for " + e.title + "!"); registered.postValue(true); },
                    () -> toast.postValue("Registration failed — try again")
            );
        }
    }
}
