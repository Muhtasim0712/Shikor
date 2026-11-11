package com.shikor.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.shikor.app.R;
import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.databinding.ItemStoryPhotoBinding;
import com.shikor.app.databinding.ItemStoryTextAudioBinding;
import com.shikor.app.databinding.ItemTimelinePhotoBinding;
import com.shikor.app.databinding.ItemTimelineYearHeaderBinding;
import com.shikor.app.ui.timeline.TimelineItem;
import com.shikor.app.util.ImageEncoder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * TimelineAdapter - ListAdapter rendering chronological photo and story memory milestones grouped by Year Headers.
 */
public class TimelineAdapter extends ListAdapter<TimelineItem, RecyclerView.ViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoEntity photo);
    }

    public interface OnStoryClickListener {
        void onStoryClick(StoryEntity story);
    }

    private static final int TYPE_YEAR_HEADER = 0;
    private static final int TYPE_PHOTO = 1;
    private static final int TYPE_STORY_TEXT = 2;
    private static final int TYPE_STORY_PHOTO = 3;

    private final OnPhotoClickListener photoClickListener;
    private final OnStoryClickListener storyClickListener;
    private final Map<String, String> personNameMap = new HashMap<>();

    public TimelineAdapter(OnPhotoClickListener photoClickListener, OnStoryClickListener storyClickListener) {
        super(DIFF_CALLBACK);
        this.photoClickListener = photoClickListener;
        this.storyClickListener = storyClickListener;
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
            } else if (oldItem.getType() == TimelineItem.TYPE_PHOTO) {
                return oldItem.getPhoto() != null && newItem.getPhoto() != null &&
                        Objects.equals(oldItem.getPhoto().getId(), newItem.getPhoto().getId());
            } else {
                return oldItem.getStory() != null && newItem.getStory() != null &&
                        Objects.equals(oldItem.getStory().getId(), newItem.getStory().getId());
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull TimelineItem oldItem, @NonNull TimelineItem newItem) {
            if (oldItem.getType() == TimelineItem.TYPE_YEAR_HEADER) {
                return oldItem.getYear() == newItem.getYear() && oldItem.getCount() == newItem.getCount();
            } else if (oldItem.getType() == TimelineItem.TYPE_PHOTO) {
                PhotoEntity p1 = oldItem.getPhoto();
                PhotoEntity p2 = newItem.getPhoto();
                if (p1 == null || p2 == null) return false;
                return Objects.equals(p1.getPath(), p2.getPath()) && Objects.equals(p1.getCaption(), p2.getCaption());
            } else {
                StoryEntity s1 = oldItem.getStory();
                StoryEntity s2 = newItem.getStory();
                if (s1 == null || s2 == null) return false;
                return Objects.equals(s1.getTitle(), s2.getTitle()) && Objects.equals(s1.getBody(), s2.getBody())
                        && Objects.equals(s1.getPhotoPath(), s2.getPhotoPath());
            }
        }
    };

    @Override
    public int getItemViewType(int position) {
        TimelineItem item = getItem(position);
        if (item.getType() == TimelineItem.TYPE_YEAR_HEADER) return TYPE_YEAR_HEADER;
        if (item.getType() == TimelineItem.TYPE_PHOTO) return TYPE_PHOTO;
        if (item.getStory() != null && item.getStory().getPhotoPath() != null && !item.getStory().getPhotoPath().trim().isEmpty()) {
            return TYPE_STORY_PHOTO;
        }
        return TYPE_STORY_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_YEAR_HEADER) {
            ItemTimelineYearHeaderBinding binding = ItemTimelineYearHeaderBinding.inflate(inflater, parent, false);
            return new YearHeaderViewHolder(binding);
        } else if (viewType == TYPE_STORY_PHOTO) {
            ItemStoryPhotoBinding binding = ItemStoryPhotoBinding.inflate(inflater, parent, false);
            return new StoryPhotoViewHolder(binding);
        } else if (viewType == TYPE_STORY_TEXT) {
            ItemStoryTextAudioBinding binding = ItemStoryTextAudioBinding.inflate(inflater, parent, false);
            return new StoryTextViewHolder(binding);
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
        } else if (holder instanceof StoryPhotoViewHolder) {
            ((StoryPhotoViewHolder) holder).bind(item.getStory());
        } else if (holder instanceof StoryTextViewHolder) {
            ((StoryTextViewHolder) holder).bind(item.getStory());
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
            int count = item.getCount();
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
                if (pos != RecyclerView.NO_POSITION && photoClickListener != null) {
                    TimelineItem item = getItem(pos);
                    if (item.getPhoto() != null) {
                        photoClickListener.onPhotoClick(item.getPhoto());
                    }
                }
            });
        }

        void bind(PhotoEntity photo) {
            if (photo == null) return;
            binding.tvBadgeYear.setText(String.valueOf(photo.getYear()));

            if (photo.getCaption() != null && !photo.getCaption().trim().isEmpty()) {
                binding.tvTimelineCaption.setText(photo.getCaption());
                binding.tvTimelineCaption.setVisibility(View.VISIBLE);
            } else {
                binding.tvTimelineCaption.setVisibility(View.GONE);
            }

            // Tagged Persons
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

            ImageEncoder.loadImage(binding.ivTimelinePhoto, photo.getPath());
        }
    }

    class StoryPhotoViewHolder extends RecyclerView.ViewHolder {
        private final ItemStoryPhotoBinding binding;

        StoryPhotoViewHolder(ItemStoryPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && storyClickListener != null) {
                    TimelineItem item = getItem(pos);
                    if (item.getStory() != null) {
                        storyClickListener.onStoryClick(item.getStory());
                    }
                }
            });
        }

        void bind(StoryEntity story) {
            if (story == null) return;
            binding.tvStoryTitle.setText(story.getTitle() != null ? story.getTitle() : "Family Memory");

            String personName = personNameMap.get(story.getPersonId());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(story.getCreatedAt() > 0 ? story.getCreatedAt() : System.currentTimeMillis()));
            if (personName != null && !personName.isEmpty()) {
                binding.tvStoryDate.setText("Memory for " + personName + " • " + dateStr);
            } else {
                binding.tvStoryDate.setText(dateStr);
            }

            if (story.getBody() != null && !story.getBody().trim().isEmpty()) {
                binding.tvStoryExcerpt.setText(story.getBody());
                binding.tvStoryExcerpt.setVisibility(View.VISIBLE);
            } else {
                binding.tvStoryExcerpt.setVisibility(View.GONE);
            }

            if (story.getAudioPath() != null && !story.getAudioPath().isEmpty()) {
                binding.layoutAudioBadge.setVisibility(View.VISIBLE);
                long sec = story.getAudioDurationMs() / 1000;
                binding.tvAudioDurationBadge.setText(String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60));
            } else {
                binding.layoutAudioBadge.setVisibility(View.GONE);
            }

            ImageEncoder.loadImage(binding.ivStoryPhoto, story.getPhotoPath());
        }
    }

    class StoryTextViewHolder extends RecyclerView.ViewHolder {
        private final ItemStoryTextAudioBinding binding;

        StoryTextViewHolder(ItemStoryTextAudioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && storyClickListener != null) {
                    TimelineItem item = getItem(pos);
                    if (item.getStory() != null) {
                        storyClickListener.onStoryClick(item.getStory());
                    }
                }
            });
        }

        void bind(StoryEntity story) {
            if (story == null) return;
            binding.tvStoryTitle.setText(story.getTitle() != null ? story.getTitle() : "Family Memory");

            String personName = personNameMap.get(story.getPersonId());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(story.getCreatedAt() > 0 ? story.getCreatedAt() : System.currentTimeMillis()));
            if (personName != null && !personName.isEmpty()) {
                binding.tvStoryDate.setText("Memory for " + personName + " • " + dateStr);
            } else {
                binding.tvStoryDate.setText(dateStr);
            }

            if (story.getBody() != null && !story.getBody().trim().isEmpty()) {
                binding.tvStoryExcerpt.setText(story.getBody());
                binding.tvStoryExcerpt.setVisibility(View.VISIBLE);
            } else {
                binding.tvStoryExcerpt.setVisibility(View.GONE);
            }

            if (story.getAudioPath() != null && !story.getAudioPath().isEmpty()) {
                binding.layoutAudioCapsule.setVisibility(View.VISIBLE);
                long sec = story.getAudioDurationMs() / 1000;
                binding.tvAudioCapsuleDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60));
            } else {
                binding.layoutAudioCapsule.setVisibility(View.GONE);
            }
        }
    }
}
