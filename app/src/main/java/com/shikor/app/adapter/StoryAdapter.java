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
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.databinding.ItemStoryPhotoBinding;
import com.shikor.app.databinding.ItemStoryTextAudioBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * StoryAdapter - RecyclerView ListAdapter with TWO distinct view types:
 * 1. Photo Story: Featured visual memory card with image header and audio indicator badge.
 * 2. Text & Audio Story: Oral history memory card with prominent audio capsule and readable typography.
 */
public class StoryAdapter extends ListAdapter<StoryEntity, RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_PHOTO = 1;
    public static final int VIEW_TYPE_TEXT_AUDIO = 2;

    public interface OnStoryClickListener {
        void onStoryClick(StoryEntity story);
    }

    private final OnStoryClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public StoryAdapter(OnStoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<StoryEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<StoryEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull StoryEntity oldItem, @NonNull StoryEntity newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull StoryEntity oldItem, @NonNull StoryEntity newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && (oldItem.getBody() != null ? oldItem.getBody().equals(newItem.getBody()) : newItem.getBody() == null)
                    && (oldItem.getPhotoPath() != null ? oldItem.getPhotoPath().equals(newItem.getPhotoPath()) : newItem.getPhotoPath() == null)
                    && (oldItem.getAudioPath() != null ? oldItem.getAudioPath().equals(newItem.getAudioPath()) : newItem.getAudioPath() == null);
        }
    };

    @Override
    public int getItemViewType(int position) {
        StoryEntity story = getItem(position);
        if (story.getPhotoPath() != null && !story.getPhotoPath().trim().isEmpty()) {
            return VIEW_TYPE_PHOTO;
        }
        return VIEW_TYPE_TEXT_AUDIO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_PHOTO) {
            ItemStoryPhotoBinding binding = ItemStoryPhotoBinding.inflate(inflater, parent, false);
            return new PhotoStoryViewHolder(binding);
        } else {
            ItemStoryTextAudioBinding binding = ItemStoryTextAudioBinding.inflate(inflater, parent, false);
            return new TextAudioStoryViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StoryEntity story = getItem(position);
        if (holder instanceof PhotoStoryViewHolder) {
            ((PhotoStoryViewHolder) holder).bind(story);
        } else if (holder instanceof TextAudioStoryViewHolder) {
            ((TextAudioStoryViewHolder) holder).bind(story);
        }
    }

    private String formatDuration(long ms) {
        if (ms <= 0) return "00:00";
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
    }

    class PhotoStoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemStoryPhotoBinding binding;

        PhotoStoryViewHolder(ItemStoryPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onStoryClick(getItem(pos));
                }
            });
        }

        void bind(StoryEntity story) {
            binding.tvStoryTitle.setText(story.getTitle());
            binding.tvStoryDate.setText(dateFormat.format(new Date(story.getCreatedAt())));

            if (story.getBody() != null && !story.getBody().trim().isEmpty()) {
                binding.tvStoryExcerpt.setText(story.getBody());
                binding.tvStoryExcerpt.setVisibility(View.VISIBLE);
            } else {
                binding.tvStoryExcerpt.setVisibility(View.GONE);
            }

            // Load photo
            if (story.getPhotoPath() != null) {
                File file = new File(story.getPhotoPath());
                Glide.with(binding.ivStoryPhoto.getContext())
                        .load(file.exists() ? file : story.getPhotoPath())
                        .placeholder(R.color.surface_variant)
                        .error(R.color.surface_variant)
                        .centerCrop()
                        .into(binding.ivStoryPhoto);
            }

            // Audio badge
            if (story.getAudioPath() != null && !story.getAudioPath().isEmpty()) {
                binding.layoutAudioBadge.setVisibility(View.VISIBLE);
                binding.tvAudioDurationBadge.setText(formatDuration(story.getAudioDurationMs()));
            } else {
                binding.layoutAudioBadge.setVisibility(View.GONE);
            }
        }
    }

    class TextAudioStoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemStoryTextAudioBinding binding;

        TextAudioStoryViewHolder(ItemStoryTextAudioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onStoryClick(getItem(pos));
                }
            });
        }

        void bind(StoryEntity story) {
            binding.tvStoryTitle.setText(story.getTitle());
            binding.tvStoryDate.setText(dateFormat.format(new Date(story.getCreatedAt())));

            if (story.getBody() != null && !story.getBody().trim().isEmpty()) {
                binding.tvStoryExcerpt.setText(story.getBody());
                binding.tvStoryExcerpt.setVisibility(View.VISIBLE);
            } else {
                binding.tvStoryExcerpt.setVisibility(View.GONE);
            }

            // Audio Capsule
            if (story.getAudioPath() != null && !story.getAudioPath().isEmpty()) {
                binding.layoutAudioCapsule.setVisibility(View.VISIBLE);
                binding.tvAudioCapsuleDuration.setText(formatDuration(story.getAudioDurationMs()));
            } else {
                binding.layoutAudioCapsule.setVisibility(View.GONE);
            }
        }
    }
}
