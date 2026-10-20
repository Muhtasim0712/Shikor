package com.shikor.app.ui.main;

import android.graphics.Bitmap;
import android.content.Intent;
import android.os.Bundle;
import com.shikor.app.util.TreeExporter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.databinding.FragmentTreeBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.ui.person.PersonViewModel;
import com.shikor.app.util.Prefs;
import com.shikor.app.view.TreePainter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * TreeFragment - Displays the interactive family tree canvas view and root controls.
 *
 * CONCEPT: Hosts FamilyTreeView with dual view modes (Living Natural Tree vs Classic Chart),
 * full screen zoom/fit controls, toolbar menu, and explicit Intent navigation to PersonDetailActivity.
 */
public class TreeFragment extends Fragment {

    private FragmentTreeBinding binding;
    private PersonViewModel personViewModel;
    private List<PersonEntity> membersList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTreeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        personViewModel = new ViewModelProvider(requireActivity()).get(PersonViewModel.class);
        personViewModel.syncFromCloud();

        setupTreeView();
        setupControls();
        setupToolbarMenu();
        observeFamilyMembers();
    }

    private void setupTreeView() {
        // Explicit Intent to PersonDetailActivity on node tap
        binding.familyTreeView.setOnPersonClickListener(personId -> {
            Intent intent = new Intent(requireContext(), PersonDetailActivity.class);
            intent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, personId);
            startActivity(intent);
        });

        binding.btnReCenter.setOnClickListener(v -> binding.familyTreeView.reCenterTree());
        binding.btnFitTree.setOnClickListener(v -> binding.familyTreeView.fitWholeTree());
        binding.btnDiurnalPhase.setOnClickListener(v -> showDiurnalAtmosphereDialog());

        updateToggleButtonIcon(binding.familyTreeView.getViewMode());
        binding.btnToggleMode.setOnClickListener(v -> {
            String currentMode = binding.familyTreeView.getViewMode();
            String newMode = Prefs.VIEW_MODE_NATURAL_TREE.equals(currentMode)
                    ? Prefs.VIEW_MODE_CLASSIC_CHART
                    : Prefs.VIEW_MODE_NATURAL_TREE;
            setTreeMode(newMode);
        });
    }

    private void setTreeMode(String mode) {
        binding.familyTreeView.setViewMode(mode);
        updateToggleButtonIcon(mode);
        requireActivity().invalidateOptionsMenu();
    }

    private void updateToggleButtonIcon(String mode) {
        if (binding == null) return;
        if (Prefs.VIEW_MODE_NATURAL_TREE.equals(mode)) {
            binding.btnToggleMode.setIconResource(R.drawable.ic_chart_view);
            binding.btnToggleMode.setContentDescription(getString(R.string.tree_view_chart));
            binding.btnDiurnalPhase.setVisibility(View.VISIBLE);
        } else {
            binding.btnToggleMode.setIconResource(R.drawable.ic_nature_tree);
            binding.btnToggleMode.setContentDescription(getString(R.string.tree_view_natural));
            binding.btnDiurnalPhase.setVisibility(View.GONE);
        }
    }

    private void showDiurnalAtmosphereDialog() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        String currentTimeStr = sdf.format(new java.util.Date());
        TreePainter.DiurnalPhase currentAutoPhase = TreePainter.DiurnalPhase.fromCurrentTime();

        String autoLabel = getString(R.string.diurnal_auto_opt, currentTimeStr + " (" + currentAutoPhase.displayName + ")");
        String[] options = new String[]{
                autoLabel,
                getString(R.string.diurnal_day_opt),
                getString(R.string.diurnal_afternoon_opt),
                getString(R.string.diurnal_evening_opt),
                getString(R.string.diurnal_night_opt)
        };

        TreePainter.DiurnalPhase currentOverride = binding.familyTreeView.getDiurnalPhaseOverride();
        int selectedIndex = 0;
        if (currentOverride == TreePainter.DiurnalPhase.DAY) selectedIndex = 1;
        else if (currentOverride == TreePainter.DiurnalPhase.AFTERNOON) selectedIndex = 2;
        else if (currentOverride == TreePainter.DiurnalPhase.EVENING) selectedIndex = 3;
        else if (currentOverride == TreePainter.DiurnalPhase.NIGHT) selectedIndex = 4;

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.tree_diurnal_weather)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    TreePainter.DiurnalPhase chosenPhase = null;
                    String chosenName = autoLabel;
                    if (which == 1) {
                        chosenPhase = TreePainter.DiurnalPhase.DAY;
                        chosenName = getString(R.string.diurnal_day_opt);
                    } else if (which == 2) {
                        chosenPhase = TreePainter.DiurnalPhase.AFTERNOON;
                        chosenName = getString(R.string.diurnal_afternoon_opt);
                    } else if (which == 3) {
                        chosenPhase = TreePainter.DiurnalPhase.EVENING;
                        chosenName = getString(R.string.diurnal_evening_opt);
                    } else if (which == 4) {
                        chosenPhase = TreePainter.DiurnalPhase.NIGHT;
                        chosenName = getString(R.string.diurnal_night_opt);
                    }

                    binding.familyTreeView.setDiurnalPhaseOverride(chosenPhase);
                    Toast.makeText(requireContext(), getString(R.string.diurnal_applied_fmt, chosenName), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupToolbarMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_tree_view, menu);

                String currentMode = binding != null
                        ? binding.familyTreeView.getViewMode()
                        : Prefs.getInstance(requireContext()).getTreeViewMode();

                if (Prefs.VIEW_MODE_CLASSIC_CHART.equals(currentMode)) {
                    MenuItem chartItem = menu.findItem(R.id.menu_mode_classic_chart);
                    if (chartItem != null) chartItem.setChecked(true);
                } else {
                    MenuItem treeItem = menu.findItem(R.id.menu_mode_natural_tree);
                    if (treeItem != null) treeItem.setChecked(true);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_fit_tree) {
                    if (binding != null) {
                        binding.familyTreeView.fitWholeTree();
                    }
                    return true;
                } else if (itemId == R.id.menu_mode_natural_tree) {
                    menuItem.setChecked(true);
                    setTreeMode(Prefs.VIEW_MODE_NATURAL_TREE);
                    return true;
                } else if (itemId == R.id.menu_mode_classic_chart) {
                    menuItem.setChecked(true);
                    setTreeMode(Prefs.VIEW_MODE_CLASSIC_CHART);
                    return true;
                } else if (itemId == R.id.action_export_tree) {
                    showExportTreeDialog();
                    return true;
                } else if (itemId == R.id.menu_export_tree_png) {
                    if (binding != null) {
                        Bitmap bmp = binding.familyTreeView.exportFullTreeBitmap();
                        String familyName = Prefs.getInstance(requireContext()).getFamilyName();
                        TreeExporter.exportTreeAsPng(requireActivity(), bmp, familyName, membersList);
                    }
                    return true;
                } else if (itemId == R.id.menu_export_tree_pdf) {
                    if (binding != null) {
                        Bitmap bmp = binding.familyTreeView.exportFullTreeBitmap();
                        String familyName = Prefs.getInstance(requireContext()).getFamilyName();
                        TreeExporter.exportTreeAsPdf(requireActivity(), bmp, familyName, membersList);
                    }
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showExportTreeDialog() {
        if (!isAdded() || getContext() == null) return;
        String[] options = {
                getString(R.string.export_tree_pdf),
                getString(R.string.export_tree_png)
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.action_export))
                .setItems(options, (dialog, which) -> {
                    if (binding == null || !isAdded()) return;
                    Bitmap bmp = binding.familyTreeView.exportFullTreeBitmap();
                    String familyName = Prefs.getInstance(requireContext()).getFamilyName();
                    if (which == 0) {
                        TreeExporter.exportTreeAsPdf(requireActivity(), bmp, familyName, membersList);
                    } else {
                        TreeExporter.exportTreeAsPng(requireActivity(), bmp, familyName, membersList);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupControls() {
        binding.spinnerTreeRoot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    binding.familyTreeView.setRoot(null);
                } else if (position - 1 < membersList.size()) {
                    binding.familyTreeView.setRoot(membersList.get(position - 1).getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void observeFamilyMembers() {
        personViewModel.getPeople().observe(getViewLifecycleOwner(), people -> {
            if (people != null && !people.isEmpty()) {
                membersList = people;
                binding.treeEmptyState.emptyStateLayout.setVisibility(View.GONE);
                binding.familyTreeView.setVisibility(View.VISIBLE);

                // Populate tree view with data
                binding.familyTreeView.setPeople(people);

                // Populate Root Selector Spinner
                List<String> names = new ArrayList<>();
                names.add("All Generations (Default)");
                for (PersonEntity p : people) {
                    names.add(p.getFullName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item, names
                );
                binding.spinnerTreeRoot.setAdapter(adapter);
            } else {
                binding.treeEmptyState.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.familyTreeView.setVisibility(View.GONE);
                binding.treeEmptyState.tvEmptyTitle.setText(R.string.empty_tree_title);
                binding.treeEmptyState.tvEmptySubtitle.setText(R.string.empty_tree_subtitle);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
