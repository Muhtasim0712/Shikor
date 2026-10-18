package com.shikor.app.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * KinshipResolver - Parses kinship_terms.xml via XmlPullParser and computes
 * shortest relationship paths and exact Bengali & English kinship terms between any two relatives.
 *
 * CONCEPT: XmlPullParser for externalized localized strings, BFS shortest path graph traversal,
 * and Bengali cultural kinship logic.
 */
public class KinshipResolver {

    private static final String TAG = "KinshipResolver";
    private static Map<String, KinshipTerm> sTermsCache;

    public static class KinshipTerm {
        public final String key;
        public final String english;
        public final String bangla;

        public KinshipTerm(String key, String english, String bangla) {
            this.key = key;
            this.english = english;
            this.bangla = bangla;
        }
    }

    public static class PathStep {
        public final PersonEntity person;
        public final String relationFromPrevious; // e.g., "Father", "Mother", "Spouse", "Son", "Daughter"

        public PathStep(PersonEntity person, String relationFromPrevious) {
            this.person = person;
            this.relationFromPrevious = relationFromPrevious;
        }
    }

    public static class KinshipResult {
        public final String englishTerm;
        public final String banglaTerm;
        public final String description;
        public final List<PathStep> path;

        public KinshipResult(String englishTerm, String banglaTerm, String description, List<PathStep> path) {
            this.englishTerm = englishTerm;
            this.banglaTerm = banglaTerm;
            this.description = description;
            this.path = path;
        }
    }

