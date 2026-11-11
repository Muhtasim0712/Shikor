package com.shikor.app.view;

import android.graphics.RectF;

import com.shikor.app.data.local.entities.PersonEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TreeLayoutEngine - Calculates non-overlapping hierarchical coordinates for family tree nodes.
 *
 * CONCEPT: Computes generation-based subtrees recursively, grouping married couples into single units
 * and centering children beneath/above parents without overlapping. Supports both Natural Tree
 * mode (roots at bottom, generations growing upward) and Classic Chart mode (top-down boxes).
 */
public class TreeLayoutEngine {

    public static class LayoutResult {
        public final List<TreeUnit> allUnits = new ArrayList<>();
        public final List<TreeUnit> rootUnits = new ArrayList<>();
        public final RectF bounds = new RectF();
        public int maxLevel = 0;
        public float trunkBaseX = 0;
        public float trunkTopY = 0;
        public float groundY = 0;
    }

    public static LayoutResult calculateLayout(
            List<PersonEntity> rawPeople,
            String rootPersonId,
            boolean isNaturalTreeMode,
            float nodeWidth,
            float nodeHeight,
            float coupleGap,
            float siblingGap,
            float levelGap,
            float density
    ) {
        LayoutResult result = new LayoutResult();
        if (rawPeople == null || rawPeople.isEmpty()) {
            return result;
        }

        Map<String, PersonEntity> personMap = new HashMap<>();
        for (PersonEntity p : rawPeople) {
            personMap.put(p.getId(), p);
        }

        // 1. Filter people if a rootPersonId is specified
        Set<String> activeIds = new HashSet<>();
        if (rootPersonId != null && personMap.containsKey(rootPersonId)) {
            collectDescendants(rootPersonId, personMap, rawPeople, activeIds);
        } else {
            for (PersonEntity p : rawPeople) {
                activeIds.add(p.getId());
            }
        }

        // 2. Group into TreeUnits (person + optional spouse)
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, TreeUnit> unitMap = new HashMap<>();

        for (PersonEntity p : rawPeople) {
            if (!activeIds.contains(p.getId())) continue;
            if (Boolean.TRUE.equals(visited.get(p.getId()))) continue;

            PersonEntity spouse = null;
            if (p.getSpouseId() != null && activeIds.contains(p.getSpouseId()) && personMap.containsKey(p.getSpouseId())) {
                spouse = personMap.get(p.getSpouseId());
                visited.put(spouse.getId(), true);
            }
            visited.put(p.getId(), true);

            PersonEntity primary = p;
            PersonEntity partner = spouse;
            if (partner != null) {
                if (rootPersonId != null && partner.getId().equals(rootPersonId)) {
                    primary = partner;
                    partner = p;
                } else if (rootPersonId != null && primary.getId().equals(rootPersonId)) {
                    // Already primary
                } else {
                    // If partner has parents in active tree but p does not, prioritize the blood descendant as primary!
                    boolean pHasParents = (p.getFatherId() != null && activeIds.contains(p.getFatherId()))
                            || (p.getMotherId() != null && activeIds.contains(p.getMotherId()));
                    boolean partnerHasParents = (partner.getFatherId() != null && activeIds.contains(partner.getFatherId()))
                            || (partner.getMotherId() != null && activeIds.contains(partner.getMotherId()));

                    if (!pHasParents && partnerHasParents) {
                        primary = partner;
                        partner = p;
                    }
                }
            }

            TreeUnit unit = new TreeUnit(primary, partner);
            result.allUnits.add(unit);
            unitMap.put(primary.getId(), unit);
            if (partner != null) {
                unitMap.put(partner.getId(), unit);
            }
        }

        // 3. Establish Parent-Child hierarchy between units for both primary and spouse
        for (TreeUnit unit : result.allUnits) {
            String fId = unit.primary.getFatherId();
            String mId = unit.primary.getMotherId();
            TreeUnit primaryParent = null;

            if (fId != null && unitMap.containsKey(fId)) {
                primaryParent = unitMap.get(fId);
            } else if (mId != null && unitMap.containsKey(mId)) {
                primaryParent = unitMap.get(mId);
            }

            if (primaryParent != null && primaryParent != unit) {
                unit.parentUnit = primaryParent;
                if (!primaryParent.childrenUnits.contains(unit)) {
                    primaryParent.childrenUnits.add(unit);
                }
            }

            // Check spouse's parents (maternal grandparents / in-laws)
            if (unit.spouse != null) {
                String sfId = unit.spouse.getFatherId();
                String smId = unit.spouse.getMotherId();
                TreeUnit spouseParent = null;

                if (sfId != null && unitMap.containsKey(sfId)) {
                    spouseParent = unitMap.get(sfId);
                } else if (smId != null && unitMap.containsKey(smId)) {
                    spouseParent = unitMap.get(smId);
                }

                if (spouseParent != null && spouseParent != unit) {
                    unit.spouseParentUnit = spouseParent;
                    if (unit.parentUnit == null && !spouseParent.childrenUnits.contains(unit)) {
                        spouseParent.childrenUnits.add(unit);
                    }
                }
            }
        }

        // 4. Identify root units (units with no parent in this subtree)
        Set<TreeUnit> childUnits = new HashSet<>();
        for (TreeUnit u : result.allUnits) {
            childUnits.addAll(u.childrenUnits);
        }
        for (TreeUnit u : result.allUnits) {
            if (!childUnits.contains(u)) {
                result.rootUnits.add(u);
            }
        }

        // 5. Assign generation levels and find max depth
        for (TreeUnit rootUnit : result.rootUnits) {
            assignLevels(rootUnit, 0, result);
        }

        // 6. Measure subtree widths recursively
        for (TreeUnit rootUnit : result.rootUnits) {
            measureSubtreeWidth(rootUnit, nodeWidth, coupleGap, siblingGap);
        }

        // 7. Position subtrees
        float startX = 60 * density;
        float paddingTop = 80 * density;

        for (TreeUnit rootUnit : result.rootUnits) {
            positionSubtree(rootUnit, startX, result.maxLevel, isNaturalTreeMode,
                    paddingTop, nodeWidth, nodeHeight, coupleGap, siblingGap, levelGap);
            startX += rootUnit.subtreeWidth + siblingGap;
        }

        // 8. Compute global bounds, ground level, and trunk anchor
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for (TreeUnit u : result.allUnits) {
            minX = Math.min(minX, u.primaryRect.left);
            maxX = Math.max(maxX, u.spouse != null ? u.spouseRect.right : u.primaryRect.right);
            minY = Math.min(minY, u.primaryRect.top);
            maxY = Math.max(maxY, u.spouse != null ? Math.max(u.primaryRect.bottom, u.spouseRect.bottom) : u.primaryRect.bottom);
        }

        if (minX == Float.MAX_VALUE) {
            minX = 0;
            maxX = nodeWidth;
            minY = 0;
            maxY = nodeHeight;
        }

        result.bounds.set(minX - (40 * density), minY - (40 * density), maxX + (40 * density), maxY + (140 * density));

        if (isNaturalTreeMode) {
            result.groundY = maxY + (60 * density);
            // Trunk connects to the center of generation 0 roots
            float sumGen0X = 0;
            float maxGen0Y = 0;
            int gen0Count = 0;
            for (TreeUnit r : result.rootUnits) {
                float mid = r.spouse != null
                        ? (r.primaryRect.left + r.spouseRect.right) / 2f
                        : r.primaryRect.centerX();
                sumGen0X += mid;
                maxGen0Y = Math.max(maxGen0Y, r.primaryRect.bottom);
                gen0Count++;
            }
            result.trunkBaseX = gen0Count > 0 ? (sumGen0X / gen0Count) : ((minX + maxX) / 2f);
            result.trunkTopY = maxGen0Y;
        } else {
            result.groundY = maxY + (40 * density);
            result.trunkBaseX = (minX + maxX) / 2f;
            result.trunkTopY = minY;
        }

        return result;
    }

