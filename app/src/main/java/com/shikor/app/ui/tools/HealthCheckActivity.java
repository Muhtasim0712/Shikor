package com.shikor.app.ui.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.shikor.app.R;
import com.shikor.app.adapter.HealthCheckAdapter;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.person.PersonDetailActivity;

import java.util.Locale;

/**
 * HealthCheckActivity - Displays tree health inspection results with severity counts,
 * 'Re-run' capability, and direct navigation to affected member profiles.
 *
 * CONCEPT: Read-only health analysis, RecyclerView ListAdapter, MVVM ViewModel, and Accessibility.
 */
public class HealthCheckActivity extends BaseActivity {

    private HealthCheckViewModel viewModel;
    private HealthCheckAdapter adapter;

    private ProgressBar progressHealth;
    private TextView tvHealthEmpty;
    private RecyclerView rvHealthIssues;
    private TextView tvHighCount, tvMediumCount, tvLowCount;
    private MaterialButton btnRerunCheck;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_check);
        applySystemWindowInsets(findViewById(R.id.rootHealth));

        MaterialToolbar toolbar = findViewById(R.id.toolbarHealth);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressHealth = findViewById(R.id.progressHealth);
        tvHealthEmpty = findViewById(R.id.tvHealthEmpty);
        rvHealthIssues = findViewById(R.id.rvHealthIssues);
        tvHighCount = findViewById(R.id.tvHighCount);
        tvMediumCount = findViewById(R.id.tvMediumCount);
        tvLowCount = findViewById(R.id.tvLowCount);
        btnRerunCheck = findViewById(R.id.btnRerunCheck);

        adapter = new HealthCheckAdapter(issue -> {
            if (issue.personId != null && !issue.personId.isEmpty()) {
                Intent intent = new Intent(this, PersonDetailActivity.class);
                intent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, issue.personId);
                startActivity(intent);
            }
        });

        rvHealthIssues.setLayoutManager(new LinearLayoutManager(this));
        rvHealthIssues.setAdapter(adapter);

        btnRerunCheck.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.runCheck();
            }
        });

        viewModel = new ViewModelProvider(this).get(HealthCheckViewModel.class);
        viewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressHealth.setVisibility(View.VISIBLE);
                rvHealthIssues.setVisibility(View.GONE);
                tvHealthEmpty.setVisibility(View.GONE);
            } else {
                progressHealth.setVisibility(View.GONE);
            }
        });

        viewModel.getSummaryLiveData().observe(this, summary -> {
            if (summary != null) {
                tvHighCount.setText(String.format(Locale.getDefault(), "%d Critical", summary.highCount));
                tvMediumCount.setText(String.format(Locale.getDefault(), "%d Warning", summary.mediumCount));
                tvLowCount.setText(String.format(Locale.getDefault(), "%d Info", summary.lowCount));
            }
        });

        viewModel.getIssuesLiveData().observe(this, issues -> {
            if (issues == null || issues.isEmpty()) {
                rvHealthIssues.setVisibility(View.GONE);
                tvHealthEmpty.setVisibility(View.VISIBLE);
                adapter.submitList(null);
            } else {
                tvHealthEmpty.setVisibility(View.GONE);
                rvHealthIssues.setVisibility(View.VISIBLE);
                adapter.submitList(issues);
            }
        });
    }
}
