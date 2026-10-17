package com.shikor.app.ui.timeline;

import com.shikor.app.data.local.entities.PhotoEntity;

/**
 * TimelineItem - Union model representing either a Year Header or a Photo Card in the chronological timeline.
 */
public class TimelineItem {

    public static final int TYPE_YEAR_HEADER = 0;
    public static final int TYPE_PHOTO = 1;

    private final int type;
    private final int year;
    private final int photoCount;
    private final PhotoEntity photo;

    public static TimelineItem header(int year, int photoCount) {
        return new TimelineItem(TYPE_YEAR_HEADER, year, photoCount, null);
    }

    public static TimelineItem photo(PhotoEntity photo) {
        return new TimelineItem(TYPE_PHOTO, photo != null ? photo.getYear() : 0, 0, photo);
    }

    private TimelineItem(int type, int year, int photoCount, PhotoEntity photo) {
        this.type = type;
        this.year = year;
        this.photoCount = photoCount;
        this.photo = photo;
    }

    public int getType() {
        return type;
    }

    public int getYear() {
        return year;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public PhotoEntity getPhoto() {
        return photo;
    }
}