    private static void assignLevels(TreeUnit unit, int level, LayoutResult result) {
        if (level > unit.level) {
            unit.level = level;
        }
        if (unit.level > result.maxLevel) {
            result.maxLevel = unit.level;
        }
        for (TreeUnit child : unit.childrenUnits) {
            assignLevels(child, unit.level + 1, result);
        }
    }

    private static float measureSubtreeWidth(TreeUnit unit, float nodeWidth, float coupleGap, float siblingGap) {
        float unitWidth = unit.spouse != null ? (nodeWidth * 2 + coupleGap) : nodeWidth;
        if (unit.childrenUnits.isEmpty()) {
            unit.subtreeWidth = unitWidth;
            return unit.subtreeWidth;
        }

        float childrenTotalWidth = 0;
        for (TreeUnit child : unit.childrenUnits) {
            childrenTotalWidth += measureSubtreeWidth(child, nodeWidth, coupleGap, siblingGap);
        }
        childrenTotalWidth += (unit.childrenUnits.size() - 1) * siblingGap;

        unit.subtreeWidth = Math.max(unitWidth, childrenTotalWidth);
        return unit.subtreeWidth;
    }

    private static void positionSubtree(
            TreeUnit unit,
            float leftX,
            int maxLevel,
            boolean isNaturalTreeMode,
            float paddingTop,
            float nodeWidth,
            float nodeHeight,
            float coupleGap,
            float siblingGap,
            float levelGap
    ) {
        float unitWidth = unit.spouse != null ? (nodeWidth * 2 + coupleGap) : nodeWidth;

        // In Natural Tree mode: Level 0 (oldest) is at the bottom, higher levels grow upward into the sky
        float y = isNaturalTreeMode
                ? paddingTop + ((maxLevel - unit.level) * (nodeHeight + levelGap))
                : paddingTop + (unit.level * (nodeHeight + levelGap));

        float unitCenterX = leftX + (unit.subtreeWidth / 2.0f);
        float primaryLeft = unitCenterX - (unitWidth / 2.0f);

        unit.primaryRect.set(primaryLeft, y, primaryLeft + nodeWidth, y + nodeHeight);
        if (unit.spouse != null) {
            float spouseLeft = primaryLeft + nodeWidth + coupleGap;
            unit.spouseRect.set(spouseLeft, y, spouseLeft + nodeWidth, y + nodeHeight);
        }

        float childX;
        if (!unit.childrenUnits.isEmpty()) {
            float childrenTotalWidth = 0;
            for (TreeUnit child : unit.childrenUnits) {
                childrenTotalWidth += child.subtreeWidth;
            }
            childrenTotalWidth += Math.max(0, unit.childrenUnits.size() - 1) * siblingGap;

            float parentCenterX = unit.spouse != null
                    ? (unit.primaryRect.left + unit.spouseRect.right) / 2f
                    : unit.primaryRect.centerX();
            childX = parentCenterX - (childrenTotalWidth / 2.0f);
        } else {
            childX = leftX;
        }

        for (TreeUnit child : unit.childrenUnits) {
            positionSubtree(child, childX, maxLevel, isNaturalTreeMode,
                    paddingTop, nodeWidth, nodeHeight, coupleGap, siblingGap, levelGap);
            childX += child.subtreeWidth + siblingGap;
        }
    }

