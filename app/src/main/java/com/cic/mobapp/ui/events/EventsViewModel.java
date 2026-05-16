package com.cic.mobapp.ui.events;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.repository.EventRepository;
import java.util.List;

public class EventsViewModel extends AndroidViewModel {

    private final EventRepository repository;

    // Stable reference — never reassigned so fragment observers always stay valid
    private final LiveData<List<EventEntity>> events;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(true);
    private final MutableLiveData<String>  error   = new MutableLiveData<>(null);

    public EventsViewModel(@NonNull Application application) {
        super(application);
        repository = new EventRepository(application);
        events = repository.getEventsLiveData();
        doRefresh();
    }

    public LiveData<List<EventEntity>> getEvents()  { return events; }
    public LiveData<Boolean>           isLoading()  { return loading; }
    public LiveData<String>            getError()   { return error; }

    public void refresh() { doRefresh(); }

    private void doRefresh() {
        loading.setValue(true);
        error.setValue(null);
        repository.refreshFromApi(
                () -> { loading.postValue(false); error.postValue(null); },
                msg -> { loading.postValue(false); error.postValue(msg); }
        );
    }

    public void registerForEvent(String id, Runnable onSuccess, Runnable onError) {
        repository.registerForEvent(id, onSuccess, onError);
    }
}
