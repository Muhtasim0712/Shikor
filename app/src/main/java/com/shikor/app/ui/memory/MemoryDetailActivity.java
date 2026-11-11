package com.shikor.app.ui.memory;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shikor.app.R;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.MemoryPhotoEntity;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.ActivityMemoryDetailBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.ui.person.PersonViewModel;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;
import com.shikor.app.util.TtsHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MemoryDetailActivity - Displays full details of a family memory including photos, audio, and tagged relatives (Phase 4).
 * CONCEPT: ViewPager2 for photos, MediaPlayer with SeekBar, TTS Read Aloud, share intent, edit/delete permissions.
 */
public class MemoryDetailActivity extends BaseActivity {

    public static final String EXTRA_MEMORY_ID = "extra_memory_id";

    private ActivityMemoryDetailBinding binding;
    private MemoryViewModel memoryViewModel;
    private PersonViewModel personViewModel;

    private String memoryId;
    private MemoryEntity currentMemory;
    private List<PersonEntity> allPersons = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private final Handler seekHandler = new Handler(Looper.getMainLooper());
    private Runnable seekRunnable;
    private TtsHelper ttsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        setSupportActionBar(binding.toolbarMemoryDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarMemoryDetail.setNavigationOnClickListener(v -> finish());

        memoryId = getIntent().getStringExtra(EXTRA_MEMORY_ID);
        if (memoryId == null || memoryId.isEmpty()) {
            finish();
            return;
        }

        memoryViewModel = new ViewModelProvider(this).get(MemoryViewModel.class);
        personViewModel = new ViewModelProvider(this).get(PersonViewModel.class);
        ttsHelper = new TtsHelper(this);

        setupControls();
        observeData();
    }

    private void setupControls() {
        binding.btnReadAloudMemory.setOnClickListener(v -> {
            if (currentMemory != null && !TextUtils.isEmpty(currentMemory.getDescription())) {
                ttsHelper.speak(currentMemory.getDescription());
            } else if (currentMemory != null) {
                ttsHelper.speak(currentMemory.getTitle());
            }
        });

        binding.btnShareMemory.setOnClickListener(v -> {
            if (currentMemory == null) return;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = currentMemory.getTitle() + "\n\n" + currentMemory.getDescription() + "\n\nShared via Shikor";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share Memory"));
        });
    }

    private void observeData() {
        personViewModel.getPeople().observe(this, people -> {
            if (people != null) {
                allPersons = people;
                if (currentMemory != null) {
                    renderTaggedPeople(currentMemory);
                }
            }
        });

        memoryViewModel.getMemoryById(memoryId).observe(this, memory -> {
            if (memory != null) {
                currentMemory = memory;
                bindMemoryData(memory);
            }
        });

        memoryViewModel.getPhotosForMemory(memoryId).observe(this, photos -> {
            if (photos != null) {
                bindPhotosData(photos);
            }
        });
    }

    private void bindMemoryData(MemoryEntity memory) {
        binding.toolbarMemoryDetail.setTitle(memory.getTitle());
        binding.tvDetailTitle.setText(memory.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(memory.getEventDate() > 0 ? memory.getEventDate() : memory.getCreatedAt()));
        String uploader = memory.getUploaderName() != null && !memory.getUploaderName().isEmpty() ? memory.getUploaderName() : "Family Member";
        binding.tvDetailMeta.setText(dateStr + " • Added by " + uploader);

        if (memory.getDescription() != null && !memory.getDescription().trim().isEmpty()) {
            binding.tvDetailDescription.setText(memory.getDescription());
            binding.cardDescription.setVisibility(View.VISIBLE);
        } else {
            binding.cardDescription.setVisibility(View.GONE);
        }

        if (memory.isHasAudio() && memory.getAudioPath() != null && new File(memory.getAudioPath()).exists()) {
            binding.cardAudioPlayer.setVisibility(View.VISIBLE);
            setupAudioPlayer(memory.getAudioPath());
        } else {
            binding.cardAudioPlayer.setVisibility(View.GONE);
        }

        renderTaggedPeople(memory);
        invalidateOptionsMenu();
    }

    private void bindPhotosData(List<MemoryPhotoEntity> photos) {
        if (photos != null && !photos.isEmpty()) {
            List<String> validPaths = new ArrayList<>();
            for (MemoryPhotoEntity p : photos) {
                if (p.getLocalPath() != null && new File(p.getLocalPath()).exists()) {
                    validPaths.add(p.getLocalPath());
                }
            }
            if (!validPaths.isEmpty()) {
                binding.viewPagerPhotos.setVisibility(View.VISIBLE);
                PhotoPagerAdapter adapter = new PhotoPagerAdapter(validPaths);
                binding.viewPagerPhotos.setAdapter(adapter);
            } else {
                binding.viewPagerPhotos.setVisibility(View.GONE);
            }
        } else {
            binding.viewPagerPhotos.setVisibility(View.GONE);
        }
    }

