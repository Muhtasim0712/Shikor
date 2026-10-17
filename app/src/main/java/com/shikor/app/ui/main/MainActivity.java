package com.shikor.app.ui.main;

import android.os.Bundle;

import com.shikor.app.R;
import com.shikor.app.databinding.ActivityMainBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.util.Prefs;

/**
 * MainActivity - Primary container activity for Shikor.
 */
public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        String familyName = Prefs.getInstance(this).getFamilyName();
        if (familyName != null && !familyName.isEmpty()) {
            binding.toolbar.setTitle(familyName);
            binding.tvWelcomeMain.setText(getString(R.string.family_setup_title) + ": " + familyName);
        }
    }
}
