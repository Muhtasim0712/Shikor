package com.shikor.app.ui.tools;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;
import com.shikor.app.util.StatsCalculator;

import java.util.List;

/**
 * StatsViewModel - ViewModel loading family entities off main thread and computing statistics.
 * CONCEPT: MVVM ViewModel, LiveData, Room data queries on AppExecutors, and StatsCalculator integration.
 */
public class StatsViewModel extends AndroidViewModel {

    private final MutableLiveData<StatsCalculator.FamilyStats> statsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(true);

    public StatsViewModel(@NonNull Application application) {
        super(application);
        loadStats();
    }

    public LiveData<StatsCalculator.FamilyStats> getStatsLiveData() {
        return statsLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public void loadStats() {
        isLoadingLiveData.setValue(true);
        AppExecutors.getInstance().diskIO().execute(() -> {
            String familyId = Prefs.getInstance(getApplication()).getFamilyId();
            AppDatabase db = AppDatabase.getInstance(getApplication());

            List<PersonEntity> people;
            List<StoryEntity> stories;
            List<PhotoEntity> photos;

            if (familyId != null && !familyId.isEmpty()) {
                people = db.personDao().getAllPeopleSync(familyId);
                stories = db.storyDao().getAllStoriesForFamilySync(familyId);
                photos = db.photoDao().getAllPhotosForFamilySync(familyId);
            } else {
                people = db.personDao().getAllPeopleGlobalSync();
                stories = db.storyDao().getAllStoriesGlobalSync();
                photos = db.photoDao().getAllPhotosGlobalSync();
            }

            StatsCalculator.FamilyStats stats = StatsCalculator.computeStats(people, stories, photos);

            AppExecutors.getInstance().mainThread().execute(() -> {
                statsLiveData.setValue(stats);
                isLoadingLiveData.setValue(false);
            });
        });
    }
}
