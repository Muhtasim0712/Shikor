package com.shikor.app.util;

import com.shikor.app.data.local.entities.PersonEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * HealthChecker - Evaluates family tree integrity rules (death before birth, underage parent,
 * ancestor cycles, non-mutual spouses, duplicates, disconnected components, future dates, missing info).
 *
 * CONCEPT: Read-only graph analysis, cycle detection, date validation, and data integrity verification.
 */
public class HealthChecker {

    public enum Severity {
        HIGH,    // Critical issues (cycles, invalid dates, future dates)
        MEDIUM,  // Integrity warnings (non-mutual spouse, duplicates, disconnected)
        LOW      // Informational (missing photo or birth date)
    }

    public static class HealthIssue {
        public final String personId;
        public final String personName;
        public final String issueTitle;
        public final String issueDescription;
        public final Severity severity;

        public HealthIssue(String personId, String personName, String issueTitle, String issueDescription, Severity severity) {
            this.personId = personId;
            this.personName = personName;
            this.issueTitle = issueTitle;
            this.issueDescription = issueDescription;
            this.severity = severity;
        }
    }

    public static class HealthSummary {
        public int highCount;
        public int mediumCount;
        public int lowCount;
        public int totalIssues;
    }

    public static List<HealthIssue> checkTreeHealth(List<PersonEntity> people) {
        if (people == null || people.isEmpty()) {
            return Collections.emptyList();
        }

        List<HealthIssue> issues = new ArrayList<>();
        Map<String, PersonEntity> personMap = new HashMap<>();
        for (PersonEntity p : people) {
            personMap.put(p.getId(), p);
        }

        long now = System.currentTimeMillis();

        // 1. Date & Individual Validation
        for (PersonEntity p : people) {
            long birthMs = parseDateToMillis(p.getBirthDate());
            long deathMs = parseDateToMillis(p.getDeathDate());
            long marriageMs = parseDateToMillis(p.getMarriageDate());

            // Rule 1: Death before birth
            if (birthMs > 0 && deathMs > 0 && deathMs < birthMs) {
                issues.add(new HealthIssue(
                        p.getId(), p.getFullName(),
                        "Death before birth",
                        p.getFullName() + "'s recorded death date is earlier than their birth date.",
                        Severity.HIGH
                ));
            }

            // Rule 7: Future dates
            if (birthMs > now) {
                issues.add(new HealthIssue(
                        p.getId(), p.getFullName(),
                        "Future birth date",
                        p.getFullName() + "'s birth date is set in the future.",
                        Severity.HIGH
                ));
            }
            if (deathMs > now) {
                issues.add(new HealthIssue(
                        p.getId(), p.getFullName(),
                        "Future death date",
                        p.getFullName() + "'s death date is set in the future.",
                        Severity.HIGH
                ));
            }
            if (marriageMs > now) {
                issues.add(new HealthIssue(
                        p.getId(), p.getFullName(),
                        "Future marriage date",
                        p.getFullName() + "'s marriage date is set in the future.",
                        Severity.HIGH
                ));
            }

            // Rule 2: Child born before or <12 yrs after parent
            if (birthMs > 0) {
                if (p.getFatherId() != null && personMap.containsKey(p.getFatherId())) {
                    PersonEntity father = personMap.get(p.getFatherId());
                    checkParentChildAge(p, father, "Father", birthMs, issues);
                }
                if (p.getMotherId() != null && personMap.containsKey(p.getMotherId())) {
                    PersonEntity mother = personMap.get(p.getMotherId());
                    checkParentChildAge(p, mother, "Mother", birthMs, issues);
                }
            }

            // Rule 4: Non-mutual spouse link
            if (p.getSpouseId() != null && !p.getSpouseId().isEmpty()) {
                PersonEntity spouse = personMap.get(p.getSpouseId());
                if (spouse == null || !Objects.equals(spouse.getSpouseId(), p.getId())) {
                    issues.add(new HealthIssue(
                            p.getId(), p.getFullName(),
                            "Unmatched spouse link",
                            p.getFullName() + " lists a spouse who does not link back to them.",
                            Severity.MEDIUM
                    ));
                }
            }

            // Rule 8: Missing photo or birth date (Low severity)
            if (p.getBirthDate() == null || p.getBirthDate().trim().isEmpty()) {
                issues.add(new HealthIssue(
                        p.getId(), p.getFullName(),
                        "Missing birth date",
                        p.getFullName() + " has no birth date recorded.",
                        Severity.LOW
                ));
            }
            if (p.getPhotoPath() == null || p.getPhotoPath().trim().isEmpty()) {
                issues.add(new HealthIssue(
                        p.getId(), p.getFullName(),
                        "Missing profile photo",
                        p.getFullName() + " has no profile photo uploaded.",
                        Severity.LOW
                ));
            }
        }

        // Rule 3: Ancestor cycle detection (DFS)
        for (PersonEntity p : people) {
            Set<String> visited = new HashSet<>();
            if (hasAncestorCycle(p.getId(), personMap, visited)) {
                issues.add(new HealthIssue(
                        p.getId(), p.getFullName(),
                        "Ancestor cycle detected",
                        p.getFullName() + " is listed as their own ancestor in parent links.",
                        Severity.HIGH
                ));
            }
        }

        // Rule 5: Likely duplicates (Same full name & birth date)
        Map<String, List<PersonEntity>> duplicateMap = new HashMap<>();
        for (PersonEntity p : people) {
            if (p.getBirthDate() != null && !p.getBirthDate().trim().isEmpty()) {
                String key = p.getFullName().trim().toLowerCase() + "_" + p.getBirthDate().trim();
                duplicateMap.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
            }
        }
        for (List<PersonEntity> dups : duplicateMap.values()) {
            if (dups.size() > 1) {
                for (PersonEntity dup : dups) {
                    issues.add(new HealthIssue(
                            dup.getId(), dup.getFullName(),
                            "Likely duplicate relative",
                            dup.getFullName() + " shares an identical name and birth date with another profile.",
                            Severity.MEDIUM
                    ));
                }
            }
        }

        // Rule 6: Disconnected tree components
        if (people.size() > 2) {
            Set<String> mainConnectedSet = findMainConnectedComponent(people, personMap);
            for (PersonEntity p : people) {
                if (!mainConnectedSet.contains(p.getId())) {
                    issues.add(new HealthIssue(
                            p.getId(), p.getFullName(),
                            "Disconnected relative",
                            p.getFullName() + " is not connected to the main family tree.",
                            Severity.MEDIUM
                    ));
                }
            }
        }

        return issues;
    }

