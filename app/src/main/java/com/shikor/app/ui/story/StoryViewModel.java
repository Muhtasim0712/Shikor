package com.shikor.app.ui.story;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.data.repository.FamilyRepository;
import com.shikor.app.util.Prefs;

import java.util.List;

/**
 * StoryViewModel - MVVM ViewModel managing stories, oral history recordings, and photo timeline.
 */
public class StoryViewModel extends AndroidViewModel {

    private final FamilyRepository repository;
    private final String currentFamilyId;

    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> operationError = new MutableLiveData<>();

    public StoryViewModel(@NonNull Application application) {
        super(application);
        this.repository = FamilyRepository.getInstance(application);
        this.currentFamilyId = Prefs.getInstance(application).getFamilyId();
    }

    public String getCurrentFamilyId() {
        return currentFamilyId;
    }

    public void syncFromCloud() {
        String familyId = Prefs.getInstance(getApplication()).getFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            repository.syncDownFamilyData(familyId, null);
        }
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public LiveData<String> getOperationError() {
        return operationError;
    }

    public LiveData<List<StoryEntity>> getStoriesForPerson(String personId) {
        return repository.getStoriesForPerson(personId);
    }

    public LiveData<List<StoryEntity>> getAllStories() {
        if (currentFamilyId == null || currentFamilyId.isEmpty()) {
            return new MutableLiveData<>();
        }
        return repository.getAllStoriesForFamily(currentFamilyId);
    }

    public LiveData<StoryEntity> getStoryById(String storyId) {
        return repository.getStoryById(storyId);
    }

    public void saveStory(StoryEntity story) {
        repository.insertStory(story, new FamilyRepository.RepositoryCallback<Void>() {
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

    public void deleteStory(StoryEntity story) {
        repository.deleteStory(story, new FamilyRepository.RepositoryCallback<Void>() {
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

    public LiveData<List<PhotoEntity>> getTimelinePhotos() {
        if (currentFamilyId == null || currentFamilyId.isEmpty()) {
            return new MutableLiveData<>();
        }
        return repository.getTimelinePhotos(currentFamilyId);
    }

    public void savePhoto(PhotoEntity photo) {
        repository.insertPhoto(photo, new FamilyRepository.RepositoryCallback<Void>() {
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

    public void deletePhoto(PhotoEntity photo) {
        repository.deletePhoto(photo, new FamilyRepository.RepositoryCallback<Void>() {
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
