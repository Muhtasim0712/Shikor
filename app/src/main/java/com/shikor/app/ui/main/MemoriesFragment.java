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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.shikor.app.R;
import com.shikor.app.adapter.MemoriesAdapter;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.FragmentMemoriesBinding;
import com.shikor.app.ui.memory.AddMemoryActivity;
import com.shikor.app.ui.memory.MemoryDetailActivity;
import com.shikor.app.ui.memory.MemoryViewModel;
import com.shikor.app.ui.person.PersonViewModel;
import com.shikor.app.worker.UploadMemoryWorker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MemoriesFragment - Displays family memories ordered chronologically with year headers (Phase 4).
 * CONCEPT: RecyclerView with multi-view types, filters (search/year/person), pull to refresh, FAB.
 */
public class MemoriesFragment extends Fragment {

    private FragmentMemoriesBinding binding;
    private MemoryViewModel memoryViewModel;
    private PersonViewModel personViewModel;

    private MemoriesAdapter memoriesAdapter;
    private List<MemoryEntity> allMemories = new ArrayList<>();
    private final Map<String, String> personNameMap = new HashMap<>();

    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMemoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        memoryViewModel = new ViewModelProvider(requireActivity()).get(MemoryViewModel.class);
        personViewModel = new ViewModelProvider(this).get(PersonViewModel.class);

        setupRecyclerView();
        observeData();

        binding.fabAddMemory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddMemoryActivity.class);
            startActivity(intent);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.etSearchMemories.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                filterAndSubmitMemories();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        memoriesAdapter = new MemoriesAdapter(new MemoriesAdapter.OnMemoryClickListener() {
            @Override
            public void onMemoryClick(MemoryEntity memory) {
                Intent intent = new Intent(requireContext(), MemoryDetailActivity.class);
                intent.putExtra(MemoryDetailActivity.EXTRA_MEMORY_ID, memory.getId());
                startActivity(intent);
            }

            @Override
            public void onRetryClick(MemoryEntity memory) {
                Constraints constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder(UploadMemoryWorker.class)
                        .setInputData(new androidx.work.Data.Builder().putString(UploadMemoryWorker.KEY_MEMORY_ID, memory.getId()).build())
                        .setConstraints(constraints)
                        .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                        .build();

                WorkManager.getInstance(requireContext()).enqueueUniqueWork(
                        "upload_memory_" + memory.getId(),
                        ExistingWorkPolicy.REPLACE,
                        uploadWork
                );
                showSnackbar("Retrying memory upload...");
            }
        });

        binding.rvMemories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMemories.setAdapter(memoriesAdapter);
    }

    private void observeData() {
        binding.progressMemories.setVisibility(View.VISIBLE);

        personViewModel.getPeople().observe(getViewLifecycleOwner(), people -> {
            if (people != null) {
                personNameMap.clear();
                for (PersonEntity p : people) {
                    personNameMap.put(p.getId(), p.getFullName());
                }
                memoriesAdapter.setPersonNames(personNameMap);
            }
        });

        memoryViewModel.getMemories().observe(getViewLifecycleOwner(), memories -> {
            binding.progressMemories.setVisibility(View.GONE);
            allMemories = memories != null ? memories : new ArrayList<>();
            filterAndSubmitMemories();
        });
    }

    private void filterAndSubmitMemories() {
        List<MemoryEntity> filtered = new ArrayList<>();
        for (MemoryEntity m : allMemories) {
            if (!searchQuery.isEmpty()) {
                boolean matchTitle = m.getTitle() != null && m.getTitle().toLowerCase().contains(searchQuery);
                boolean matchDesc = m.getDescription() != null && m.getDescription().toLowerCase().contains(searchQuery);
                boolean matchUploader = m.getUploaderName() != null && m.getUploaderName().toLowerCase().contains(searchQuery);
                if (!matchTitle && !matchDesc && !matchUploader) {
                    continue;
                }
            }
            filtered.add(m);
        }

        List<MemoriesAdapter.MemoryItem> items = buildMemoryItemsWithHeaders(filtered);
        if (!items.isEmpty()) {
            binding.memoriesEmptyState.emptyStateLayout.setVisibility(View.GONE);
            binding.rvMemories.setVisibility(View.VISIBLE);
            memoriesAdapter.submitList(items);
        } else {
            binding.memoriesEmptyState.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.rvMemories.setVisibility(View.GONE);
            binding.memoriesEmptyState.tvEmptyTitle.setText(R.string.empty_memories_title);
            binding.memoriesEmptyState.tvEmptySubtitle.setText(R.string.empty_memories_subtitle);
        }
    }

    private List<MemoriesAdapter.MemoryItem> buildMemoryItemsWithHeaders(List<MemoryEntity> memories) {
        Map<Integer, List<MemoryEntity>> groupedByYear = new LinkedHashMap<>();
        for (MemoryEntity m : memories) {
            int year = m.getYear() > 0 ? m.getYear() : Calendar.getInstance().get(Calendar.YEAR);
            groupedByYear.computeIfAbsent(year, k -> new ArrayList<>()).add(m);
        }

        List<Integer> years = new ArrayList<>(groupedByYear.keySet());
        years.sort((a, b) -> Integer.compare(b, a));

        List<MemoriesAdapter.MemoryItem> result = new ArrayList<>();
        for (int year : years) {
            List<MemoryEntity> yearMemories = groupedByYear.get(year);
            if (yearMemories != null && !yearMemories.isEmpty()) {
                result.add(MemoriesAdapter.MemoryItem.header(year, yearMemories.size()));
                for (MemoryEntity m : yearMemories) {
                    result.add(MemoriesAdapter.MemoryItem.card(m));
                }
            }
        }
        return result;
    }

    private void showSnackbar(String message) {
        if (getView() != null) {
            com.google.android.material.snackbar.Snackbar.make(getView(), message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        }
    }
}
