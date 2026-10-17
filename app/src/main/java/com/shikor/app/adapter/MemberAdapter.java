package com.shikor.app.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.ItemMemberBinding;

/**
 * MemberAdapter - RecyclerView adapter for family members list.
 *
 * CONCEPT: ListAdapter with DiffUtil.ItemCallback and View Binding in ViewHolder
 */
public class MemberAdapter extends ListAdapter<PersonEntity, MemberAdapter.MemberViewHolder> {

    public interface OnMemberClickListener {
        void onMemberClick(PersonEntity person);
    }

    private final OnMemberClickListener listener;

    public MemberAdapter(OnMemberClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // CONCEPT: Efficient RecyclerView updates using DiffUtil
    private static final DiffUtil.ItemCallback<PersonEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<PersonEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull PersonEntity oldItem, @NonNull PersonEntity newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PersonEntity oldItem, @NonNull PersonEntity newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberBinding binding = ItemMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        PersonEntity person = getItem(position);
        holder.bind(person, listener);
    }

    public PersonEntity getPersonAt(int position) {
        return getItem(position);
    }

    // CONCEPT: ViewHolder holding View Binding
    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberBinding binding;

        public MemberViewHolder(@NonNull ItemMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PersonEntity person, OnMemberClickListener listener) {
            String displayName = person.getFullName();
            if (person.getNickName() != null && !person.getNickName().isEmpty()) {
                displayName += " (" + person.getNickName() + ")";
            }
            binding.tvMemberName.setText(displayName);

            // Subtitle info: Occupation or hometown
            String subtitle = person.getOccupation();
            if (TextUtils.isEmpty(subtitle)) {
                subtitle = person.getHometown();
            }
            if (!TextUtils.isEmpty(subtitle)) {
                binding.tvMemberRelation.setText(subtitle);
                binding.tvMemberRelation.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvMemberRelation.setVisibility(android.view.View.GONE);
            }

            // Lifespan: "1945 – 2020" or "b. 1998"
            binding.tvMemberLifespan.setText(person.getLifeSpan());

            // Setup Custom AvatarView
            binding.avatarView.setGender(person.getGender());
            binding.avatarView.setDeceased(person.isDeceased());
            binding.avatarView.setInitials(person.getInitials());
            binding.avatarView.setPhotoPath(person.getPhotoPath());

            // Click listener
            binding.cardMember.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMemberClick(person);
                }
            });
        }
    }
}
