package com.shikor.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.shikor.app.R;
import com.shikor.app.databinding.ActivityRegisterBinding;
import com.shikor.app.ui.base.BaseActivity;

/**
 * RegisterActivity - Handles new user registration with Firebase Auth and Firestore.
 *
 * CONCEPT: View Binding, Input Validation with TextInputLayout, and ViewModel-driven state.
 */
public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupTextWatchers();
        setupClickListeners();
        observeViewModel();
    }

    private void setupTextWatchers() {
        binding.etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilName.setError(null);
            }
        });

        binding.etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
            }
        });

        binding.etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }
        });

        binding.etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilConfirmPassword.setError(null);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> attemptRegister());

        binding.btnGoToLogin.setOnClickListener(v -> {
            // Finish and return to Login
            finish();
        });
    }

    private void attemptRegister() {
        hideKeyboard();

        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        boolean cancel = false;

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_empty_field));
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_empty_field));
            cancel = true;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_short_password));
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_empty_field));
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError(getString(R.string.error_empty_field));
            cancel = true;
        }

        if (!cancel) {
            authViewModel.register(name, email, password);
        }
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.loadingOverlay.loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showSnackbar(error);
            }
        });

        authViewModel.getAuthSuccess().observe(this, user -> {
            if (user != null) {
                // New user registered -> Navigate to FamilySetupActivity
                // CONCEPT: Explicit Intent to FamilySetupActivity
                Intent intent = new Intent(RegisterActivity.this, FamilySetupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
