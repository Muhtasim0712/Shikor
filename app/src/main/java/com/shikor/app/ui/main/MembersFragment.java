package com.shikor.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.shikor.app.R;
import com.shikor.app.adapter.MemberAdapter;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.FragmentMembersBinding;
import com.shikor.app.ui.person.AddEditPersonActivity;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.ui.person.PersonViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * MembersFragment - Displays family members list with search, filter chips,
 * and swipe-to-delete with Snackbar undo.
 *
 * CONCEPT: RecyclerView + ListAdapter + DiffUtil, ItemTouchHelper swipe-to-delete with Snackbar Undo,
 * TextWatcher search, and Filter Chips.
 */
public class MembersFragment extends Fragment {

    private FragmentMembersBinding binding;
    private PersonViewModel personViewModel;
    private MemberAdapter memberAdapter;
    private List<PersonEntity> allMembersList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int currentFilterType = 0; // 0=All, 1=Alive, 2=Deceased

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        personViewModel = new ViewModelProvider(requireActivity()).get(PersonViewModel.class);
        personViewModel.syncFromCloud();

        setupRecyclerView();
        setupSearchAndFilters();
        setupSwipeToDelete();
        setupFab();
        observeMembers();
    }

    private void setupRecyclerView() {
        memberAdapter = new MemberAdapter(person -> {
            // CONCEPT: Explicit Intent to PersonDetailActivity with personId extra
            Intent intent = new Intent(requireContext(), PersonDetailActivity.class);
            intent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, person.getId());
            startActivity(intent);
        });

        binding.rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMembers.setAdapter(memberAdapter);
    }

    /**
     * CONCEPT: ItemTouchHelper for swipe-to-delete with Snackbar Undo
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position >= 0 && position < memberAdapter.getCurrentList().size()) {
                    PersonEntity personToDelete = memberAdapter.getPersonAt(position);
                    personViewModel.deletePerson(personToDelete);

                    // CONCEPT: Snackbar Undo pattern
                    Snackbar.make(binding.getRoot(), personToDelete.getFullName() + " removed", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> personViewModel.savePerson(personToDelete))
                            .show();
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvMembers);
    }

    /**
     * CONCEPT: TextWatcher for real-time member search
     */
    private void setupSearchAndFilters() {
        binding.etSearchMembers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                binding.btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> binding.etSearchMembers.setText(""));

        // Filter chips (All, Living, Deceased)
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipFilterAlive)) {
                currentFilterType = 1;
            } else if (checkedIds.contains(R.id.chipFilterDeceased)) {
                currentFilterType = 2;
            } else {
                currentFilterType = 0;
            }
            applyFilterAndSearch();
        });
    }

    private void setupFab() {
        binding.fabAddPerson.setOnClickListener(v -> {
            // CONCEPT: Explicit Intent to AddEditPersonActivity
            Intent intent = new Intent(requireContext(), AddEditPersonActivity.class);
            startActivity(intent);
        });
    }

    private void observeMembers() {
        personViewModel.getPeople().observe(getViewLifecycleOwner(), people -> {
            allMembersList = people != null ? people : new ArrayList<>();
            applyFilterAndSearch();
        });
    }

    private void applyFilterAndSearch() {
        List<PersonEntity> filtered = new ArrayList<>();

        for (PersonEntity p : allMembersList) {
            // Status check
            if (currentFilterType == 1 && p.isDeceased()) continue;
            if (currentFilterType == 2 && !p.isDeceased()) continue;

            // Search query check
            if (!currentSearchQuery.isEmpty()) {
                String q = currentSearchQuery.toLowerCase();
                boolean matchesName = p.getFullName() != null && p.getFullName().toLowerCase().contains(q);
                boolean matchesNick = p.getNickName() != null && p.getNickName().toLowerCase().contains(q);
                boolean matchesTown = p.getHometown() != null && p.getHometown().toLowerCase().contains(q);
                if (!matchesName && !matchesNick && !matchesTown) {
                    continue;
                }
            }

            filtered.add(p);
        }

        memberAdapter.submitList(filtered);

        // CONCEPT: Empty state handling with layout_empty_state
        if (filtered.isEmpty()) {
            binding.emptyState.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.emptyState.tvEmptyTitle.setText("No relatives found");
            binding.emptyState.tvEmptySubtitle.setText("Add family members using the + button below");
        } else {
            binding.emptyState.emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
