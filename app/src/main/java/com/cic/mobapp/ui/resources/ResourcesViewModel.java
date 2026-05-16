package com.cic.mobapp.ui.resources;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import com.cic.mobapp.data.repository.ResourceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResourcesViewModel extends AndroidViewModel {

    private final ResourceRepository repository;

    private final MutableLiveData<Boolean>              loading     = new MutableLiveData<>(true);
    private final MutableLiveData<String>               error       = new MutableLiveData<>(null);
    private final MutableLiveData<List<ResourceEntity>> filtered    = new MutableLiveData<>(new ArrayList<>());

    private List<ResourceEntity> allCached = new ArrayList<>();

    public ResourcesViewModel(@NonNull Application application) {
        super(application);
        repository = new ResourceRepository(application);
        doRefresh();
    }

    public LiveData<List<ResourceEntity>> getResources() { return filtered; }
    public LiveData<Boolean>              isLoading()    { return loading; }
    public LiveData<String>               getError()     { return error; }

    public void filterByCategory(String category) {
        if (category == null) {
            filtered.setValue(allCached);
        } else {
            filtered.setValue(allCached.stream()
                    .filter(r -> category.equalsIgnoreCase(r.category))
                    .collect(Collectors.toList()));
        }
    }

    public void filterByDifficulty(String difficulty) {
        filtered.setValue(allCached.stream()
                .filter(r -> difficulty.equalsIgnoreCase(r.difficulty))
                .collect(Collectors.toList()));
    }

    public void filterByType(String type) {
        filtered.setValue(allCached.stream()
                .filter(r -> type.equalsIgnoreCase(r.type))
                .collect(Collectors.toList()));
    }

    public void search(String query) {
        if (query == null || query.isEmpty()) {
            filtered.setValue(allCached);
            return;
        }
        String q = query.toLowerCase();
        filtered.setValue(allCached.stream()
                .filter(r -> (r.title       != null && r.title.toLowerCase().contains(q))
                          || (r.category    != null && r.category.toLowerCase().contains(q))
                          || (r.description != null && r.description.toLowerCase().contains(q)))
                .collect(Collectors.toList()));
    }

    private void doRefresh() {
        loading.setValue(true);
        error.setValue(null);
        repository.refreshFromApi(
                null,
                () -> {
                    // After DB write, observe the Room LiveData once to populate allCached
                    repository.getResourcesLiveData(null).observeForever(list -> {
                        if (list != null) {
                            allCached = list;
                            filtered.postValue(list);
                        }
                        loading.postValue(false);
                        error.postValue(null);
                    });
                },
                msg -> { loading.postValue(false); error.postValue(msg); }
        );
    }
}
