package com.shikor.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.FragmentTreeBinding;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.ui.person.PersonViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * TreeFragment - Displays the interactive family tree canvas view and root controls.
 *
 * CONCEPT: Fragment hosting Custom View 1 (FamilyTreeView) with explicit Intent navigation
 * to PersonDetailActivity on node clicks.
 */
public class TreeFragment extends Fragment {

    private FragmentTreeBinding binding;
    private PersonViewModel personViewModel;
    private List<PersonEntity> membersList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTreeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        personViewModel = new ViewModelProvider(requireActivity()).get(PersonViewModel.class);

        setupTreeView();
        setupControls();
        observeFamilyMembers();
    }

    private void setupTreeView() {
        // CONCEPT: Explicit Intent to PersonDetailActivity on node tap
        binding.familyTreeView.setOnPersonClickListener(personId -> {
            Intent intent = new Intent(requireContext(), PersonDetailActivity.class);
            intent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, personId);
            startActivity(intent);
        });

        binding.btnReCenter.setOnClickListener(v -> binding.familyTreeView.reCenterTree());
    }

    private void setupControls() {
        binding.spinnerTreeRoot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    binding.familyTreeView.setRoot(null);
                } else if (position - 1 < membersList.size()) {
                    binding.familyTreeView.setRoot(membersList.get(position - 1).getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void observeFamilyMembers() {
        personViewModel.getPeople().observe(getViewLifecycleOwner(), people -> {
            if (people != null && !people.isEmpty()) {
                membersList = people;
                binding.treeEmptyState.emptyStateLayout.setVisibility(View.GONE);
                binding.familyTreeView.setVisibility(View.VISIBLE);

                // Populate tree view with data
                binding.familyTreeView.setPeople(people);

                // Populate Root Selector Spinner
                List<String> names = new ArrayList<>();
                names.add("All Generations (Default)");
                for (PersonEntity p : people) {
                    names.add(p.getFullName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item, names
                );
                binding.spinnerTreeRoot.setAdapter(adapter);
            } else {
                binding.treeEmptyState.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.familyTreeView.setVisibility(View.GONE);
                binding.treeEmptyState.tvEmptyTitle.setText("Family Tree is Empty");
                binding.treeEmptyState.tvEmptySubtitle.setText("Add your family members from the Members tab to see your tree grow!");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
