package com.shikor.app.ui.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.view.BarChartView;
import com.shikor.app.view.DonutChartView;
import com.shikor.app.util.StatsCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * StatsActivity - Displays family statistics, summary cards, and custom Canvas charts
 * (BarChartView for birth months, DonutChartView for gender split, and hometowns breakdown).
 *
 * CONCEPT: Custom Views & Canvas charts, ViewModel & LiveData, Room aggregates, and Accessibility.
 */
public class StatsActivity extends BaseActivity {

    private StatsViewModel viewModel;
    private ProgressBar progressStats;
    private TextView tvStatsEmpty;
    private View layoutStatsContent;

    private TextView tvStatTotalMembers, tvStatLiving, tvStatDeceased, tvStatGenerations, tvStatAvgLifespan;
    private TextView tvStatOldestName, tvStatYoungestName, tvStatStories, tvStatAudioMins, tvStatPhotos;
    private BarChartView barChartView;
    private DonutChartView donutChartView;
    private LinearLayout layoutHometownsContainer;

    private PersonEntity oldestPerson;
    private PersonEntity youngestPerson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        MaterialToolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressStats = findViewById(R.id.progressStats);
        tvStatsEmpty = findViewById(R.id.tvStatsEmpty);
        layoutStatsContent = findViewById(R.id.layoutStatsContent);

        tvStatTotalMembers = findViewById(R.id.tvStatTotalMembers);
        tvStatLiving = findViewById(R.id.tvStatLiving);
        tvStatDeceased = findViewById(R.id.tvStatDeceased);
        tvStatGenerations = findViewById(R.id.tvStatGenerations);
        tvStatAvgLifespan = findViewById(R.id.tvStatAvgLifespan);
        tvStatOldestName = findViewById(R.id.tvStatOldestName);
        tvStatYoungestName = findViewById(R.id.tvStatYoungestName);
        tvStatStories = findViewById(R.id.tvStatStories);
        tvStatAudioMins = findViewById(R.id.tvStatAudioMins);
        tvStatPhotos = findViewById(R.id.tvStatPhotos);

        barChartView = findViewById(R.id.barChartView);
        donutChartView = findViewById(R.id.donutChartView);
        layoutHometownsContainer = findViewById(R.id.layoutHometownsContainer);

        tvStatOldestName.setOnClickListener(v -> {
            if (oldestPerson != null) {
                Intent intent = new Intent(this, PersonDetailActivity.class);
                intent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, oldestPerson.getId());
                startActivity(intent);
            }
        });

        tvStatYoungestName.setOnClickListener(v -> {
            if (youngestPerson != null) {
                Intent intent = new Intent(this, PersonDetailActivity.class);
                intent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, youngestPerson.getId());
                startActivity(intent);
            }
        });

        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);
        viewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressStats.setVisibility(View.VISIBLE);
                layoutStatsContent.setVisibility(View.GONE);
                tvStatsEmpty.setVisibility(View.GONE);
            } else {
                progressStats.setVisibility(View.GONE);
            }
        });

        viewModel.getStatsLiveData().observe(this, stats -> {
            if (stats == null || stats.totalMembers == 0) {
                layoutStatsContent.setVisibility(View.GONE);
                tvStatsEmpty.setVisibility(View.VISIBLE);
            } else {
                tvStatsEmpty.setVisibility(View.GONE);
                layoutStatsContent.setVisibility(View.VISIBLE);
                bindStats(stats);
            }
        });
    }

    private void bindStats(StatsCalculator.FamilyStats stats) {
        tvStatTotalMembers.setText(String.valueOf(stats.totalMembers));
        tvStatLiving.setText(String.valueOf(stats.livingCount));
        tvStatDeceased.setText(String.valueOf(stats.deceasedCount));
        tvStatGenerations.setText(String.valueOf(stats.generationCount));
        tvStatAvgLifespan.setText(String.format(java.util.Locale.getDefault(), "%.1f yrs", stats.averageLifespan));

        oldestPerson = stats.oldestMember;
        if (oldestPerson != null) {
            tvStatOldestName.setText(oldestPerson.getFullName());
            tvStatOldestName.setContentDescription("Oldest member: " + oldestPerson.getFullName());
        } else {
            tvStatOldestName.setText("—");
        }

        youngestPerson = stats.youngestMember;
        if (youngestPerson != null) {
            tvStatYoungestName.setText(youngestPerson.getFullName());
            tvStatYoungestName.setContentDescription("Youngest member: " + youngestPerson.getFullName());
        } else {
            tvStatYoungestName.setText("—");
        }

        tvStatStories.setText(String.valueOf(stats.totalStories));
        tvStatAudioMins.setText(stats.totalAudioMinutes + "m");
        tvStatPhotos.setText(String.valueOf(stats.totalPhotos));

        // 1. Bar Chart: Birthdays per month (Jan - Dec)
        List<String> monthLabels = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        List<Float> monthValues = new ArrayList<>();
        for (int count : stats.birthdaysPerMonth) {
            monthValues.add((float) count);
        }
        barChartView.setData(monthLabels, monthValues);
        barChartView.setContentDescription("Bar chart showing family birthdays per month");

        // 2. Donut Chart: Gender split (Male, Female, Other)
        List<String> genderLabels = List.of("Male", "Female", "Other");
        List<Float> genderValues = List.of((float) stats.maleCount, (float) stats.femaleCount, (float) stats.otherGenderCount);
        List<Integer> genderColors = List.of(
                androidx.core.content.ContextCompat.getColor(this, R.color.primary),
                androidx.core.content.ContextCompat.getColor(this, R.color.tertiary),
                androidx.core.content.ContextCompat.getColor(this, R.color.secondary)
        );
        donutChartView.setData(genderLabels, genderValues, genderColors);
        donutChartView.setContentDescription("Donut chart showing gender split");

        // 3. Hometowns breakdown (Top 5)
        layoutHometownsContainer.removeAllViews();
        int hometownCount = 0;
        for (Map.Entry<String, Integer> entry : stats.hometownsMap.entrySet()) {
            if (hometownCount >= 5) break;
            TextView row = new TextView(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setPadding(0, 8, 0, 8);
            row.setText(entry.getKey() + ": " + entry.getValue() + " member(s)");
            row.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body1);
            layoutHometownsContainer.addView(row);
            hometownCount++;
        }
        if (hometownCount == 0) {
            TextView none = new TextView(this);
            none.setText("No hometown data available");
            layoutHometownsContainer.addView(none);
        }
    }
}