    private void renderTaggedPeople(MemoryEntity memory) {
        binding.chipGroupDetailTags.removeAllViews();
        List<String> tagged = memory.getTaggedPersonIds();
        if (tagged != null && !tagged.isEmpty()) {
            for (String pId : tagged) {
                PersonEntity found = null;
                for (PersonEntity p : allPersons) {
                    if (p.getId().equals(pId)) {
                        found = p;
                        break;
                    }
                }
                final String personId = pId;
                String name = found != null ? found.getFullName() : "Relative";
                Chip chip = new Chip(this);
                chip.setText(name);
                chip.setCheckable(false);
                chip.setClickable(true);
                chip.setChipIconResource(android.R.drawable.ic_menu_myplaces);
                chip.setOnClickListener(v -> {
                    Intent intent = new Intent(MemoryDetailActivity.this, PersonDetailActivity.class);
                    intent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, personId);
                    startActivity(intent);
                });
                binding.chipGroupDetailTags.addView(chip);
            }
            binding.chipGroupDetailTags.setVisibility(View.VISIBLE);
        } else {
            binding.chipGroupDetailTags.setVisibility(View.GONE);
        }
    }

    private void setupAudioPlayer(String audioPath) {
        binding.btnPlayPauseAudio.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(audioPath);
                    mediaPlayer.prepare();
                    binding.seekBarAudio.setMax(mediaPlayer.getDuration());
                    long totalSec = mediaPlayer.getDuration() / 1000;
                    binding.tvAudioTotalDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", totalSec / 60, totalSec % 60));

                    mediaPlayer.setOnCompletionListener(mp -> {
                        isPlaying = false;
                        binding.btnPlayPauseAudio.setIconResource(android.R.drawable.ic_media_play);
                        binding.seekBarAudio.setProgress(0);
                    });
                } catch (Exception ignored) {}
            }

            if (isPlaying) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                isPlaying = false;
                binding.btnPlayPauseAudio.setIconResource(android.R.drawable.ic_media_play);
                if (seekRunnable != null) seekHandler.removeCallbacks(seekRunnable);
            } else {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
                isPlaying = true;
                binding.btnPlayPauseAudio.setIconResource(android.R.drawable.ic_media_pause);
                startSeekBarUpdates();
            }
        });

        binding.seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startSeekBarUpdates() {
        seekRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    binding.seekBarAudio.setProgress(currentPosition);
                    long curSec = currentPosition / 1000;
                    binding.tvAudioCurrentTime.setText(String.format(Locale.getDefault(), "%02d:%02d", curSec / 60, curSec % 60));
                    seekHandler.postDelayed(this, 500);
                }
            }
        };
        seekHandler.post(seekRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (currentMemory != null && user != null) {
            if (user.getUid().equals(currentMemory.getUploaderUid())) {
                menu.add(0, 101, 0, "Delete Memory").setIcon(android.R.drawable.ic_menu_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 101) {
            confirmDeleteMemory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteMemory() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Memory")
                .setMessage("Are you sure you want to delete this memory? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> AppExecutors.getInstance().diskIO().execute(() -> {
                    if (currentMemory.getAudioPath() != null) {
                        File af = new File(currentMemory.getAudioPath());
                        if (af.exists()) af.delete();
                    }
                    File photoDir = new File(getFilesDir(), "memories/" + memoryId);
                    if (photoDir.exists() && photoDir.isDirectory()) {
                        File[] files = photoDir.listFiles();
                        if (files != null) {
                            for (File f : files) f.delete();
                        }
                        photoDir.delete();
                    }
                    com.shikor.app.data.local.AppDatabase db = com.shikor.app.data.local.AppDatabase.getInstance(this);
                    db.memoryPhotoDao().deletePhotosForMemory(memoryId);
                    db.memoryDao().deleteById(memoryId);

                    String fId = currentMemory.getFamilyId();
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("families")
                            .child(fId).child("memories").child(memoryId).removeValue();
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("families")
                            .child(fId).child("memoryMedia").child(memoryId).removeValue();

                    AppExecutors.getInstance().mainThread().execute(() -> {
                        Toast.makeText(this, "Memory deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder> {
        private final List<String> paths;
        public PhotoPagerAdapter(List<String> paths) { this.paths = paths; }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new PhotoViewHolder(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(new File(paths.get(position))).into(holder.imageView);
        }

        @Override
        public int getItemCount() { return paths.size(); }

        static class PhotoViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;
            PhotoViewHolder(ImageView iv) { super(iv); imageView = iv; }
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            try { mediaPlayer.release(); } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        seekHandler.removeCallbacksAndMessages(null);
        ttsHelper.shutdown();
        super.onDestroy();
    }
}
