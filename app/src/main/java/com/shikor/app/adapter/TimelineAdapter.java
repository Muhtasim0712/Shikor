package com.shikor.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shikor.app.R;
import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.databinding.ItemTimelinePhotoBinding;
import com.shikor.app.databinding.ItemTimelineYearHeaderBinding;
import com.shikor.app.ui.timeline.TimelineItem;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TimelineAdapter - ListAdapter rendering chronological photo milestones grouped by Year Headers.
 * Supports sticky/sectioned year badges and photo cards with captions and tagged relative indicators.
 */
public class TimelineAdapter extends ListAdapter<TimelineItem, RecyclerView.ViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoEntity photo);
    }

    private final OnPhotoClickListener listener;
    private final Map<String, String> personNameMap = new HashMap<>();

    public TimelineAdapter(OnPhotoClickListener listener) {
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

    private static final DiffUtil.ItemCallback<TimelineItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TimelineItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TimelineItem oldItem, @NonNull TimelineItem newItem) {
            if (oldItem.getType() != newItem.getType()) return false;
            if (oldItem.getType() == TimelineItem.TYPE_YEAR_HEADER) {
                return oldItem.getYear() == newItem.getYear();
            } else {
                return oldItem.getPhoto() != null && newItem.getPhoto() != null &&
                        oldItem.getPhoto().getId().equals(newItem.getPhoto().getId());
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull TimelineItem oldItem, @NonNull TimelineItem newItem) {
            if (oldItem.getType() == TimelineItem.TYPE_YEAR_HEADER) {
                return oldItem.getYear() == newItem.getYear() && oldItem.getPhotoCount() == newItem.getPhotoCount();
            } else {
                PhotoEntity p1 = oldItem.getPhoto();
                PhotoEntity p2 = newItem.getPhoto();
                if (p1 == null || p2 == null) return false;
                return p1.getPath().equals(p2.getPath())
                        && (p1.getCaption() != null ? p1.getCaption().equals(p2.getCaption()) : p2.getCaption() == null)
                        && p1.getYear() == p2.getYear();
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
        if (viewType == TimelineItem.TYPE_YEAR_HEADER) {
            ItemTimelineYearHeaderBinding binding = ItemTimelineYearHeaderBinding.inflate(inflater, parent, false);
            return new YearHeaderViewHolder(binding);
        } else {
            ItemTimelinePhotoBinding binding = ItemTimelinePhotoBinding.inflate(inflater, parent, false);
            return new PhotoViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TimelineItem item = getItem(position);
        if (holder instanceof YearHeaderViewHolder) {
            ((YearHeaderViewHolder) holder).bind(item);
        } else if (holder instanceof PhotoViewHolder) {
            ((PhotoViewHolder) holder).bind(item.getPhoto());
        }
    }

    class YearHeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimelineYearHeaderBinding binding;

        YearHeaderViewHolder(ItemTimelineYearHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TimelineItem item) {
            binding.tvTimelineYear.setText(String.valueOf(item.getYear()));
            int count = item.getPhotoCount();
            binding.tvPhotoCount.setText(count + (count == 1 ? " Memory" : " Memories"));
        }
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimelinePhotoBinding binding;

        PhotoViewHolder(ItemTimelinePhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    TimelineItem item = getItem(pos);
                    if (item.getPhoto() != null) {
                        listener.onPhotoClick(item.getPhoto());
                    }
                }
            });
        }

        void bind(PhotoEntity photo) {
            binding.tvBadgeYear.setText(String.valueOf(photo.getYear()));

            if (photo.getCaption() != null && !photo.getCaption().trim().isEmpty()) {
                binding.tvTimelineCaption.setText(photo.getCaption());
                binding.tvTimelineCaption.setVisibility(View.VISIBLE);
            } else {
                binding.tvTimelineCaption.setVisibility(View.GONE);
            }

            // Tagged Persons (Supports multiple tagged relatives)
            binding.chipGroupTimelineTaggedPersons.removeAllViews();
            List<String> taggedIds = photo.getTaggedPersonIds();
            if ((taggedIds == null || taggedIds.isEmpty()) && photo.getPersonId() != null && !photo.getPersonId().isEmpty()) {
                taggedIds = java.util.Collections.singletonList(photo.getPersonId());
            }

            if (taggedIds != null && !taggedIds.isEmpty()) {
                boolean anyAdded = false;
                for (String pId : taggedIds) {
                    String name = personNameMap.get(pId);
                    if (name != null && !name.isEmpty()) {
                        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(binding.getRoot().getContext());
                        chip.setText(name);
                        chip.setCheckable(false);
                        chip.setClickable(false);
                        chip.setChipIconResource(android.R.drawable.ic_menu_myplaces);
                        chip.setEnsureMinTouchTargetSize(false);
                        binding.chipGroupTimelineTaggedPersons.addView(chip);
                        anyAdded = true;
                    }
                }
                binding.chipGroupTimelineTaggedPersons.setVisibility(anyAdded ? View.VISIBLE : View.GONE);
            } else {
                binding.chipGroupTimelineTaggedPersons.setVisibility(View.GONE);
            }

            // Load photo
            File file = new File(photo.getPath());
            Glide.with(binding.ivTimelinePhoto.getContext())
                    .load(file.exists() ? file : photo.getPath())
                    .placeholder(R.color.surface_variant)
                    .error(R.color.surface_variant)
                    .centerCrop()
                    .into(binding.ivTimelinePhoto);
        }
    }
}
