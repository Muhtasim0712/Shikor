package com.shikor.app.ui.tools;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shikor.app.databinding.ActivityExportBinding;
import com.shikor.app.databinding.ItemExportContactBinding;
import com.shikor.app.provider.FamilyProvider;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.R;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.FamilyImporter;
import android.graphics.Bitmap;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.util.Prefs;
import com.shikor.app.util.TreeExporter;
import com.shikor.app.view.FamilyTreeView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ExportActivity - Screen for previewing and exporting family directory contacts (vCard .vcf & CSV).
 *
 * CONCEPT: Consumes FamilyProvider (ContentProvider) via ContentResolver and uses FileProvider for safe sharing.
 */
public class ExportActivity extends BaseActivity {

    private ActivityExportBinding binding;
    private final List<ContactItem> contactList = new ArrayList<>();
    private ContactAdapter adapter;

    private final ActivityResultLauncher<String[]> importFilePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    importContactsFromUri(uri);
                }
            });

    public static class ContactItem {
        final String id;
        final String fullName;
        final String phoneNumber;
        final String gender;
        final boolean isDeceased;
        final String photoPath;
        final String birthDate;
        final String hometown;

        public ContactItem(String id, String fullName, String phoneNumber, String gender,
                           boolean isDeceased, String photoPath, String birthDate, String hometown) {
            this.id = id;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.gender = gender;
            this.isDeceased = isDeceased;
            this.photoPath = photoPath;
            this.birthDate = birthDate;
            this.hometown = hometown;
        }

        public String getInitials() {
            if (fullName == null || fullName.trim().isEmpty()) return "?";
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        setupClickListeners();
        loadContactsFromContentProvider();
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter(contactList, contact -> {
            if (contact.phoneNumber != null && !contact.phoneNumber.trim().isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.phoneNumber.trim()));
                startActivity(dialIntent);
            } else {
                Toast.makeText(this, "No phone number recorded for " + contact.fullName, Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvExportContacts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExportContacts.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnExportPdf.setOnClickListener(v -> exportPdfArchive());
        binding.btnExportPng.setOnClickListener(v -> exportPngArchive());
        binding.btnExportVcf.setOnClickListener(v -> exportVCard());
        binding.btnExportCsv.setOnClickListener(v -> exportCsv());
        binding.btnImportContacts.setOnClickListener(v -> {
            importFilePickerLauncher.launch(new String[]{
                    "text/*",
                    "text/csv",
                    "text/comma-separated-values",
                    "text/vcard",
                    "text/x-vcard",
                    "*/*"
            });
        });
    }

    private void exportPdfArchive() {
        String familyId = Prefs.getInstance(this).getFamilyId();
        String familyName = Prefs.getInstance(this).getFamilyName();

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<PersonEntity> people = AppDatabase.getInstance(this).personDao().getAllPeopleSync(familyId);
            if (people == null || people.isEmpty()) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        Toast.makeText(this, "No family members found to export.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            AppExecutors.getInstance().mainThread().execute(() -> {
                FamilyTreeView tempTreeView = new FamilyTreeView(this);
                tempTreeView.setPeople(people);
                tempTreeView.postDelayed(() -> {
                    Bitmap bmp = tempTreeView.exportFullTreeBitmap();
                    TreeExporter.exportTreeAsPdf(this, bmp, familyName, people);
                }, 350);
            });
        });
    }

    private void exportPngArchive() {
        String familyId = Prefs.getInstance(this).getFamilyId();
        String familyName = Prefs.getInstance(this).getFamilyName();

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<PersonEntity> people = AppDatabase.getInstance(this).personDao().getAllPeopleSync(familyId);
            if (people == null || people.isEmpty()) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        Toast.makeText(this, "No family members found to export.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            AppExecutors.getInstance().mainThread().execute(() -> {
                FamilyTreeView tempTreeView = new FamilyTreeView(this);
                tempTreeView.setPeople(people);
                tempTreeView.postDelayed(() -> {
                    Bitmap bmp = tempTreeView.exportFullTreeBitmap();
                    TreeExporter.exportTreeAsPng(this, bmp, familyName);
                }, 350);
            });
        });
    }

    private void importContactsFromUri(Uri uri) {
        Toast.makeText(this, "Importing contacts from file...", Toast.LENGTH_SHORT).show();

        AppExecutors.getInstance().diskIO().execute(() -> {
            FamilyImporter.ImportResult result = FamilyImporter.importFromUri(this, uri);

            AppExecutors.getInstance().mainThread().execute(() -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(result.success ? "Import Complete" : "Import Failed")
                        .setMessage(result.message)
                        .setPositiveButton("OK", null)
                        .setIcon(result.success ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                        .show();

                if (result.success) {
                    loadContactsFromContentProvider();
                }
            });
        });
    }

    /**
     * CONCEPT: Querying FamilyProvider via android.content.ContentResolver
     */
    private void loadContactsFromContentProvider() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<ContactItem> items = new ArrayList<>();

            try (Cursor cursor = getContentResolver().query(FamilyProvider.CONTENT_URI, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int nameIdx = cursor.getColumnIndex("fullName");
                    int phoneIdx = cursor.getColumnIndex("phoneNumber");
                    int genderIdx = cursor.getColumnIndex("gender");
                    int deceasedIdx = cursor.getColumnIndex("isDeceased");
                    int photoIdx = cursor.getColumnIndex("photoPath");
                    int birthIdx = cursor.getColumnIndex("birthDate");
                    int townIdx = cursor.getColumnIndex("hometown");

                    do {
                        String id = idIdx >= 0 ? cursor.getString(idIdx) : "";
                        String fullName = nameIdx >= 0 ? cursor.getString(nameIdx) : "";
                        String phone = phoneIdx >= 0 ? cursor.getString(phoneIdx) : "";
                        String gender = genderIdx >= 0 ? cursor.getString(genderIdx) : "other";
                        boolean isDeceased = deceasedIdx >= 0 && cursor.getInt(deceasedIdx) == 1;
                        String photo = photoIdx >= 0 ? cursor.getString(photoIdx) : null;
                        String birth = birthIdx >= 0 ? cursor.getString(birthIdx) : null;
                        String town = townIdx >= 0 ? cursor.getString(townIdx) : null;

                        items.add(new ContactItem(id, fullName, phone, gender, isDeceased, photo, birth, town));
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                android.util.Log.e("ExportActivity", "Error loading from FamilyProvider: " + e.getMessage(), e);
            }

            AppExecutors.getInstance().mainThread().execute(() -> {
                contactList.clear();
                contactList.addAll(items);
                adapter.notifyDataSetChanged();

                int totalCount = contactList.size();
                int withPhoneCount = 0;
                for (ContactItem item : contactList) {
                    if (item.phoneNumber != null && !item.phoneNumber.trim().isEmpty()) {
                        withPhoneCount++;
                    }
                }

                binding.tvContactCountBadge.setText(totalCount + " Relatives (" + withPhoneCount + " with phone numbers)");
                binding.tvEmptyContacts.setText(R.string.empty_contacts_title);
                binding.tvEmptyContacts.setVisibility(totalCount == 0 ? View.VISIBLE : View.GONE);
            });
        });
    }

    /**
     * CONCEPT: Formats standard vCard 3.0 file and shares via Intent.ACTION_SEND using FileProvider
     */
    private void exportVCard() {
        if (contactList.isEmpty()) {
            Toast.makeText(this, "No contacts available to export", Toast.LENGTH_SHORT).show();
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                File cacheDir = new File(getCacheDir(), "exports");
                if (!cacheDir.exists()) cacheDir.mkdirs();

                File vcfFile = new File(cacheDir, "shikor_family_contacts.vcf");
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(vcfFile), StandardCharsets.UTF_8)) {
                    for (ContactItem c : contactList) {
                        writer.write("BEGIN:VCARD\r\n");
                        writer.write("VERSION:3.0\r\n");
                        writer.write("FN:" + (c.fullName != null ? c.fullName : "Relative") + "\r\n");
                        if (c.phoneNumber != null && !c.phoneNumber.trim().isEmpty()) {
                            writer.write("TEL;TYPE=CELL:" + c.phoneNumber.trim() + "\r\n");
                        }
                        if (c.birthDate != null && !c.birthDate.trim().isEmpty()) {
                            writer.write("BDAY:" + c.birthDate.trim() + "\r\n");
                        }
                        String note = "Family Member (Shikor Family Archive)";
                        if (c.hometown != null && !c.hometown.isEmpty()) {
                            note += " - Hometown: " + c.hometown;
                        }
                        writer.write("NOTE:" + note + "\r\n");
                        writer.write("END:VCARD\r\n");
                    }
                }

                AppExecutors.getInstance().mainThread().execute(() -> shareExportFile(vcfFile, "text/x-vcard", "Share Family Contacts (vCard)"));
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        Toast.makeText(this, "Failed to generate vCard: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * CONCEPT: Formats CSV directory and shares via Intent.ACTION_SEND
     */
    private void exportCsv() {
        if (contactList.isEmpty()) {
            Toast.makeText(this, "No contacts available to export", Toast.LENGTH_SHORT).show();
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                File cacheDir = new File(getCacheDir(), "exports");
                if (!cacheDir.exists()) cacheDir.mkdirs();

                File csvFile = new File(cacheDir, "shikor_family_directory.csv");
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8)) {
                    writer.write("Full Name,Phone Number,Birth Date,Hometown,Status\r\n");
                    for (ContactItem c : contactList) {
                        String name = c.fullName != null ? escapeCsv(c.fullName) : "";
                        String phone = c.phoneNumber != null ? escapeCsv(c.phoneNumber) : "";
                        String birth = c.birthDate != null ? escapeCsv(c.birthDate) : "";
                        String town = c.hometown != null ? escapeCsv(c.hometown) : "";
                        String status = c.isDeceased ? "Deceased" : "Living";

                        writer.write(name + "," + phone + "," + birth + "," + town + "," + status + "\r\n");
                    }
                }

                AppExecutors.getInstance().mainThread().execute(() -> shareExportFile(csvFile, "text/csv", "Share Family Directory (CSV)"));
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        Toast.makeText(this, "Failed to generate CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void shareExportFile(File file, String mimeType, String chooserTitle) {
        Uri contentUri = FileProvider.getUriForFile(this, "com.shikor.app.fileprovider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shikor Family Contacts Export");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, chooserTitle));
    }

    // --- RECYCLERVIEW ADAPTER ---

    private static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
        interface OnCallClickListener {
            void onCall(ContactItem contact);
        }

        private final List<ContactItem> list;
        private final OnCallClickListener listener;

        ContactAdapter(List<ContactItem> list, OnCallClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemExportContactBinding binding = ItemExportContactBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ContactItem item = list.get(position);
            holder.binding.tvContactName.setText(item.fullName);

            if (item.phoneNumber != null && !item.phoneNumber.trim().isEmpty()) {
                holder.binding.tvContactPhone.setText(item.phoneNumber);
                holder.binding.tvContactPhone.setVisibility(View.VISIBLE);
                holder.binding.btnCallContact.setVisibility(View.VISIBLE);
                holder.binding.btnCallContact.setOnClickListener(v -> listener.onCall(item));
            } else {
                holder.binding.tvContactPhone.setVisibility(View.GONE);
                holder.binding.btnCallContact.setVisibility(View.GONE);
            }

            StringBuilder details = new StringBuilder();
            if (item.hometown != null && !item.hometown.trim().isEmpty()) {
                details.append("Hometown: ").append(item.hometown.trim());
            }
            if (item.birthDate != null && !item.birthDate.trim().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("Born: ").append(item.birthDate.trim());
            }
            if (item.isDeceased) {
                if (details.length() > 0) details.append(" • ");
                details.append("Late (In Loving Memory)");
            }
            holder.binding.tvContactDetails.setText(details.toString());

            // Avatar configuration
            holder.binding.avatarContact.setGender(item.gender);
            holder.binding.avatarContact.setDeceased(item.isDeceased);
            holder.binding.avatarContact.setInitials(item.getInitials());
            holder.binding.avatarContact.setPhotoPath(item.photoPath);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemExportContactBinding binding;
            ViewHolder(ItemExportContactBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
