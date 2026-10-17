package com.shikor.app.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.shikor.app.R;
import com.shikor.app.databinding.ActivityFamilySetupBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.main.MainActivity;

/**
 * FamilySetupActivity - Create a new family (generates 6-char code) or join an existing family.
 *
 * CONCEPT: View Binding, Explicit Intents, Implicit Intents (ACTION_SEND & ACTION_SENDTO),
 * and Firestore Family data handling.
 */
public class FamilySetupActivity extends BaseActivity {

    private ActivityFamilySetupBinding binding;
    private AuthViewModel authViewModel;
    private String generatedOrJoinedCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamilySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnCreateFamily.setOnClickListener(v -> {
            hideKeyboard();
            String familyName = binding.etFamilyName.getText() != null
                    ? binding.etFamilyName.getText().toString().trim() : "";
            if (TextUtils.isEmpty(familyName)) {
                binding.tilFamilyName.setError(getString(R.string.error_empty_field));
                return;
            }
            binding.tilFamilyName.setError(null);
            authViewModel.createFamily(familyName);
        });

        binding.btnJoinFamily.setOnClickListener(v -> {
            hideKeyboard();
            String code = binding.etFamilyCode.getText() != null
                    ? binding.etFamilyCode.getText().toString().trim() : "";
            if (TextUtils.isEmpty(code) || code.length() != 6) {
                binding.tilFamilyCode.setError(getString(R.string.error_invalid_code));
                return;
            }
            binding.tilFamilyCode.setError(null);
            authViewModel.joinFamily(code);
        });

        // CONCEPT: Implicit Intent to share family code via apps/SMS
        binding.btnShareCode.setOnClickListener(v -> shareFamilyCode(generatedOrJoinedCode));

        // CONCEPT: Explicit Intent to launch MainActivity
        binding.btnContinueToMain.setOnClickListener(v -> {
            Intent intent = new Intent(FamilySetupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * CONCEPT: Implicit Intent with ACTION_SEND and ACTION_SENDTO
     */
    private void shareFamilyCode(String code) {
        String message = getString(R.string.family_code_invite_message, code);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Family Invite");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_family_code)));
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.loadingOverlay.loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnCreateFamily.setEnabled(!isLoading);
            binding.btnJoinFamily.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showSnackbar(error);
            }
        });

        authViewModel.getFamilySuccess().observe(this, code -> {
            if (code != null) {
                generatedOrJoinedCode = code;
                binding.cardCreateFamily.setVisibility(View.GONE);
                binding.cardJoinFamily.setVisibility(View.GONE);
                binding.cardSuccess.setVisibility(View.VISIBLE);
                binding.tvDisplayCode.setText(code);
            }
        });
    }
}
