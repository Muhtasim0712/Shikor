package com.shikor.app.ui.timeline;

import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;

import java.util.Calendar;

/**
 * TimelineItem - Union model representing a Year Header, Photo Card, or Memory/Story Card in the chronological timeline.
 */
public class TimelineItem {

    public static final int TYPE_YEAR_HEADER = 0;
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_STORY = 2;

    private final int type;
    private final int year;
    private final int count;
    private final PhotoEntity photo;
    private final StoryEntity story;

    public static TimelineItem header(int year, int count) {
        return new TimelineItem(TYPE_YEAR_HEADER, year, count, null, null);
    }

    public static TimelineItem photo(PhotoEntity photo) {
        return new TimelineItem(TYPE_PHOTO, photo != null ? photo.getYear() : 0, 0, photo, null);
    }

    public static TimelineItem story(StoryEntity story) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (story != null && story.getCreatedAt() > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(story.getCreatedAt());
            year = cal.get(Calendar.YEAR);
        }
        return new TimelineItem(TYPE_STORY, year, 0, null, story);
    }

    private TimelineItem(int type, int year, int count, PhotoEntity photo, StoryEntity story) {
        this.type = type;
        this.year = year;
        this.count = count;
        this.photo = photo;
        this.story = story;
    }

    public int getType() {
        return type;
    }

    public int getYear() {
        return year;
    }

    public int getCount() {
        return count;
    }

    public PhotoEntity getPhoto() {
        return photo;
    }

    public StoryEntity getStory() {
        return story;
    }
}