    private static void checkParentChildAge(PersonEntity child, PersonEntity parent, String parentRole, long childBirthMs, List<HealthIssue> issues) {
        if (parent == null) return;
        long parentBirthMs = parseDateToMillis(parent.getBirthDate());
        if (parentBirthMs <= 0) return;

        if (childBirthMs < parentBirthMs) {
            issues.add(new HealthIssue(
                    child.getId(), child.getFullName(),
                    "Child born before " + parentRole.toLowerCase(),
                    child.getFullName() + " is recorded as born before their " + parentRole.toLowerCase() + " (" + parent.getFullName() + ").",
                    Severity.HIGH
            ));
        } else {
            long diffYears = (childBirthMs - parentBirthMs) / (1000L * 60 * 60 * 24 * 365);
            if (diffYears < 12) {
                issues.add(new HealthIssue(
                        child.getId(), child.getFullName(),
                        "Parent under 12 at child's birth",
                        parent.getFullName() + " was under 12 years old when " + child.getFullName() + " was born.",
                        Severity.HIGH
                ));
            }
        }
    }

    private static boolean hasAncestorCycle(String personId, Map<String, PersonEntity> map, Set<String> visited) {
        if (personId == null) return false;
        if (visited.contains(personId)) return true; // Cycle detected!

        visited.add(personId);
        PersonEntity person = map.get(personId);
        if (person == null) return false;

        if (person.getFatherId() != null && hasAncestorCycle(person.getFatherId(), map, new HashSet<>(visited))) {
            return true;
        }
        if (person.getMotherId() != null && hasAncestorCycle(person.getMotherId(), map, new HashSet<>(visited))) {
            return true;
        }
        return false;
    }

    private static Set<String> findMainConnectedComponent(List<PersonEntity> people, Map<String, PersonEntity> personMap) {
        if (people.isEmpty()) return Collections.emptySet();

        // Build adjacency graph for all relations
        Map<String, Set<String>> graph = new HashMap<>();
        for (PersonEntity p : people) {
            graph.computeIfAbsent(p.getId(), k -> new HashSet<>());
            if (p.getFatherId() != null && personMap.containsKey(p.getFatherId())) {
                graph.get(p.getId()).add(p.getFatherId());
                graph.computeIfAbsent(p.getFatherId(), k -> new HashSet<>()).add(p.getId());
            }
            if (p.getMotherId() != null && personMap.containsKey(p.getMotherId())) {
                graph.get(p.getId()).add(p.getMotherId());
                graph.computeIfAbsent(p.getMotherId(), k -> new HashSet<>()).add(p.getId());
            }
            if (p.getSpouseId() != null && personMap.containsKey(p.getSpouseId())) {
                graph.get(p.getId()).add(p.getSpouseId());
                graph.computeIfAbsent(p.getSpouseId(), k -> new HashSet<>()).add(p.getId());
            }
        }

        // BFS from first root/person to find largest connected component
        String startId = people.get(0).getId();
        Set<String> visited = new HashSet<>();
        List<String> queue = new ArrayList<>();
        queue.add(startId);
        visited.add(startId);

        int head = 0;
        while (head < queue.size()) {
            String curr = queue.get(head++);
            Set<String> neighbors = graph.get(curr);
            if (neighbors != null) {
                for (String neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return visited;
    }

    private static long parseDateToMillis(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return 0;
        try {
            String[] parts = dateStr.trim().split("[-/.]");
            if (parts.length >= 3) {
                int y, m, d;
                if (parts[0].length() == 4) {
                    y = Integer.parseInt(parts[0]);
                    m = Integer.parseInt(parts[1]) - 1;
                    d = Integer.parseInt(parts[2]);
                } else {
                    d = Integer.parseInt(parts[0]);
                    m = Integer.parseInt(parts[1]) - 1;
                    y = Integer.parseInt(parts[2]);
                }
                Calendar cal = Calendar.getInstance();
                cal.set(y, m, d, 0, 0, 0);
                return cal.getTimeInMillis();
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
