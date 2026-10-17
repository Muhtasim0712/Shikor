package com.shikor.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.shikor.app.R;
import com.shikor.app.databinding.ActivityLoginBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.main.MainActivity;
import com.shikor.app.util.Prefs;

/**
 * LoginActivity - Handles email & password login with Firebase Auth.
 *
 * CONCEPT: View Binding, Event handling with lambda and anonymous inner class,
 * TextWatcher, TextInputLayout errors, and explicit Intents.
 */
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupTextWatchers();
        setupClickListeners();
        observeViewModel();
    }

    /**
     * CONCEPT: TextWatcher to dynamically validate and clear errors
     */
    private void setupTextWatchers() {
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        // CONCEPT: Event handling via Lambda expression
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // CONCEPT: Event handling via Anonymous Inner Class (to demonstrate both styles)
        binding.btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CONCEPT: Explicit Intent to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        binding.btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void attemptLogin() {
        hideKeyboard();

        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        boolean cancel = false;

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

        if (!cancel) {
            authViewModel.login(email, password);
        }
    }

    private void showForgotPasswordDialog() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            binding.etEmail.requestFocus();
            return;
        }
        authViewModel.resetPassword(email);
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.loadingOverlay.loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showSnackbar(error);
            }
        });

        authViewModel.getResetPasswordSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.reset_password_sent, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getAuthSuccess().observe(this, user -> {
            if (user != null) {
                String familyId = Prefs.getInstance(this).getFamilyId();
                if (familyId != null && !familyId.isEmpty()) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LoginActivity.this, FamilySetupActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }
}
