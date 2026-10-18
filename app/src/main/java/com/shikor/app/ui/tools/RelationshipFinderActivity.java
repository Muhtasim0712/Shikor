package com.shikor.app.ui.tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.ActivityRelationshipFinderBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.person.PersonViewModel;
import com.shikor.app.util.KinshipResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * RelationshipFinderActivity - Calculates shortest genealogical paths and cultural kinship terms
 * between any two relatives in the family tree.
 *
 * CONCEPT: Graph traversal with KinshipResolver, XML-driven kinship terms, and interactive path breakdown.
 */
public class RelationshipFinderActivity extends BaseActivity {

    public static final String EXTRA_INITIAL_PERSON_ID = "extra_initial_person_id";

    private ActivityRelationshipFinderBinding binding;
    private PersonViewModel personViewModel;

    private final List<PersonEntity> peopleList = new ArrayList<>();
    private final List<String> personNames = new ArrayList<>();
    private PersonEntity selectedPersonA;
    private PersonEntity selectedPersonB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRelationshipFinderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        personViewModel = new ViewModelProvider(this).get(PersonViewModel.class);

        setupListeners();
        observePeople();
    }

    private void setupListeners() {
        binding.btnCalculate.setOnClickListener(v -> calculateRelationship());

        binding.btnSwap.setOnClickListener(v -> {
            PersonEntity temp = selectedPersonA;
            selectedPersonA = selectedPersonB;
            selectedPersonB = temp;

            binding.actvPersonA.setText(selectedPersonA != null ? formatDisplayName(selectedPersonA) : "", false);
            binding.actvPersonB.setText(selectedPersonB != null ? formatDisplayName(selectedPersonB) : "", false);

            if (selectedPersonA != null && selectedPersonB != null) {
                calculateRelationship();
            }
        });
    }

    private void observePeople() {
        personViewModel.getPeople().observe(this, people -> {
            if (people == null || people.isEmpty()) {
                Toast.makeText(this, "Add family members first to find relationships.", Toast.LENGTH_LONG).show();
                return;
            }

            peopleList.clear();
            peopleList.addAll(people);

            personNames.clear();
            for (PersonEntity p : peopleList) {
                personNames.add(formatDisplayName(p));
            }

            ArrayAdapter<String> adapterA = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, personNames);
            ArrayAdapter<String> adapterB = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, personNames);

            binding.actvPersonA.setAdapter(adapterA);
            binding.actvPersonB.setAdapter(adapterB);

            binding.actvPersonA.setOnItemClickListener((parent, view, position, id) -> {
                selectedPersonA = peopleList.get(position);
            });

            binding.actvPersonB.setOnItemClickListener((parent, view, position, id) -> {
                selectedPersonB = peopleList.get(position);
            });

            // Handle optional initial pre-selection
            String initialId = getIntent().getStringExtra(EXTRA_INITIAL_PERSON_ID);
            if (initialId != null) {
                for (int i = 0; i < peopleList.size(); i++) {
                    if (peopleList.get(i).getId().equals(initialId)) {
                        selectedPersonA = peopleList.get(i);
                        binding.actvPersonA.setText(formatDisplayName(selectedPersonA), false);
                        break;
                    }
                }
            } else if (!peopleList.isEmpty()) {
                selectedPersonA = peopleList.get(0);
                binding.actvPersonA.setText(formatDisplayName(selectedPersonA), false);
            }

            if (peopleList.size() > 1 && selectedPersonB == null) {
                selectedPersonB = peopleList.get(1);
                binding.actvPersonB.setText(formatDisplayName(selectedPersonB), false);
            }
        });
    }

    private String formatDisplayName(PersonEntity p) {
        String name = p.getFullName();
        if (p.getNickName() != null && !p.getNickName().isEmpty()) {
            name += " (" + p.getNickName() + ")";
        }
        return name;
    }

    private void calculateRelationship() {
        if (selectedPersonA == null || selectedPersonB == null) {
            Toast.makeText(this, "Please select both Person A and Person B.", Toast.LENGTH_SHORT).show();
            return;
        }

        KinshipResolver.KinshipResult result = KinshipResolver.resolveRelationship(
                this, peopleList, selectedPersonA, selectedPersonB
        );

        binding.cardResult.setVisibility(View.VISIBLE);
        binding.tvKinshipBangla.setText(result.banglaTerm);
        binding.tvKinshipEnglish.setText(result.englishTerm);
        binding.tvKinshipExplanation.setText(result.description);

        renderPathSteps(result.path);
    }

    private void renderPathSteps(List<KinshipResolver.PathStep> path) {
        binding.layoutPathSteps.removeAllViews();
        if (path == null || path.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < path.size(); i++) {
            KinshipResolver.PathStep step = path.get(i);
            View stepView = inflater.inflate(R.layout.item_relationship_step, binding.layoutPathSteps, false);

            TextView tvStepRelation = stepView.findViewById(R.id.tvStepRelation);
            TextView tvStepPersonName = stepView.findViewById(R.id.tvStepPersonName);
            View indicatorLine = stepView.findViewById(R.id.indicatorLine);

            if (i == 0) {
                tvStepRelation.setText("Start Relative");
            } else {
                tvStepRelation.setText("↳ " + step.relationFromPrevious + " of previous");
            }

            tvStepPersonName.setText(step.person != null ? step.person.getFullName() : "Relative");

            // Hide connecting line on the final target relative
            if (i == path.size() - 1) {
                indicatorLine.setVisibility(View.GONE);
            } else {
                indicatorLine.setVisibility(View.VISIBLE);
            }

            binding.layoutPathSteps.addView(stepView);
        }
    }
}