    /**
     * Loads and parses kinship_terms.xml using Android's native XmlPullParser.
     */
    public static synchronized Map<String, KinshipTerm> loadKinshipTerms(@NonNull Context context) {
        if (sTermsCache != null) return sTermsCache;

        Map<String, KinshipTerm> terms = new HashMap<>();
        try {
            XmlPullParser parser = context.getResources().getXml(R.xml.kinship_terms);
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "term".equals(parser.getName())) {
                    String key = parser.getAttributeValue(null, "key");
                    String english = parser.getAttributeValue(null, "english");
                    String bangla = parser.getAttributeValue(null, "bangla");

                    if (key != null && english != null && bangla != null) {
                        terms.put(key, new KinshipTerm(key, english, bangla));
                    }
                }
                eventType = parser.next();
            }
            Log.d(TAG, "Successfully parsed " + terms.size() + " kinship terms from kinship_terms.xml");
        } catch (Exception e) {
            Log.e(TAG, "Error parsing kinship_terms.xml: " + e.getMessage(), e);
        }

        sTermsCache = terms;
        return sTermsCache;
    }

    private static class GraphEdge {
        final String targetId;
        final String relation; // "FATHER", "MOTHER", "CHILD", "SPOUSE"

        GraphEdge(String targetId, String relation) {
            this.targetId = targetId;
            this.relation = relation;
        }
    }

    /**
     * Calculates the relationship from personA to personB.
     */
    @NonNull
    public static KinshipResult resolveRelationship(@NonNull Context context,
                                                    @NonNull List<PersonEntity> allPeople,
                                                    @NonNull PersonEntity personA,
                                                    @NonNull PersonEntity personB) {
        Map<String, KinshipTerm> terms = loadKinshipTerms(context);

        if (personA.getId().equals(personB.getId())) {
            List<PathStep> path = Collections.singletonList(new PathStep(personA, "Self"));
            return new KinshipResult("Self", "নিজে", personA.getFullName() + " is the same person.", path);
        }

        // Build index of people
        Map<String, PersonEntity> personMap = new HashMap<>();
        for (PersonEntity p : allPeople) {
            personMap.put(p.getId(), p);
        }

        // Build bidirectional adjacency graph
        Map<String, List<GraphEdge>> adj = new HashMap<>();
        for (PersonEntity p : allPeople) {
            adj.putIfAbsent(p.getId(), new ArrayList<>());

            if (p.getFatherId() != null && personMap.containsKey(p.getFatherId())) {
                adj.get(p.getId()).add(new GraphEdge(p.getFatherId(), "FATHER"));
                adj.putIfAbsent(p.getFatherId(), new ArrayList<>());
                adj.get(p.getFatherId()).add(new GraphEdge(p.getId(), "CHILD"));
            }
            if (p.getMotherId() != null && personMap.containsKey(p.getMotherId())) {
                adj.get(p.getId()).add(new GraphEdge(p.getMotherId(), "MOTHER"));
                adj.putIfAbsent(p.getMotherId(), new ArrayList<>());
                adj.get(p.getMotherId()).add(new GraphEdge(p.getId(), "CHILD"));
            }
            if (p.getSpouseId() != null && personMap.containsKey(p.getSpouseId())) {
                adj.get(p.getId()).add(new GraphEdge(p.getSpouseId(), "SPOUSE"));
            }
        }

        // BFS to find shortest path from personA to personB
        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> prevNode = new HashMap<>();
        Map<String, String> edgeTaken = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(personA.getId());
        visited.add(personA.getId());

        boolean found = false;
        while (!queue.isEmpty()) {
            String curr = queue.poll();
            if (curr.equals(personB.getId())) {
                found = true;
                break;
            }

            List<GraphEdge> edges = adj.get(curr);
            if (edges != null) {
                for (GraphEdge edge : edges) {
                    if (!visited.contains(edge.targetId)) {
                        visited.add(edge.targetId);
                        prevNode.put(edge.targetId, curr);
                        edgeTaken.put(edge.targetId, edge.relation);
                        queue.add(edge.targetId);
                    }
                }
            }
        }

        if (!found) {
            return new KinshipResult("Unrelated / Distant", "সম্পর্ক পাওয়া যায়নি",
                    "No direct genealogical link found in the tree between " + personA.getFullName() + " and " + personB.getFullName() + ".",
                    Collections.emptyList());
        }

        // Reconstruct path
        List<String> nodePath = new ArrayList<>();
        List<String> relPath = new ArrayList<>();
        String curr = personB.getId();
        while (curr != null) {
            nodePath.add(curr);
            String prev = prevNode.get(curr);
            if (prev != null) {
                relPath.add(edgeTaken.get(curr));
            }
            curr = prev;
        }
        Collections.reverse(nodePath);
        Collections.reverse(relPath);

        List<PathStep> pathSteps = new ArrayList<>();
        pathSteps.add(new PathStep(personA, "Start"));
        for (int i = 1; i < nodePath.size(); i++) {
            PersonEntity nodePerson = personMap.get(nodePath.get(i));
            String rawRel = relPath.get(i - 1);
            String humanRel = formatRelationEdge(rawRel, nodePerson);
            pathSteps.add(new PathStep(nodePerson, humanRel));
        }

        // Resolve exact cultural Kinship Term
        return determineKinshipTerm(terms, personA, personB, relPath, personMap, pathSteps);
    }

    private static String formatRelationEdge(String edgeType, PersonEntity target) {
        boolean isFemale = target != null && "female".equalsIgnoreCase(target.getGender());
        switch (edgeType) {
            case "FATHER": return "Father";
            case "MOTHER": return "Mother";
            case "SPOUSE": return isFemale ? "Wife" : "Husband";
            case "CHILD": return isFemale ? "Daughter" : "Son";
            default: return "Relative";
        }
    }

    private static KinshipResult determineKinshipTerm(Map<String, KinshipTerm> terms,
                                                     PersonEntity a, PersonEntity b,
                                                     List<String> relPath,
                                                     Map<String, PersonEntity> personMap,
                                                     List<PathStep> pathSteps) {
        int length = relPath.size();
        boolean bIsFemale = "female".equalsIgnoreCase(b.getGender());
        String bName = b.getFullName();
        String aName = a.getFullName();

        // 1 Step Relationships
        if (length == 1) {
            String step = relPath.get(0);
            if ("FATHER".equals(step)) {
                return termResult(terms, "father", bName + " is " + aName + "'s Father.", pathSteps);
            } else if ("MOTHER".equals(step)) {
                return termResult(terms, "mother", bName + " is " + aName + "'s Mother.", pathSteps);
            } else if ("SPOUSE".equals(step)) {
                return termResult(terms, bIsFemale ? "wife" : "husband", bName + " is " + aName + "'s " + (bIsFemale ? "Wife" : "Husband") + ".", pathSteps);
            } else if ("CHILD".equals(step)) {
                return termResult(terms, bIsFemale ? "daughter" : "son", bName + " is " + aName + "'s " + (bIsFemale ? "Daughter" : "Son") + ".", pathSteps);
            }
        }

        // 2 Step Relationships
        if (length == 2) {
            String s1 = relPath.get(0);
            String s2 = relPath.get(1);

            // Sibling: PARENT -> CHILD
            if (("FATHER".equals(s1) || "MOTHER".equals(s1)) && "CHILD".equals(s2)) {
                boolean bOlder = isOlder(b, a);
                if (bIsFemale) {
                    String key = bOlder ? "elder_sister" : "younger_sister";
                    return termResult(terms, key, bName + " is " + aName + "'s " + (bOlder ? "Elder Sister" : "Younger Sister") + ".", pathSteps);
                } else {
                    String key = bOlder ? "elder_brother" : "younger_brother";
                    return termResult(terms, key, bName + " is " + aName + "'s " + (bOlder ? "Elder Brother" : "Younger Brother") + ".", pathSteps);
                }
            }

            // Grandparents: FATHER -> FATHER / MOTHER, MOTHER -> FATHER / MOTHER
            if ("FATHER".equals(s1) && "FATHER".equals(s2)) {
                return termResult(terms, "paternal_grandfather", bName + " is " + aName + "'s Paternal Grandfather (Father's Father).", pathSteps);
            }
            if ("FATHER".equals(s1) && "MOTHER".equals(s2)) {
                return termResult(terms, "paternal_grandmother", bName + " is " + aName + "'s Paternal Grandmother (Father's Mother).", pathSteps);
            }
            if ("MOTHER".equals(s1) && "FATHER".equals(s2)) {
                return termResult(terms, "maternal_grandfather", bName + " is " + aName + "'s Maternal Grandfather (Mother's Father).", pathSteps);
            }
            if ("MOTHER".equals(s1) && "MOTHER".equals(s2)) {
                return termResult(terms, "maternal_grandmother", bName + " is " + aName + "'s Maternal Grandmother (Mother's Mother).", pathSteps);
            }

            // Grandchildren: CHILD -> CHILD
            if ("CHILD".equals(s1) && "CHILD".equals(s2)) {
                return termResult(terms, bIsFemale ? "granddaughter" : "grandson", bName + " is " + aName + "'s Grandchild.", pathSteps);
            }

            // In-laws: SPOUSE -> FATHER / MOTHER
            if ("SPOUSE".equals(s1) && ("FATHER".equals(s2) || "MOTHER".equals(s2))) {
                String en = s2.equals("FATHER") ? "Father-in-law" : "Mother-in-law";
                String bn = s2.equals("FATHER") ? "শ্বশুর" : "শাশুড়ি";
                return new KinshipResult(en, bn, bName + " is " + aName + "'s " + en + ".", pathSteps);
            }

            // Daughter/Son-in-law: CHILD -> SPOUSE
            if ("CHILD".equals(s1) && "SPOUSE".equals(s2)) {
                String en = bIsFemale ? "Daughter-in-law" : "Son-in-law";
                String bn = bIsFemale ? "পুত্রবধূ (বউমা)" : "জামাতা (জামাই)";
                return new KinshipResult(en, bn, bName + " is " + aName + "'s " + en + ".", pathSteps);
            }
        }

        // 3 Step Relationships (Uncles, Aunts, and In-laws)
        if (length == 3) {
            String s1 = relPath.get(0);
            String s2 = relPath.get(1);
            String s3 = relPath.get(2);

            // Father's Sibling: FATHER -> (PARENT) -> CHILD
            if ("FATHER".equals(s1) && ("FATHER".equals(s2) || "MOTHER".equals(s2)) && "CHILD".equals(s3)) {
                if (bIsFemale) {
                    return termResult(terms, "father_sister", bName + " is " + aName + "'s Paternal Aunt (Fufu / ফুফু).", pathSteps);
                } else {
                    // Check if Uncle is older than Father
                    PersonEntity father = pathSteps.get(1).person;
                    boolean uncleOlder = isOlder(b, father);
                    String key = uncleOlder ? "father_elder_brother" : "father_younger_brother";
                    return termResult(terms, key, bName + " is " + aName + "'s Paternal Uncle (" + (uncleOlder ? "Jetha / জেঠা" : "Chacha / চাচা") + ").", pathSteps);
                }
            }

            // Mother's Sibling: MOTHER -> (PARENT) -> CHILD
            if ("MOTHER".equals(s1) && ("FATHER".equals(s2) || "MOTHER".equals(s2)) && "CHILD".equals(s3)) {
                if (bIsFemale) {
                    return termResult(terms, "mother_sister", bName + " is " + aName + "'s Maternal Aunt (Khala / খালা).", pathSteps);
                } else {
                    return termResult(terms, "mother_brother", bName + " is " + aName + "'s Maternal Uncle (Mama / মামা).", pathSteps);
                }
            }
        }

        // 4 Step Relationships (First Cousins)
        if (length == 4) {
            String s1 = relPath.get(0);
            String s2 = relPath.get(1);
            String s3 = relPath.get(2);
            String s4 = relPath.get(3);

            if ("CHILD".equals(s4) && ("FATHER".equals(s2) || "MOTHER".equals(s2)) && "CHILD".equals(s3)) {
                if ("FATHER".equals(s1)) {
                    PersonEntity uncleOrAunt = pathSteps.get(3).person;
                    boolean isAunt = "female".equalsIgnoreCase(uncleOrAunt.getGender());
                    String key = isAunt ? "cousin_paternal_aunt" : "cousin_paternal_uncle";
                    return termResult(terms, key, bName + " is " + aName + "'s First Cousin (" + (isAunt ? "Fufato" : "Chachato") + ").", pathSteps);
                } else if ("MOTHER".equals(s1)) {
                    PersonEntity uncleOrAunt = pathSteps.get(3).person;
                    boolean isAunt = "female".equalsIgnoreCase(uncleOrAunt.getGender());
                    String key = isAunt ? "cousin_maternal_aunt" : "cousin_maternal_uncle";
                    return termResult(terms, key, bName + " is " + aName + "'s First Cousin (" + (isAunt ? "Khalato" : "Mamato") + ").", pathSteps);
                }
            }
        }

        // Fallback for distant relationships:
        String degreeName = length + "th-degree relative";
        String banglaDegree = length + " ধাপের আত্মীয়";
        return new KinshipResult(degreeName, banglaDegree,
                bName + " is connected to " + aName + " via " + length + " steps in the family tree.", pathSteps);
    }

    private static boolean isOlder(PersonEntity p1, PersonEntity p2) {
        if (p1 == null || p2 == null) return false;
        String b1 = p1.getBirthDate();
        String b2 = p2.getBirthDate();
        if (b1 != null && b2 != null && !b1.isEmpty() && !b2.isEmpty()) {
            return b1.compareTo(b2) < 0; // Earlier birth date = older
        }
        return false;
    }

    private static KinshipResult termResult(Map<String, KinshipTerm> terms, String key, String desc, List<PathStep> path) {
        KinshipTerm term = terms != null ? terms.get(key) : null;
        if (term != null) {
            return new KinshipResult(term.english, term.bangla, desc, path);
        }
        return new KinshipResult(key, key, desc, path);
    }
}
