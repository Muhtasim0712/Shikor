package com.shikor.app.ui.person;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.ActivityAddEditPersonBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.util.Prefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * AddEditPersonActivity - Create or update a family member.
 *
 * CONCEPT: ActivityResultLauncher for Implicit Intents (Photo Picker & Camera),
 * DatePickerDialog callbacks, OnCheckedChangeListener, and validation.
 */
public class AddEditPersonActivity extends BaseActivity {

    public static final String EXTRA_PERSON_ID = "extra_person_id";

    private ActivityAddEditPersonBinding binding;
    private PersonViewModel personViewModel;

    private String personId;
    private boolean isEditMode = false;
    private String selectedPhotoPath;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private List<PersonEntity> existingMembers = new ArrayList<>();
    private List<PersonEntity> fatherCandidates = new ArrayList<>();
    private List<PersonEntity> motherCandidates = new ArrayList<>();
    private List<PersonEntity> spouseCandidates = new ArrayList<>();
    private List<PersonEntity> childCandidates = new ArrayList<>();

    // CONCEPT: ActivityResultLauncher with implicit GetContent intent for photo picker
    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveImageFromUri(uri);
                }
            });

    // CONCEPT: ActivityResultLauncher with implicit TakePicturePreview intent for camera
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    saveImageFromBitmap(bitmap);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditPersonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        personViewModel = new ViewModelProvider(this).get(PersonViewModel.class);

        personId = getIntent().getStringExtra(EXTRA_PERSON_ID);
        isEditMode = (personId != null && !personId.isEmpty());

        binding.toolbar.setTitle(isEditMode ? "Edit Relative" : "Add Relative");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupListeners();
        observeExistingMembers();
    }

    private void setupListeners() {
        // Photo actions
        binding.btnPickPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        binding.btnTakePhoto.setOnClickListener(v -> cameraLauncher.launch(null));

        // CONCEPT: DatePickerDialog callbacks for birth & death dates
        binding.etBirthDate.setOnClickListener(v -> showDatePicker(true));
        binding.tilBirthDate.setEndIconOnClickListener(v -> showDatePicker(true));

        binding.etDeathDate.setOnClickListener(v -> showDatePicker(false));
        binding.tilDeathDate.setEndIconOnClickListener(v -> showDatePicker(false));

        // CONCEPT: OnCheckedChangeListener on Switch
        binding.switchDeceased.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.tilDeathDate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            binding.avatarPreview.setDeceased(isChecked);
        });

        // Gender changes
        binding.rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFemale) {
                binding.avatarPreview.setGender("female");
            } else if (checkedId == R.id.rbMale) {
                binding.avatarPreview.setGender("male");
            } else {
                binding.avatarPreview.setGender("other");
            }
        });

        binding.btnSavePerson.setOnClickListener(v -> savePerson());
    }

    private void showDatePicker(boolean isBirthDate) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String dateStr = dateFormat.format(calendar.getTime());
                    if (isBirthDate) {
                        binding.etBirthDate.setText(dateStr);
                        binding.tilBirthDate.setError(null);
                    } else {
                        binding.etDeathDate.setText(dateStr);
                        binding.tilDeathDate.setError(null);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void observeExistingMembers() {
        personViewModel.getPeople().observe(this, people -> {
            if (people != null) {
                existingMembers = people;
                populateSpinners();

                if (isEditMode) {
                    loadPersonForEditing();
                }
            }
        });
    }

    private void populateSpinners() {
        fatherCandidates.clear();
        motherCandidates.clear();
        spouseCandidates.clear();
        childCandidates.clear();

        List<String> fatherNames = new ArrayList<>();
        List<String> motherNames = new ArrayList<>();
        List<String> spouseNames = new ArrayList<>();
        List<String> childNames = new ArrayList<>();

        fatherNames.add("None");
        motherNames.add("None");
        spouseNames.add("None");
        childNames.add("None");

        for (PersonEntity p : existingMembers) {
            // Cannot be own parent, spouse, or child of oneself
            if (isEditMode && p.getId().equals(personId)) continue;

            if (!"female".equalsIgnoreCase(p.getGender())) {
                fatherCandidates.add(p);
                fatherNames.add(p.getFullName());
            }
            if (!"male".equalsIgnoreCase(p.getGender())) {
                motherCandidates.add(p);
                motherNames.add(p.getFullName());
            }
            spouseCandidates.add(p);
            spouseNames.add(p.getFullName());

            // Any existing member can be a child candidate
            childCandidates.add(p);
            childNames.add(p.getFullName());
        }

        ArrayAdapter<String> fatherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fatherNames);
        ArrayAdapter<String> motherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, motherNames);
        ArrayAdapter<String> spouseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spouseNames);
        ArrayAdapter<String> childAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, childNames);

        binding.spinnerFather.setAdapter(fatherAdapter);
        binding.spinnerMother.setAdapter(motherAdapter);
        binding.spinnerSpouse.setAdapter(spouseAdapter);
        binding.spinnerChild.setAdapter(childAdapter);
    }

    private void loadPersonForEditing() {
        for (PersonEntity p : existingMembers) {
            if (p.getId().equals(personId)) {
                binding.etFullName.setText(p.getFullName());
                binding.etNickName.setText(p.getNickName());
                binding.etBirthDate.setText(p.getBirthDate());
                binding.etDeathDate.setText(p.getDeathDate());
                binding.switchDeceased.setChecked(p.isDeceased());
                binding.tilDeathDate.setVisibility(p.isDeceased() ? View.VISIBLE : View.GONE);
                binding.etHometown.setText(p.getHometown());
                binding.etPhoneNumber.setText(p.getPhoneNumber());
                binding.etOccupation.setText(p.getOccupation());
                binding.etBiography.setText(p.getBiography());

                if ("female".equalsIgnoreCase(p.getGender())) {
                    binding.rbFemale.setChecked(true);
                } else if ("other".equalsIgnoreCase(p.getGender())) {
                    binding.rbOther.setChecked(true);
                } else {
                    binding.rbMale.setChecked(true);
                }

                selectedPhotoPath = p.getPhotoPath();
                binding.avatarPreview.setInitials(p.getInitials());
                binding.avatarPreview.setGender(p.getGender());
                binding.avatarPreview.setDeceased(p.isDeceased());
                if (selectedPhotoPath != null) {
                    binding.avatarPreview.setPhotoPath(selectedPhotoPath);
                }

                // Set spinners
                if (p.getFatherId() != null) {
                    for (int i = 0; i < fatherCandidates.size(); i++) {
                        if (fatherCandidates.get(i).getId().equals(p.getFatherId())) {
                            binding.spinnerFather.setSelection(i + 1);
                            break;
                        }
                    }
                }
                if (p.getMotherId() != null) {
                    for (int i = 0; i < motherCandidates.size(); i++) {
                        if (motherCandidates.get(i).getId().equals(p.getMotherId())) {
                            binding.spinnerMother.setSelection(i + 1);
                            break;
                        }
                    }
                }
                if (p.getSpouseId() != null) {
                    for (int i = 0; i < spouseCandidates.size(); i++) {
                        if (spouseCandidates.get(i).getId().equals(p.getSpouseId())) {
                            binding.spinnerSpouse.setSelection(i + 1);
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * CONCEPT: Form validation logic including chronological parent-child age checks
     */
    private void savePerson() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String nickName = binding.etNickName.getText() != null ? binding.etNickName.getText().toString().trim() : "";
        String birthDate = binding.etBirthDate.getText() != null ? binding.etBirthDate.getText().toString().trim() : "";
        String deathDate = binding.etDeathDate.getText() != null ? binding.etDeathDate.getText().toString().trim() : "";
        boolean isDeceased = binding.switchDeceased.isChecked();
        String hometown = binding.etHometown.getText() != null ? binding.etHometown.getText().toString().trim() : "";
        String phoneNumber = binding.etPhoneNumber.getText() != null ? binding.etPhoneNumber.getText().toString().trim() : "";
        String occupation = binding.etOccupation.getText() != null ? binding.etOccupation.getText().toString().trim() : "";
        String biography = binding.etBiography.getText() != null ? binding.etBiography.getText().toString().trim() : "";

        String gender = "male";
        if (binding.rbFemale.isChecked()) gender = "female";
        else if (binding.rbOther.isChecked()) gender = "other";

        // 1. Validate required fields
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError("Full name is required");
            binding.etFullName.requestFocus();
            return;
        }
        binding.tilFullName.setError(null);

        if (TextUtils.isEmpty(birthDate)) {
            binding.tilBirthDate.setError("Birth date is required");
            return;
        }
        binding.tilBirthDate.setError(null);

        // 2. Validate dates
        Date bDate = parseDate(birthDate);
        if (bDate == null) {
            binding.tilBirthDate.setError("Invalid birth date");
            return;
        }
        if (bDate.after(new Date())) {
            binding.tilBirthDate.setError("Birth date cannot be in the future");
            return;
        }

        if (isDeceased && !TextUtils.isEmpty(deathDate)) {
            Date dDate = parseDate(deathDate);
            if (dDate != null && dDate.before(bDate)) {
                binding.tilDeathDate.setError("Death date cannot be before birth date");
                return;
            }
        }

        // 3. Resolve parent & spouse selections
        String fatherId = null;
        int fatherPos = binding.spinnerFather.getSelectedItemPosition();
        if (fatherPos > 0 && fatherPos - 1 < fatherCandidates.size()) {
            PersonEntity father = fatherCandidates.get(fatherPos - 1);
            fatherId = father.getId();

            // Validate parent is not younger than child
            Date fBirth = parseDate(father.getBirthDate());
            if (fBirth != null && !fBirth.before(bDate)) {
                showSnackbar("Error: Father cannot be younger than the child!");
                return;
            }
        }

        String motherId = null;
        int motherPos = binding.spinnerMother.getSelectedItemPosition();
        if (motherPos > 0 && motherPos - 1 < motherCandidates.size()) {
            PersonEntity mother = motherCandidates.get(motherPos - 1);
            motherId = mother.getId();

            // Validate parent is not younger than child
            Date mBirth = parseDate(mother.getBirthDate());
            if (mBirth != null && !mBirth.before(bDate)) {
                showSnackbar("Error: Mother cannot be younger than the child!");
                return;
            }
        }

        // Child selection: this person is a parent of selectedChild
        int childPos = binding.spinnerChild.getSelectedItemPosition();
        PersonEntity selectedChild = null;
        if (childPos > 0 && childPos - 1 < childCandidates.size()) {
            selectedChild = childCandidates.get(childPos - 1);
            Date cBirth = parseDate(selectedChild.getBirthDate());
            if (cBirth != null && !bDate.before(cBirth)) {
                showSnackbar("Error: A parent cannot be younger than their child!");
                return;
            }
        }

        String spouseId = null;
        PersonEntity selectedSpouse = null;
        int spousePos = binding.spinnerSpouse.getSelectedItemPosition();
        if (spousePos > 0 && spousePos - 1 < spouseCandidates.size()) {
            selectedSpouse = spouseCandidates.get(spousePos - 1);
            spouseId = selectedSpouse.getId();
        }

        String familyId = Prefs.getInstance(this).getFamilyId();
        String currentUid = Prefs.getInstance(this).getUserId();

        if (!isEditMode) {
            personId = UUID.randomUUID().toString();
        }

        // Calculate generation level relative to parents, child, or spouse
        int generation = 0;
        if (fatherId != null) {
            for (PersonEntity p : existingMembers) {
                if (p.getId().equals(fatherId)) {
                    generation = p.getGeneration() + 1;
                    break;
                }
            }
        } else if (motherId != null) {
            for (PersonEntity p : existingMembers) {
                if (p.getId().equals(motherId)) {
                    generation = p.getGeneration() + 1;
                    break;
                }
            }
        } else if (selectedChild != null) {
            generation = selectedChild.getGeneration() - 1;
        } else if (selectedSpouse != null) {
            generation = selectedSpouse.getGeneration();
        }

        PersonEntity person = new PersonEntity(
                personId,
                familyId,
                fullName,
                nickName,
                gender,
                birthDate,
                isDeceased ? deathDate : null,
                isDeceased,
                hometown,
                occupation,
                phoneNumber,
                biography,
                selectedPhotoPath,
                fatherId,
                motherId,
                spouseId,
                null,
                currentUid,
                generation,
                System.currentTimeMillis(),
                PersonEntity.SYNC_STATE_PENDING
        );

        if (isEditMode) {
            personViewModel.updatePerson(person);
            Toast.makeText(this, "Relative updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            personViewModel.savePerson(person);
            Toast.makeText(this, "Relative added successfully", Toast.LENGTH_SHORT).show();
        }

        // Bidirectional relationship updates
        if (selectedChild != null) {
            if ("female".equalsIgnoreCase(gender)) {
                selectedChild.setMotherId(person.getId());
            } else {
                selectedChild.setFatherId(person.getId());
            }
            personViewModel.updatePerson(selectedChild);
        }

        if (selectedSpouse != null) {
            selectedSpouse.setSpouseId(person.getId());
            if (generation != 0 && selectedSpouse.getGeneration() == 0) {
                selectedSpouse.setGeneration(generation);
            } else if (generation == 0 && selectedSpouse.getGeneration() != 0) {
                person.setGeneration(selectedSpouse.getGeneration());
                personViewModel.updatePerson(person);
            }
            personViewModel.updatePerson(selectedSpouse);
        }

        finish();
    }

    private Date parseDate(String text) {
        try {
            return dateFormat.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    // Save image to internal app-specific files storage
    private void saveImageFromUri(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            File photosDir = new File(getFilesDir(), "photos");
            if (!photosDir.exists()) photosDir.mkdirs();

            File destFile = new File(photosDir, "person_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
            selectedPhotoPath = destFile.getAbsolutePath();
            binding.avatarPreview.setPhotoPath(selectedPhotoPath);
        } catch (Exception e) {
            showSnackbar("Failed to load photo: " + e.getMessage());
        }
    }

    private void saveImageFromBitmap(Bitmap bitmap) {
        try {
            File photosDir = new File(getFilesDir(), "photos");
            if (!photosDir.exists()) photosDir.mkdirs();

            File destFile = new File(photosDir, "person_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(destFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }
            selectedPhotoPath = destFile.getAbsolutePath();
            binding.avatarPreview.setPhotoPath(selectedPhotoPath);
        } catch (Exception e) {
            showSnackbar("Failed to save camera image: " + e.getMessage());
        }
    }

    // CONCEPT: Save instance state for form fields on configuration change
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photo_path", selectedPhotoPath);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedPhotoPath = savedInstanceState.getString("photo_path");
        if (selectedPhotoPath != null) {
            binding.avatarPreview.setPhotoPath(selectedPhotoPath);
        }
    }
}
