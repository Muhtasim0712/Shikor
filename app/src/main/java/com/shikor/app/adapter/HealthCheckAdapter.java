package com.shikor.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.shikor.app.R;
import com.shikor.app.util.HealthChecker;

import java.util.Objects;

/**
 * HealthCheckAdapter - ListAdapter displaying tree health issues with severity icons and colors.
 * CONCEPT: ListAdapter with DiffUtil.ItemCallback, View Binding / View Holders, and item click callbacks.
 */
public class HealthCheckAdapter extends ListAdapter<HealthChecker.HealthIssue, HealthCheckAdapter.ViewHolder> {

    public interface OnIssueClickListener {
        void onIssueClick(HealthChecker.HealthIssue issue);
    }

    private final OnIssueClickListener listener;

    public HealthCheckAdapter(OnIssueClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<HealthChecker.HealthIssue> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<HealthChecker.HealthIssue>() {
                @Override
                public boolean areItemsTheSame(@NonNull HealthChecker.HealthIssue oldItem, @NonNull HealthChecker.HealthIssue newItem) {
                    return Objects.equals(oldItem.personId, newItem.personId) && Objects.equals(oldItem.issueTitle, newItem.issueTitle);
                }

                @Override
                public boolean areContentsTheSame(@NonNull HealthChecker.HealthIssue oldItem, @NonNull HealthChecker.HealthIssue newItem) {
                    return Objects.equals(oldItem.issueDescription, newItem.issueDescription) && oldItem.severity == newItem.severity;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_issue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthChecker.HealthIssue issue = getItem(position);
        if (issue == null) return;

        holder.tvIssueTitle.setText(issue.issueTitle != null ? issue.issueTitle : "Issue");
        holder.tvAffectedName.setText(issue.personName != null ? issue.personName : "Relative");
        holder.tvIssueDescription.setText(issue.issueDescription != null ? issue.issueDescription : "");

        int badgeBg;
        int iconRes;

        if (issue.severity == HealthChecker.Severity.HIGH) {
            badgeBg = ContextCompat.getColor(holder.itemView.getContext(), R.color.error);
            iconRes = android.R.drawable.ic_dialog_alert;
            holder.tvSeverityBadge.setText("CRITICAL");
        } else if (issue.severity == HealthChecker.Severity.MEDIUM) {
            badgeBg = ContextCompat.getColor(holder.itemView.getContext(), R.color.tertiary);
            iconRes = android.R.drawable.ic_dialog_info;
            holder.tvSeverityBadge.setText("WARNING");
        } else {
            badgeBg = ContextCompat.getColor(holder.itemView.getContext(), R.color.gender_male);
            iconRes = android.R.drawable.ic_menu_info_details;
            holder.tvSeverityBadge.setText("INFO");
        }

        holder.tvSeverityBadge.setBackgroundColor(badgeBg);
        holder.ivSeverityIcon.setImageResource(iconRes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onIssueClick(issue);
        });

        holder.itemView.setContentDescription(issue.severity + " severity issue: " + issue.issueTitle + " for " + issue.personName);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView ivSeverityIcon;
        final TextView tvIssueTitle;
        final TextView tvSeverityBadge;
        final TextView tvAffectedName;
        final TextView tvIssueDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardHealthItem);
            ivSeverityIcon = itemView.findViewById(R.id.ivSeverityIcon);
            tvIssueTitle = itemView.findViewById(R.id.tvIssueTitle);
            tvSeverityBadge = itemView.findViewById(R.id.tvSeverityBadge);
            tvAffectedName = itemView.findViewById(R.id.tvAffectedName);
            tvIssueDescription = itemView.findViewById(R.id.tvIssueDescription);
        }
    }
}