    private static void collectDescendants(
            String rootPersonId,
            Map<String, PersonEntity> personMap,
            List<PersonEntity> allPeople,
            Set<String> collected
    ) {
        if (rootPersonId == null || collected.contains(rootPersonId)) return;

        List<String> queue = new ArrayList<>();
        queue.add(rootPersonId);
        collected.add(rootPersonId);

        // Include root person's spouse so the root couple is displayed together
        PersonEntity rootPerson = personMap.get(rootPersonId);
        if (rootPerson != null && rootPerson.getSpouseId() != null && personMap.containsKey(rootPerson.getSpouseId())) {
            collected.add(rootPerson.getSpouseId());
        }

        int head = 0;
        while (head < queue.size()) {
            String currId = queue.get(head++);
            PersonEntity p = personMap.get(currId);
            String currSpouseId = (p != null) ? p.getSpouseId() : null;

            for (PersonEntity other : allPeople) {
                if (other == null) continue;
                boolean isChild = currId.equals(other.getFatherId()) || currId.equals(other.getMotherId())
                        || (currSpouseId != null && (currSpouseId.equals(other.getFatherId()) || currSpouseId.equals(other.getMotherId())));

                if (isChild && !collected.contains(other.getId())) {
                    collected.add(other.getId());
                    queue.add(other.getId());

                    // Include child's spouse so married couples in the branch are kept intact
                    if (other.getSpouseId() != null && personMap.containsKey(other.getSpouseId())) {
                        collected.add(other.getSpouseId());
                    }
                }
            }
        }
    }
}
