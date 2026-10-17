package com.shikor.app.ui.person;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.shikor.app.adapter.StoryAdapter;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.ActivityPersonDetailBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.story.AddStoryActivity;
import com.shikor.app.ui.story.StoryDetailActivity;
import com.shikor.app.ui.story.StoryViewModel;

import java.util.List;

/**
 * PersonDetailActivity - Displays full profile, relatives, stories, and triggers implicit intents
 * (ACTION_DIAL, ACTION_VIEW geo:, ACTION_SEND).
 *
 * CONCEPT: Implicit Intents:
 * - ACTION_DIAL to call relative
 * - ACTION_VIEW with geo: URI to open hometown in Google Maps
 * - ACTION_SEND to share profile details
 */
public class PersonDetailActivity extends BaseActivity {

    public static final String EXTRA_PERSON_ID = "extra_person_id";

    private ActivityPersonDetailBinding binding;
    private PersonViewModel personViewModel;
    private StoryViewModel storyViewModel;
    private StoryAdapter storyAdapter;
    private String personId;
    private PersonEntity currentPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        personId = getIntent().getStringExtra(EXTRA_PERSON_ID);
        if (personId == null || personId.isEmpty()) {
            Toast.makeText(this, "Relative ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        personViewModel = new ViewModelProvider(this).get(PersonViewModel.class);
        storyViewModel = new ViewModelProvider(this).get(StoryViewModel.class);

        setupClickListeners();
        setupStoriesRecyclerView();
        observePerson();
        observeStories();
    }

    private void setupStoriesRecyclerView() {
        storyAdapter = new StoryAdapter(story -> {
            Intent intent = new Intent(PersonDetailActivity.this, StoryDetailActivity.class);
            intent.putExtra(StoryDetailActivity.EXTRA_STORY_ID, story.getId());
            startActivity(intent);
        });
        binding.rvPersonStories.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPersonStories.setAdapter(storyAdapter);
    }

    private void observeStories() {
        storyViewModel.getStoriesForPerson(personId).observe(this, stories -> {
            if (stories != null && !stories.isEmpty()) {
                binding.rvPersonStories.setVisibility(View.VISIBLE);
                binding.tvNoStories.setVisibility(View.GONE);
                storyAdapter.submitList(stories);
            } else {
                binding.rvPersonStories.setVisibility(View.GONE);
                binding.tvNoStories.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupClickListeners() {
        // Add Story
        binding.btnAddStory.setOnClickListener(v -> {
            Intent intent = new Intent(PersonDetailActivity.this, AddStoryActivity.class);
            intent.putExtra(AddStoryActivity.EXTRA_PERSON_ID, personId);
            startActivity(intent);
        });

        // CONCEPT: Implicit Intent with ACTION_DIAL
        binding.btnCall.setOnClickListener(v -> {
            if (currentPerson != null && !TextUtils.isEmpty(currentPerson.getPhoneNumber())) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + currentPerson.getPhoneNumber().trim()));
                startActivity(dialIntent);
            } else {
                showSnackbar("No phone number recorded for this relative.");
            }
        });

        // CONCEPT: Implicit Intent with ACTION_VIEW and geo: URI for maps
        binding.btnMaps.setOnClickListener(v -> {
            if (currentPerson != null && !TextUtils.isEmpty(currentPerson.getHometown())) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(currentPerson.getHometown()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(mapIntent);
            } else {
                showSnackbar("No hometown recorded for this relative.");
            }
        });

        // CONCEPT: Implicit Intent with ACTION_SEND to share relative profile
        binding.btnShare.setOnClickListener(v -> {
            if (currentPerson != null) {
                String shareText = "Family Member in Shikor Archive:\n" +
                        "Name: " + currentPerson.getFullName() + "\n" +
                        (currentPerson.getNickName() != null ? "Nickname: " + currentPerson.getNickName() + "\n" : "") +
                        "Lifespan: " + currentPerson.getLifeSpan() + "\n" +
                        (currentPerson.getHometown() != null ? "Hometown: " + currentPerson.getHometown() + "\n" : "") +
                        (currentPerson.getOccupation() != null ? "Occupation: " + currentPerson.getOccupation() : "");

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentPerson.getFullName() + " - Shikor");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share Relative Profile"));
            }
        });

        // Edit Relative
        binding.btnEditPerson.setOnClickListener(v -> {
            Intent intent = new Intent(PersonDetailActivity.this, AddEditPersonActivity.class);
            intent.putExtra(AddEditPersonActivity.EXTRA_PERSON_ID, personId);
            startActivity(intent);
        });
    }

    private void observePerson() {
        personViewModel.getPerson(personId).observe(this, person -> {
            if (person != null) {
                currentPerson = person;
                populateUI(person);
                observeRelatives(person);
            }
        });
    }

    private void populateUI(PersonEntity person) {
        binding.toolbar.setTitle(person.getFullName());
        binding.tvDetailFullName.setText(person.getFullName());

        if (person.getNickName() != null && !person.getNickName().isEmpty()) {
            binding.tvDetailNickName.setText("(" + person.getNickName() + ")");
            binding.tvDetailNickName.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailNickName.setVisibility(View.GONE);
        }

        binding.tvDetailLifespan.setText(person.getLifeSpan());

        // Setup Avatar
        binding.detailAvatar.setGender(person.getGender());
        binding.detailAvatar.setDeceased(person.isDeceased());
        binding.detailAvatar.setInitials(person.getInitials());
        binding.detailAvatar.setPhotoPath(person.getPhotoPath());

        // Facts
        if (!TextUtils.isEmpty(person.getOccupation())) {
            binding.tvFactOccupation.setText("Occupation: " + person.getOccupation());
            binding.tvFactOccupation.setVisibility(View.VISIBLE);
        } else {
            binding.tvFactOccupation.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(person.getHometown())) {
            binding.tvFactHometown.setText("Hometown: " + person.getHometown());
            binding.tvFactHometown.setVisibility(View.VISIBLE);
            binding.btnMaps.setVisibility(View.VISIBLE);
        } else {
            binding.tvFactHometown.setVisibility(View.GONE);
            binding.btnMaps.setVisibility(View.GONE);
        }

        // Phone call visibility
        binding.btnCall.setVisibility(!TextUtils.isEmpty(person.getPhoneNumber()) ? View.VISIBLE : View.GONE);

        // Biography
        if (!TextUtils.isEmpty(person.getBiography())) {
            binding.tvDetailBiography.setText(person.getBiography());
            binding.cardBiography.setVisibility(View.VISIBLE);
        } else {
            binding.cardBiography.setVisibility(View.GONE);
        }
    }

    private void observeRelatives(PersonEntity person) {
        // Observe all people to resolve parent, spouse, children, siblings
        personViewModel.getPeople().observe(this, allPeople -> {
            if (allPeople == null) return;

            binding.chipGroupParents.removeAllViews();
            binding.chipGroupSpouse.removeAllViews();
            binding.chipGroupChildren.removeAllViews();
            binding.chipGroupSiblings.removeAllViews();

            boolean hasParents = false;
            boolean hasSpouse = false;

            for (PersonEntity p : allPeople) {
                // Father & Mother
                if (p.getId().equals(person.getFatherId()) || p.getId().equals(person.getMotherId())) {
                    hasParents = true;
                    addRelativeChip(binding.chipGroupParents, p, p.getId().equals(person.getFatherId()) ? "Father: " : "Mother: ");
                }

                // Spouse
                if (p.getId().equals(person.getSpouseId()) || (p.getSpouseId() != null && p.getSpouseId().equals(person.getId()))) {
                    hasSpouse = true;
                    addRelativeChip(binding.chipGroupSpouse, p, "Spouse: ");
                }
            }

            binding.tvParentsLabel.setVisibility(hasParents ? View.VISIBLE : View.GONE);
            binding.chipGroupParents.setVisibility(hasParents ? View.VISIBLE : View.GONE);

            binding.tvSpouseLabel.setVisibility(hasSpouse ? View.VISIBLE : View.GONE);
            binding.chipGroupSpouse.setVisibility(hasSpouse ? View.VISIBLE : View.GONE);
        });

        // Observe Children
        personViewModel.getChildren(person.getId()).observe(this, children -> {
            binding.chipGroupChildren.removeAllViews();
            if (children != null && !children.isEmpty()) {
                binding.tvChildrenLabel.setVisibility(View.VISIBLE);
                binding.chipGroupChildren.setVisibility(View.VISIBLE);
                for (PersonEntity child : children) {
                    addRelativeChip(binding.chipGroupChildren, child, "");
                }
            } else {
                binding.tvChildrenLabel.setVisibility(View.GONE);
                binding.chipGroupChildren.setVisibility(View.GONE);
            }
        });

        // Observe Siblings
        personViewModel.getSiblings(person.getId(), person.getFatherId(), person.getMotherId()).observe(this, siblings -> {
            binding.chipGroupSiblings.removeAllViews();
            if (siblings != null && !siblings.isEmpty()) {
                binding.tvSiblingsLabel.setVisibility(View.VISIBLE);
                binding.chipGroupSiblings.setVisibility(View.VISIBLE);
                for (PersonEntity sibling : siblings) {
                    addRelativeChip(binding.chipGroupSiblings, sibling, "");
                }
            } else {
                binding.tvSiblingsLabel.setVisibility(View.GONE);
                binding.chipGroupSiblings.setVisibility(View.GONE);
            }
        });
    }

    private void addRelativeChip(com.google.android.material.chip.ChipGroup group, PersonEntity relative, String prefix) {
        Chip chip = new Chip(this);
        chip.setText(prefix + relative.getFullName());
        chip.setClickable(true);
        chip.setCheckable(false);
        chip.setOnClickListener(v -> {
            Intent intent = new Intent(PersonDetailActivity.this, PersonDetailActivity.class);
            intent.putExtra(EXTRA_PERSON_ID, relative.getId());
            startActivity(intent);
        });
        group.addView(chip);
    }
}
