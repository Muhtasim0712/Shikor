package com.shikor.app.ui.person;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.repository.FamilyRepository;
import com.shikor.app.util.Prefs;

import java.util.List;

/**
 * PersonViewModel - ViewModel for Person list, CRUD, and filtering.
 * CONCEPT: MVVM Architecture with ViewModel, LiveData, and Room Repository.
 */
public class PersonViewModel extends AndroidViewModel {

    private final FamilyRepository repository;
    private final String currentFamilyId;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> statusFilter = new MutableLiveData<>(0); // 0=All, 1=Alive, 2=Deceased
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> operationError = new MutableLiveData<>();

    public PersonViewModel(@NonNull Application application) {
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

    public LiveData<List<PersonEntity>> getPeople() {
        if (currentFamilyId == null || currentFamilyId.isEmpty()) {
            return new MutableLiveData<>();
        }
        return repository.getAllPeople(currentFamilyId);
    }

    public LiveData<PersonEntity> getPerson(String personId) {
        return repository.getPersonById(personId);
    }

    public LiveData<List<PersonEntity>> getChildren(String parentId) {
        return repository.getChildren(currentFamilyId, parentId);
    }

    public LiveData<List<PersonEntity>> getSiblings(String personId, String fatherId, String motherId) {
        return repository.getSiblings(currentFamilyId, personId, fatherId, motherId);
    }

    public LiveData<List<PersonEntity>> searchPeople(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getPeople();
        }
        return repository.searchPeople(currentFamilyId, query.trim());
    }

    public LiveData<List<PersonEntity>> filterByStatus(boolean isDeceased) {
        return repository.getPeopleByStatus(currentFamilyId, isDeceased);
    }

    public void savePerson(PersonEntity person) {
        repository.insertPerson(person, new FamilyRepository.RepositoryCallback<Void>() {
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

    public void updatePerson(PersonEntity person) {
        repository.updatePerson(person, new FamilyRepository.RepositoryCallback<Void>() {
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

    public void deletePerson(PersonEntity person) {
        repository.deletePerson(person, new FamilyRepository.RepositoryCallback<Void>() {
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

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public LiveData<String> getOperationError() {
        return operationError;
    }
}
