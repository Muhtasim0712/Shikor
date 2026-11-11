package com.shikor.app.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.shikor.app.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shikor.app.adapter.TimelineAdapter;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.databinding.DialogAddTimelinePhotoBinding;
import com.shikor.app.databinding.FragmentTimelineBinding;
import com.shikor.app.ui.person.PersonViewModel;
import com.shikor.app.ui.story.StoryDetailActivity;
import com.shikor.app.ui.story.StoryViewModel;
import com.shikor.app.ui.timeline.TimelineItem;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TimelineFragment - Displays the family photo timeline ordered chronologically with Year Headers.
 *
 * CONCEPT: Photo timeline with year headers, sectioned milestones, and dynamic dialog to add photos.
 */
public class TimelineFragment extends Fragment {

    private FragmentTimelineBinding binding;
    private StoryViewModel storyViewModel;
    private PersonViewModel personViewModel;

    private TimelineAdapter timelineAdapter;
    private List<PersonEntity> familyMembers = new ArrayList<>();
    private final Map<String, String> personNameMap = new HashMap<>();

    private List<StoryEntity> latestStories = new ArrayList<>();
    private List<PhotoEntity> latestPhotos = new ArrayList<>();

    // State for Dialog photo picking
    private String dialogSelectedPhotoPath = null;
    private DialogAddTimelinePhotoBinding dialogBinding = null;

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && dialogBinding != null) {
                    saveDialogPhotoFromUri(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTimelineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storyViewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        personViewModel = new ViewModelProvider(requireActivity()).get(PersonViewModel.class);

        storyViewModel.syncFromCloud();

        setupRecyclerView();
        observeData();

        binding.fabAddTimelinePhoto.setOnClickListener(v -> showAddPhotoDialog());
    }

    private void setupRecyclerView() {
        timelineAdapter = new TimelineAdapter(
                photo -> showPhotoDetailDialog(photo),
                story -> {
                    Intent intent = new Intent(requireContext(), StoryDetailActivity.class);
                    intent.putExtra(StoryDetailActivity.EXTRA_STORY_ID, story.getId());
                    startActivity(intent);
                }
        );

        binding.rvTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTimeline.setAdapter(timelineAdapter);
    }

    private void observeData() {
        // Observe People for Name lookup and Spinner
        personViewModel.getPeople().observe(getViewLifecycleOwner(), people -> {
            if (people != null) {
                familyMembers = people;
                personNameMap.clear();
                for (PersonEntity p : people) {
                    personNameMap.put(p.getId(), p.getFullName());
                }
                timelineAdapter.setPersonNames(personNameMap);
            }
        });

        // Observe Stories
        storyViewModel.getAllStories().observe(getViewLifecycleOwner(), stories -> {
            latestStories = stories != null ? stories : new ArrayList<>();
            updateTimeline();
        });

        // Observe Timeline Photos
        storyViewModel.getTimelinePhotos().observe(getViewLifecycleOwner(), photos -> {
            latestPhotos = photos != null ? photos : new ArrayList<>();
            updateTimeline();
        });
    }

    private void updateTimeline() {
        if (binding == null) return;
        List<TimelineItem> items = buildCombinedTimelineItems(latestStories, latestPhotos);
        if (!items.isEmpty()) {
            binding.timelineEmptyState.emptyStateLayout.setVisibility(View.GONE);
            binding.rvTimeline.setVisibility(View.VISIBLE);
            timelineAdapter.submitList(items);
        } else {
            binding.timelineEmptyState.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.rvTimeline.setVisibility(View.GONE);
            binding.timelineEmptyState.tvEmptyTitle.setText(R.string.empty_timeline_title);
            binding.timelineEmptyState.tvEmptySubtitle.setText(R.string.empty_timeline_subtitle);
        }
    }

    private List<TimelineItem> buildCombinedTimelineItems(List<StoryEntity> stories, List<PhotoEntity> photos) {
        Map<Integer, List<TimelineItem>> groupedByYear = new java.util.LinkedHashMap<>();

        if (stories != null) {
            for (StoryEntity s : stories) {
                TimelineItem item = TimelineItem.story(s);
                groupedByYear.computeIfAbsent(item.getYear(), k -> new ArrayList<>()).add(item);
            }
        }

        if (photos != null) {
            for (PhotoEntity p : photos) {
                TimelineItem item = TimelineItem.photo(p);
                groupedByYear.computeIfAbsent(item.getYear(), k -> new ArrayList<>()).add(item);
            }
        }

        List<Integer> years = new ArrayList<>(groupedByYear.keySet());
        years.sort((a, b) -> Integer.compare(b, a));

        List<TimelineItem> result = new ArrayList<>();
        for (int year : years) {
            List<TimelineItem> itemsInYear = groupedByYear.get(year);
            if (itemsInYear != null && !itemsInYear.isEmpty()) {
                result.add(TimelineItem.header(year, itemsInYear.size()));
                result.addAll(itemsInYear);
            }
        }
        return result;
    }

    private void showAddPhotoDialog() {
        dialogSelectedPhotoPath = null;
        dialogBinding = DialogAddTimelinePhotoBinding.inflate(getLayoutInflater());

        // Default year to current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        dialogBinding.etYear.setText(String.valueOf(currentYear));

        // Populate Tagged Members ChipGroup
        dialogBinding.chipGroupTaggedMembers.removeAllViews();
        if (familyMembers.isEmpty()) {
            Chip emptyChip = new Chip(requireContext());
            emptyChip.setText("No family members found");
            emptyChip.setEnabled(false);
            emptyChip.setCheckable(false);
            dialogBinding.chipGroupTaggedMembers.addView(emptyChip);
        } else {
            for (PersonEntity p : familyMembers) {
                Chip chip = new Chip(requireContext());
                chip.setText(p.getFullName());
                chip.setTag(p.getId());
                chip.setCheckable(true);
                chip.setClickable(true);
                chip.setCheckedIconVisible(true);
                chip.setEnsureMinTouchTargetSize(true);
                dialogBinding.chipGroupTaggedMembers.addView(chip);
            }
        }

        dialogBinding.layoutPhotoPicker.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Memory to Timeline")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", null) // Overridden below for validation
                .setNegativeButton("Cancel", (d, w) -> dialogBinding = null)
                .create();

        dialog.show();

        // Custom positive button listener to prevent dismissal on validation failure
        ((androidx.appcompat.app.AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (dialogSelectedPhotoPath == null) {
                Toast.makeText(requireContext(), "Please choose a photo", Toast.LENGTH_SHORT).show();
                return;
            }

            String yearStr = dialogBinding.etYear.getText() != null ? dialogBinding.etYear.getText().toString().trim() : "";
            if (TextUtils.isEmpty(yearStr)) {
                dialogBinding.tilYear.setError("Please enter the year");
                return;
            }
            int year;
            try {
                year = Integer.parseInt(yearStr);
                if (year < 1800 || year > 2100) {
                    dialogBinding.tilYear.setError("Enter a valid year (1800 - 2100)");
                    return;
                }
            } catch (NumberFormatException e) {
                dialogBinding.tilYear.setError("Invalid year format");
                return;
            }

            String caption = dialogBinding.etCaption.getText() != null ? dialogBinding.etCaption.getText().toString().trim() : "";

            List<String> selectedPersonIds = new ArrayList<>();
            for (int i = 0; i < dialogBinding.chipGroupTaggedMembers.getChildCount(); i++) {
                View child = dialogBinding.chipGroupTaggedMembers.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.isChecked()) {
                        String pId = (String) chip.getTag();
                        if (pId != null && !pId.isEmpty()) {
                            selectedPersonIds.add(pId);
                        }
                    }
                }
            }

            if (selectedPersonIds.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one family member to tag", Toast.LENGTH_SHORT).show();
                return;
            }

            String primaryPersonId = selectedPersonIds.get(0);
            String familyId = Prefs.getInstance(requireContext()).getFamilyId();
            if (familyId == null || familyId.trim().isEmpty()) {
                familyId = "default_family";
            }
            String photoId = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();

            PhotoEntity photo = new PhotoEntity(
                    photoId,
                    primaryPersonId,
                    familyId != null ? familyId : "",
                    dialogSelectedPhotoPath,
                    caption,
                    year,
                    selectedPersonIds,
                    now,
                    now,
                    PhotoEntity.SYNC_STATE_PENDING
            );

            storyViewModel.savePhoto(photo);
            Toast.makeText(requireContext(), "Photo added to timeline!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            dialogBinding = null;
        });
    }

    private void saveDialogPhotoFromUri(Uri uri) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                File dir = new File(requireContext().getFilesDir(), "timeline_photos");
                if (!dir.exists()) dir.mkdirs();

                File destFile = new File(dir, "timeline_" + System.currentTimeMillis() + ".jpg");
                try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }

                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (dialogBinding != null) {
                        dialogSelectedPhotoPath = destFile.getAbsolutePath();
                        dialogBinding.layoutPickPrompt.setVisibility(View.GONE);
                        dialogBinding.ivDialogPhotoPreview.setVisibility(View.VISIBLE);
                        Glide.with(this).load(destFile).into(dialogBinding.ivDialogPhotoPreview);
                    }
                });
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        Toast.makeText(requireContext(), "Failed to save photo: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showPhotoDetailDialog(PhotoEntity photo) {
        String title = "Milestone Year: " + photo.getYear();
        String message = photo.getCaption() != null ? photo.getCaption() : "";

        List<String> taggedIds = photo.getTaggedPersonIds();
        if ((taggedIds == null || taggedIds.isEmpty()) && photo.getPersonId() != null && !photo.getPersonId().isEmpty()) {
            taggedIds = java.util.Collections.singletonList(photo.getPersonId());
        }

        if (taggedIds != null && !taggedIds.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String pid : taggedIds) {
                String name = personNameMap.get(pid);
                if (name != null && !name.isEmpty()) {
                    names.add(name);
                }
            }
            if (!names.isEmpty()) {
                String label = names.size() == 1 ? "Tagged Relative: " : "Tagged Relatives: ";
                message = (message.isEmpty() ? "" : message + "\n\n") + label + TextUtils.join(", ", names);
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Close", null)
                .setNegativeButton("Delete", (dialog, which) -> {
                    storyViewModel.deletePhoto(photo);
                    Toast.makeText(requireContext(), "Photo removed from timeline", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dialogBinding = null;
    }
}
