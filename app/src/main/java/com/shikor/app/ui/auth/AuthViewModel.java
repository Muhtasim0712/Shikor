package com.shikor.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.shikor.app.data.remote.AuthRepository;
import com.shikor.app.util.Prefs;

/**
 * AuthViewModel - Manages UI state and business logic for Login, Register, and Family Setup.
 * CONCEPT: MVVM Architecture with ViewModel & LiveData
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> authSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> resetPasswordSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> familySuccess = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = AuthRepository.getInstance(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<FirebaseUser> getAuthSuccess() {
        return authSuccess;
    }

    public LiveData<Boolean> getResetPasswordSuccess() {
        return resetPasswordSuccess;
    }

    public LiveData<String> getFamilySuccess() {
        return familySuccess;
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    public boolean hasFamily() {
        String familyId = Prefs.getInstance(getApplication()).getFamilyId();
        return familyId != null && !familyId.isEmpty();
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                isLoading.setValue(false);
                authSuccess.setValue(user);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void register(String name, String email, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.register(name, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                isLoading.setValue(false);
                authSuccess.setValue(user);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void resetPassword(String email) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.sendPasswordReset(email, new AuthRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                resetPasswordSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void createFamily(String familyName) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.createFamily(familyName, new AuthRepository.FamilyCallback() {
            @Override
            public void onSuccess(String familyId, String name, String familyCode) {
                isLoading.setValue(false);
                familySuccess.setValue(familyCode);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void joinFamily(String code) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.joinFamily(code, new AuthRepository.FamilyCallback() {
            @Override
            public void onSuccess(String familyId, String familyName, String familyCode) {
                isLoading.setValue(false);
                familySuccess.setValue(familyCode);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}
