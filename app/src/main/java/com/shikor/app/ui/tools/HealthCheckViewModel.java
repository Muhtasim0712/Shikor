package com.shikor.app.ui.tools;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.HealthChecker;
import com.shikor.app.util.Prefs;

import java.util.List;

/**
 * HealthCheckViewModel - ViewModel running HealthChecker off main thread.
 * CONCEPT: MVVM ViewModel, LiveData, background thread execution via AppExecutors, and HealthChecker integration.
 */
public class HealthCheckViewModel extends AndroidViewModel {

    private final MutableLiveData<List<HealthChecker.HealthIssue>> issuesLiveData = new MutableLiveData<>();
    private final MutableLiveData<HealthChecker.HealthSummary> summaryLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(true);

    public HealthCheckViewModel(@NonNull Application application) {
        super(application);
        runCheck();
    }

    public LiveData<List<HealthChecker.HealthIssue>> getIssuesLiveData() {
        return issuesLiveData;
    }

    public LiveData<HealthChecker.HealthSummary> getSummaryLiveData() {
        return summaryLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public void runCheck() {
        isLoadingLiveData.setValue(true);
        AppExecutors.getInstance().diskIO().execute(() -> {
            String familyId = Prefs.getInstance(getApplication()).getFamilyId();
            AppDatabase db = AppDatabase.getInstance(getApplication());

            List<PersonEntity> people;
            if (familyId != null && !familyId.isEmpty()) {
                people = db.personDao().getAllPeopleSync(familyId);
            } else {
                people = db.personDao().getAllPeopleGlobalSync();
            }

            List<HealthChecker.HealthIssue> issues = HealthChecker.checkTreeHealth(people);

            HealthChecker.HealthSummary summary = new HealthChecker.HealthSummary();
            summary.totalIssues = issues.size();
            for (HealthChecker.HealthIssue issue : issues) {
                if (issue.severity == HealthChecker.Severity.HIGH) summary.highCount++;
                else if (issue.severity == HealthChecker.Severity.MEDIUM) summary.mediumCount++;
                else if (issue.severity == HealthChecker.Severity.LOW) summary.lowCount++;
            }

            AppExecutors.getInstance().mainThread().execute(() -> {
                summaryLiveData.setValue(summary);
                issuesLiveData.setValue(issues);
                isLoadingLiveData.setValue(false);
            });
        });
    }
}
