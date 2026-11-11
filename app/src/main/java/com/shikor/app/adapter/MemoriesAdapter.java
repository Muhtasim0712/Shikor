package com.shikor.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.shikor.app.R;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.databinding.ItemMemoryCardBinding;
import com.shikor.app.databinding.ItemMemoryYearHeaderBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * MemoriesAdapter - ListAdapter rendering family memories grouped by year headers (Phase 4).
 * CONCEPT: ListAdapter with DiffUtil, multi-view types, and ViewHolder binding.
 */
public class MemoriesAdapter extends ListAdapter<MemoriesAdapter.MemoryItem, RecyclerView.ViewHolder> {

    public static final int TYPE_YEAR_HEADER = 0;
    public static final int TYPE_MEMORY_CARD = 1;

    public static class MemoryItem {
        private final int type;
        private final int year;
        private final int count;
        private final MemoryEntity memory;

        public static MemoryItem header(int year, int count) {
            return new MemoryItem(TYPE_YEAR_HEADER, year, count, null);
        }

        public static MemoryItem card(MemoryEntity memory) {
            return new MemoryItem(TYPE_MEMORY_CARD, 0, 0, memory);
        }

        private MemoryItem(int type, int year, int count, MemoryEntity memory) {
            this.type = type;
            this.year = year;
            this.count = count;
            this.memory = memory;
        }

        public int getType() { return type; }
        public int getYear() { return year; }
        public int getCount() { return count; }
        public MemoryEntity getMemory() { return memory; }
    }

    public interface OnMemoryClickListener {
        void onMemoryClick(MemoryEntity memory);
        void onRetryClick(MemoryEntity memory);
    }

    private final OnMemoryClickListener listener;
    private final Map<String, String> personNameMap = new HashMap<>();

    public MemoriesAdapter(OnMemoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setPersonNames(Map<String, String> nameMap) {
        this.personNameMap.clear();
        if (nameMap != null) {
            this.personNameMap.putAll(nameMap);
        }
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<MemoryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<MemoryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull MemoryItem oldItem, @NonNull MemoryItem newItem) {
            if (oldItem.getType() != newItem.getType()) return false;
            if (oldItem.getType() == TYPE_YEAR_HEADER) {
                return oldItem.getYear() == newItem.getYear();
            } else {
                return oldItem.getMemory() != null && newItem.getMemory() != null &&
                        Objects.equals(oldItem.getMemory().getId(), newItem.getMemory().getId());
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull MemoryItem oldItem, @NonNull MemoryItem newItem) {
            if (oldItem.getType() == TYPE_YEAR_HEADER) {
                return oldItem.getYear() == newItem.getYear() && oldItem.getCount() == newItem.getCount();
            } else {
                MemoryEntity m1 = oldItem.getMemory();
                MemoryEntity m2 = newItem.getMemory();
                if (m1 == null || m2 == null) return false;
                return Objects.equals(m1.getTitle(), m2.getTitle())
                        && Objects.equals(m1.getDescription(), m2.getDescription())
                        && m1.getSyncState() == m2.getSyncState()
                        && m1.getUpdatedAt() == m2.getUpdatedAt();
            }
        }
    };

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_YEAR_HEADER) {
            ItemMemoryYearHeaderBinding binding = ItemMemoryYearHeaderBinding.inflate(inflater, parent, false);
            return new YearHeaderViewHolder(binding);
        } else {
            ItemMemoryCardBinding binding = ItemMemoryCardBinding.inflate(inflater, parent, false);
            return new MemoryCardViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MemoryItem item = getItem(position);
        if (holder instanceof YearHeaderViewHolder) {
            ((YearHeaderViewHolder) holder).bind(item);
        } else if (holder instanceof MemoryCardViewHolder) {
            ((MemoryCardViewHolder) holder).bind(item.getMemory());
        }
    }

    static class YearHeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemoryYearHeaderBinding binding;

