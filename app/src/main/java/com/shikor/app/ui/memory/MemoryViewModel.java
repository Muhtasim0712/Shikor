package com.shikor.app.ui.memory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.MemoryPhotoEntity;
import com.shikor.app.data.repository.MemoryRepository;
import com.shikor.app.util.Prefs;

import java.util.List;

/**
 * MemoryViewModel - MVVM ViewModel managing memories for the new 'Memories' feature (Phase 1).
 * CONCEPT: MVVM ViewModel, LiveData, background execution via repository.
 */
public class MemoryViewModel extends AndroidViewModel {

    private final MemoryRepository repository;
    private final String currentFamilyId;

    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> operationError = new MutableLiveData<>();

    public MemoryViewModel(@NonNull Application application) {
        super(application);
        this.repository = MemoryRepository.getInstance(application);
        this.currentFamilyId = Prefs.getInstance(application).getFamilyId();
    }

    public LiveData<List<MemoryEntity>> getMemories() {
        return repository.getMemoriesForFamily(currentFamilyId);
    }

    public LiveData<MemoryEntity> getMemoryById(String id) {
        return repository.getMemoryById(id);
    }

    public LiveData<List<MemoryPhotoEntity>> getPhotosForMemory(String memoryId) {
        return repository.getPhotosForMemory(memoryId);
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public LiveData<String> getOperationError() {
        return operationError;
    }

    public void insertMemory(MemoryEntity memory, List<MemoryPhotoEntity> photos) {
        repository.insertMemory(memory, photos, new MemoryRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                operationSuccess.setValue(true);
            }

            @Override
            public void onError(String error) {
                operationError.setValue(error);
            }
        });
    }
}
