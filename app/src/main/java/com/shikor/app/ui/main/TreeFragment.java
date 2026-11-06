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
import com.shikor.app.view.AvatarView;
import com.shikor.app.view.TreePainter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * TreeFragment - Displays the interactive family tree canvas view and root controls.
 * <p>
 * CONCEPT: Hosts FamilyTreeView with dual view modes (Living Natural Tree vs Classic Chart),
 * full screen zoom/fit controls, toolbar menu, and explicit Intent navigation to PersonDetailActivity.
 */
public class TreeFragment extends Fragment {

    private FragmentTreeBinding binding;
    private PersonViewModel personViewModel;
    private List<PersonEntity> membersList = new ArrayList<>();
    private String currentRootPersonId = null;

    public interface OnRootSelectedListener {
        void onSelected(String personId);
    }

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
        if (Prefs.VIEW_MODE_CLASSIC_CHART.equals(mode)) {
            binding.btnToggleMode.setIconResource(R.drawable.ic_nature_tree);
            binding.btnToggleMode.setContentDescription(getString(R.string.tree_view_natural));
            binding.btnDiurnalPhase.setVisibility(View.GONE);
        } else {
            binding.btnToggleMode.setIconResource(R.drawable.ic_chart_view);
            binding.btnToggleMode.setContentDescription(getString(R.string.tree_view_chart));
            binding.btnDiurnalPhase.setVisibility(View.VISIBLE);
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
        int selectedIndex;
        if (currentOverride == TreePainter.DiurnalPhase.DAY) selectedIndex = 1;
        else if (currentOverride == TreePainter.DiurnalPhase.AFTERNOON) selectedIndex = 2;
        else if (currentOverride == TreePainter.DiurnalPhase.EVENING) selectedIndex = 3;
        else if (currentOverride == TreePainter.DiurnalPhase.NIGHT) selectedIndex = 4;
        else selectedIndex = 0;

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.tree_diurnal_weather)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    TreePainter.DiurnalPhase chosenPhase;
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
                    } else {
                        chosenPhase = null;
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

                int itemIdTosetChecked = Prefs.VIEW_MODE_CLASSIC_CHART.equals(currentMode)
                        ? R.id.menu_mode_classic_chart
                        : R.id.menu_mode_natural_tree;
                MenuItem item = menu.findItem(itemIdTosetChecked);
                if (item != null) item.setChecked(true);
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
        binding.btnSelectRoot.setOnClickListener(v -> showRootSelectionDialog());

        binding.spinnerTreeRoot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentRootPersonId = null;
                } else if (position - 1 < membersList.size()) {
                    currentRootPersonId = membersList.get(position - 1).getId();
                }
                binding.familyTreeView.setRoot(currentRootPersonId);
                updateRootButtonDisplay();
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

                // Update styled Root Selection button display
                updateRootButtonDisplay();

                // Populate Root Selector Spinner for background compatibility
                List<String> names = new ArrayList<>();
                names.add(getString(R.string.tree_root_all_generations));
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

    private PersonEntity findPersonById(String personId) {
        if (membersList == null || personId == null) return null;
        for (PersonEntity p : membersList) {
            if (Objects.equals(personId, p.getId())) return p;
        }
        return null;
    }

    private int getChildCount(String personId) {
        if (membersList == null || personId == null) return 0;
        int count = 0;
        for (PersonEntity p : membersList) {
            if (Objects.equals(personId, p.getFatherId()) || Objects.equals(personId, p.getMotherId())) {
                count++;
            }
        }
        return count;
    }

    private int getDescendantCount(String personId) {
        if (membersList == null || personId == null) return 0;
        Map<String, PersonEntity> personMap = new HashMap<>();
        for (PersonEntity p : membersList) {
            personMap.put(p.getId(), p);
        }
        Set<String> collected = new HashSet<>();
        collectDescendantsHelper(personId, personMap, membersList, collected);
        return collected.size();
    }

    private void collectDescendantsHelper(String personId, Map<String, PersonEntity> map, List<PersonEntity> list, Set<String> collected) {
        if (collected.contains(personId)) return;
        collected.add(personId);
        PersonEntity person = map.get(personId);
        if (person == null) return;
        String spouseId = person.getSpouseId();
        if (spouseId != null && map.get(spouseId) != null) {
            collected.add(spouseId);
        }
        for (PersonEntity p : list) {
            if (Objects.equals(personId, p.getFatherId()) || Objects.equals(personId, p.getMotherId())
                    || (person.getSpouseId() != null && (Objects.equals(person.getSpouseId(), p.getFatherId()) || Objects.equals(person.getSpouseId(), p.getMotherId())))) {
                collectDescendantsHelper(p.getId(), map, list, collected);
            }
        }
    }

    private void updateRootButtonDisplay() {
        if (binding == null) return;

        if (currentRootPersonId == null) {
            binding.tvSelectedRootName.setText(R.string.tree_root_all_generations);
            int total = membersList != null ? membersList.size() : 0;
            binding.tvSelectedRootDetail.setText(getString(R.string.tree_root_all_summary, total));
            binding.ivSelectedRootIcon.setVisibility(View.VISIBLE);
            binding.tvSelectedRootAvatar.setVisibility(View.GONE);
        } else {
            PersonEntity p = findPersonById(currentRootPersonId);
            if (p != null) {
                binding.tvSelectedRootName.setText(p.getFullName());
                String genStr = (p.getGeneration() == 0)
                        ? getString(R.string.tree_root_gen_zero_badge)
                        : getString(R.string.tree_root_gen_format, p.getGeneration());
                int children = getChildCount(p.getId());
                int descendants = getDescendantCount(p.getId());
                binding.tvSelectedRootDetail.setText(getString(R.string.tree_root_descendant_summary, genStr, children, descendants));
                binding.ivSelectedRootIcon.setVisibility(View.GONE);
                binding.tvSelectedRootAvatar.setVisibility(View.VISIBLE);
                binding.tvSelectedRootAvatar.setText(p.getInitials());
            } else {
                currentRootPersonId = null;
                updateRootButtonDisplay();
            }
        }
    }

    private void showRootSelectionDialog() {
        if (!isAdded() || getContext() == null || membersList == null || membersList.isEmpty()) {
            Toast.makeText(requireContext(), "No family members available to select", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_root_ancestor, null);
        RecyclerView rv = dialogView.findViewById(R.id.rvRootAncestors);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Sort members by generation ascending, then by birthdate / name
        List<PersonEntity> sortedPeople = new ArrayList<>(membersList);
        sortedPeople.sort((a, b) -> {
            if (a.getGeneration() != b.getGeneration()) {
                return Integer.compare(a.getGeneration(), b.getGeneration());
            }
            if (a.getBirthDate() != null && b.getBirthDate() != null) {
                return a.getBirthDate().compareTo(b.getBirthDate());
            }
            return a.getFullName().compareToIgnoreCase(b.getFullName());
        });

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.tree_root_dialog_title)
                .setIcon(R.drawable.ic_nature_tree)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .create();

        RootAncestorAdapter adapter = new RootAncestorAdapter(sortedPeople, currentRootPersonId, selectedId -> {
            currentRootPersonId = selectedId;
            binding.familyTreeView.setRoot(currentRootPersonId);
            updateRootButtonDisplay();

            // Keep hidden spinner in sync
            if (selectedId == null) {
                binding.spinnerTreeRoot.setSelection(0);
            } else {
                int i = 0;
                while (i < membersList.size()) {
                    if (Objects.equals(membersList.get(i).getId(), selectedId)) {
                        binding.spinnerTreeRoot.setSelection(i + 1);
                        break;
                    }
                    i++;
                }
            }
            dialog.dismiss();
        });

        rv.setAdapter(adapter);
        dialog.show();
    }

    private class RootAncestorAdapter extends RecyclerView.Adapter<RootAncestorAdapter.ViewHolder> {
        private final List<PersonEntity> people;
        private final String selectedId;
        private final OnRootSelectedListener listener;

        RootAncestorAdapter(List<PersonEntity> people, String selectedId, OnRootSelectedListener listener) {
            this.people = people;
            this.selectedId = selectedId;
            this.listener = listener;
        }

        @Override
        public int getItemCount() {
            return 1 + people.size(); // Position 0 is "All Generations"
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_root_ancestor, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            float density = getResources().getDisplayMetrics().density;
            if (position == 0) {
                // All Generations Option
                holder.avatarView.setVisibility(View.GONE);
                holder.ivAllGenIcon.setVisibility(View.VISIBLE);
                holder.tvName.setText(R.string.tree_root_all_generations);
                holder.tvGenBadge.setText(R.string.tree_root_all_badge);
                int total = membersList != null ? membersList.size() : 0;
                holder.tvDetails.setText(getString(R.string.tree_root_all_summary, total));

                boolean isSelected = (selectedId == null);
                holder.ivSelectedCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                holder.card.setStrokeColor(ContextCompat.getColor(requireContext(),
                        isSelected ? R.color.primary : R.color.outline_variant));
                holder.card.setStrokeWidth(isSelected ? (int) (1.8f * density) : (int) density);

                holder.card.setOnClickListener(v -> listener.onSelected(null));
            } else {
                PersonEntity p = people.get(position - 1);
                holder.ivAllGenIcon.setVisibility(View.GONE);
                holder.avatarView.setVisibility(View.VISIBLE);
                holder.avatarView.setInitials(p.getInitials());
                holder.avatarView.setGender(p.getGender());
                holder.avatarView.setDeceased(p.isDeceased());
                if (p.getPhotoPath() != null && !p.getPhotoPath().isEmpty()) {
                    holder.avatarView.setPhotoPath(p.getPhotoPath());
                }

                String name = p.getFullName();
                if (p.getNickName() != null && !p.getNickName().isEmpty()) {
                    name += " (" + p.getNickName() + ")";
                }
                holder.tvName.setText(name);

                String genStr = (p.getGeneration() == 0)
                        ? getString(R.string.tree_root_gen_zero_badge)
                        : getString(R.string.tree_root_gen_format, p.getGeneration());
                holder.tvGenBadge.setText(genStr);

                int children = getChildCount(p.getId());
                int descendants = getDescendantCount(p.getId());
                String details = getString(R.string.tree_root_descendant_summary, genStr, children, descendants);
                String lifespan = p.getLifeSpan();
                if (lifespan != null && !lifespan.isEmpty()) {
                    details += " • " + lifespan;
                }
                holder.tvDetails.setText(details);

                boolean isSelected = Objects.equals(p.getId(), selectedId);
                holder.ivSelectedCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                holder.card.setStrokeColor(ContextCompat.getColor(requireContext(),
                        isSelected ? R.color.primary : R.color.outline_variant));
                holder.card.setStrokeWidth(isSelected ? (int) (1.8f * density) : (int) density);

                holder.card.setOnClickListener(v -> listener.onSelected(p.getId()));
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final com.google.android.material.card.MaterialCardView card;
            final AvatarView avatarView;
            final ImageView ivAllGenIcon;
            final TextView tvName;
            final TextView tvGenBadge;
            final TextView tvDetails;
            final ImageView ivSelectedCheck;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardRootItem);
                avatarView = itemView.findViewById(R.id.avatarView);
                ivAllGenIcon = itemView.findViewById(R.id.ivAllGenIcon);
                tvName = itemView.findViewById(R.id.tvRootItemName);
                tvGenBadge = itemView.findViewById(R.id.tvGenBadge);
                tvDetails = itemView.findViewById(R.id.tvRootItemDetails);
                ivSelectedCheck = itemView.findViewById(R.id.ivSelectedCheck);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