        YearHeaderViewHolder(ItemMemoryYearHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MemoryItem item) {
            binding.tvMemoryYear.setText(String.valueOf(item.getYear()));
            int count = item.getCount();
            binding.tvMemoryYear.append(" (" + count + (count == 1 ? " Memory" : " Memories") + ")");
        }
    }

    class MemoryCardViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemoryCardBinding binding;

        MemoryCardViewHolder(ItemMemoryCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    MemoryItem item = getItem(pos);
                    if (item.getMemory() != null) {
                        listener.onMemoryClick(item.getMemory());
                    }
                }
            });

            binding.btnRetryUpload.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    MemoryItem item = getItem(pos);
                    if (item.getMemory() != null) {
                        listener.onRetryClick(item.getMemory());
                    }
                }
            });
        }

        void bind(MemoryEntity memory) {
            if (memory == null) return;
            binding.tvMemoryTitle.setText(memory.getTitle() != null ? memory.getTitle() : "");

            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            binding.tvMemoryDate.setText(sdf.format(new Date(memory.getEventDate() > 0 ? memory.getEventDate() : memory.getCreatedAt())));

            if (memory.getDescription() != null && !memory.getDescription().trim().isEmpty()) {
                binding.tvMemoryExcerpt.setText(memory.getDescription());
                binding.tvMemoryExcerpt.setVisibility(View.VISIBLE);
            } else {
                binding.tvMemoryExcerpt.setVisibility(View.GONE);
            }

            // Uploader line ("Added by <name>") with avatar initial
            String uploaderName = memory.getUploaderName() != null && !memory.getUploaderName().trim().isEmpty()
                    ? memory.getUploaderName()
                    : "Family Member";
            binding.tvUploaderName.setText("Added by " + uploaderName);
            String initial = !uploaderName.isEmpty() ? String.valueOf(uploaderName.charAt(0)).toUpperCase() : "F";
            binding.tvUploaderInitial.setText(initial);

            // Audio indicator
            if (memory.isHasAudio()) {
                binding.layoutAudioIndicator.setVisibility(View.VISIBLE);
                long sec = memory.getAudioDurationMs() / 1000;
                binding.tvAudioDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60));
            } else {
                binding.layoutAudioIndicator.setVisibility(View.GONE);
            }

            // Sync Badge & Retry
            if (memory.getSyncState() == MemoryEntity.STATE_LOCAL_PENDING || memory.getSyncState() == MemoryEntity.STATE_UPLOADING) {
                binding.tvSyncBadge.setText("PENDING");
                binding.tvSyncBadge.setBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.tertiary));
                binding.tvSyncBadge.setVisibility(View.VISIBLE);
                binding.btnRetryUpload.setVisibility(View.GONE);
            } else if (memory.getSyncState() == MemoryEntity.STATE_FAILED) {
                binding.tvSyncBadge.setText("FAILED");
                binding.tvSyncBadge.setBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.error));
                binding.tvSyncBadge.setVisibility(View.VISIBLE);
                binding.btnRetryUpload.setVisibility(View.VISIBLE);
            } else {
                binding.tvSyncBadge.setVisibility(View.GONE);
                binding.btnRetryUpload.setVisibility(View.GONE);
            }

            // Tagged People Chips
            binding.chipGroupMemoryTags.removeAllViews();
            List<String> tagged = memory.getTaggedPersonIds();
            if (tagged != null && !tagged.isEmpty()) {
                boolean anyAdded = false;
                for (String pId : tagged) {
                    String name = personNameMap.get(pId);
                    if (name != null && !name.isEmpty()) {
                        Chip chip = new Chip(binding.getRoot().getContext());
                        chip.setText(name);
                        chip.setCheckable(false);
                        chip.setClickable(false);
                        chip.setChipIconResource(android.R.drawable.ic_menu_myplaces);
                        chip.setEnsureMinTouchTargetSize(false);
                        binding.chipGroupMemoryTags.addView(chip);
                        anyAdded = true;
                    }
                }
                binding.chipGroupMemoryTags.setVisibility(anyAdded ? View.VISIBLE : View.GONE);
            } else {
                binding.chipGroupMemoryTags.setVisibility(View.GONE);
            }

            // Photo thumbnail & badge
            File photoDir = new File(binding.getRoot().getContext().getFilesDir(), "memories/" + memory.getId());
            File firstPhoto = new File(photoDir, "photo_0.jpg");
            if (memory.getPhotoCount() > 0 && firstPhoto.exists()) {
                binding.layoutPhotoContainer.setVisibility(View.VISIBLE);
                Glide.with(binding.ivMemoryThumb.getContext())
                        .load(firstPhoto)
                        .placeholder(R.color.surface_variant)
                        .error(R.color.surface_variant)
                        .centerCrop()
                        .into(binding.ivMemoryThumb);

                if (memory.getPhotoCount() > 1) {
                    binding.tvPhotoCountBadge.setText("1 / " + memory.getPhotoCount());
                    binding.tvPhotoCountBadge.setVisibility(View.VISIBLE);
                } else {
                    binding.tvPhotoCountBadge.setVisibility(View.GONE);
                }
            } else {
                binding.layoutPhotoContainer.setVisibility(View.GONE);
            }
        }
    }
}
