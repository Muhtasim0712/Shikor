package com.shikor.app.view;

import android.graphics.RectF;

import com.shikor.app.data.local.entities.PersonEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * TreeUnit - Represents an individual person or married couple node in the family tree hierarchy.
 * Holds layout boundaries, generation level, parent-child links, and dimensions.
 */
public class TreeUnit {
    public final PersonEntity primary;
    public final PersonEntity spouse;
    public TreeUnit parentUnit;
    public final List<TreeUnit> childrenUnits = new ArrayList<>();

    public int level = 0;
    public float subtreeWidth = 0;
    public final RectF primaryRect = new RectF();
    public final RectF spouseRect = new RectF();

    public TreeUnit(PersonEntity primary, PersonEntity spouse) {
        this.primary = primary;
        this.spouse = spouse;
    }
}
