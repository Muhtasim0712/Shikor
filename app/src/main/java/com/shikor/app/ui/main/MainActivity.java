package com.shikor.app.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shikor.app.R;
import com.shikor.app.databinding.ActivityMainBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.util.Prefs;

/**
 * MainActivity - Primary container activity for Shikor.
 * Holds BottomNavigationView and manages Fragments (Tree, Members, Timeline, Settings).
 *
 * CONCEPT: BottomNavigationView with Fragment switching, View Binding, and lifecycle logging.
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
        }

        setupBottomNavigation();

        // Default screen is MembersFragment (or TreeFragment)
        if (savedInstanceState == null) {
            loadFragment(new MembersFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_members);
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_members) {
                loadFragment(new MembersFragment());
                return true;
            } else if (itemId == R.id.nav_tree) {
                // Future phase will load TreeFragment
                return true;
            } else if (itemId == R.id.nav_timeline) {
                // Future phase will load TimelineFragment
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Future phase will load SettingsFragment
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navHostContainer, fragment)
                .commit();
    }
}
