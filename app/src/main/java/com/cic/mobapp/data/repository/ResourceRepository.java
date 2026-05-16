package com.cic.mobapp.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.cic.mobapp.data.local.AppDatabase;
import com.cic.mobapp.data.local.dao.ResourceDao;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.ResourceDto;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourceRepository {

    private final ResourceDao     resourceDao;
    private final ApiService      apiService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ResourceRepository(Application application) {
        resourceDao = AppDatabase.getInstance(application).resourceDao();
        apiService  = RetrofitClient.getApiService();
    }

    /** Persistent LiveData from Room. */
    public LiveData<List<ResourceEntity>> getResourcesLiveData(String category) {
        return category == null ? resourceDao.getAll() : resourceDao.getByCategory(category);
    }

    /** Triggers API refresh → Room updates → LiveData notifies. */
    public void refreshFromApi(String category, Runnable onSuccess, Consumer<String> onError) {
        apiService.getResources(category, null, null).enqueue(new Callback<List<ResourceDto>>() {
            @Override public void onResponse(Call<List<ResourceDto>> c, Response<List<ResourceDto>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<ResourceEntity> entities = new ArrayList<>();
                    for (ResourceDto dto : r.body()) entities.add(toEntity(dto));
                    executor.execute(() -> {
                        resourceDao.upsertAll(entities);
                        if (onSuccess != null) onSuccess.run();
                    });
                } else {
                    if (onError != null) onError.accept("Server error " + r.code());
                }
            }
            @Override public void onFailure(Call<List<ResourceDto>> c, Throwable t) {
                if (onError != null) onError.accept("Network unavailable — showing cached data");
            }
        });
    }

    private ResourceEntity toEntity(ResourceDto dto) {
        ResourceEntity e = new ResourceEntity();
        e.id         = dto.id;
        e.title      = dto.title;
        e.category   = dto.category;
        e.type       = dto.type;
        e.difficulty = dto.difficulty;
        e.fileUrl    = dto.fileUrl;
        e.description = dto.description;
        e.tags       = dto.tags != null ? String.join(",", dto.tags) : "";
        e.uploadedBy = dto.uploadedBy;
        e.createdAt  = dto.createdAt;
        return e;
    }
}
