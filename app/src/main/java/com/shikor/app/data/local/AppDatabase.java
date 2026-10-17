package com.shikor.app.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.shikor.app.data.local.converters.StringListConverter;
import com.shikor.app.data.local.daos.FamilyDao;
import com.shikor.app.data.local.daos.HijriDao;
import com.shikor.app.data.local.daos.PersonDao;
import com.shikor.app.data.local.daos.PhotoDao;
import com.shikor.app.data.local.daos.StoryDao;
import com.shikor.app.data.local.entities.FamilyEntity;
import com.shikor.app.data.local.entities.HijriCacheEntity;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;

/**
 * AppDatabase - Singleton Room database for local persistence in Shikor.
 *
 * CONCEPT: Room Database setup, TypeConverters, Migration example, and single source of truth.
 */
@Database(
    entities = {
        FamilyEntity.class,
        PersonEntity.class,
        StoryEntity.class,
        PhotoEntity.class,
        HijriCacheEntity.class
    },
    version = 2,
    exportSchema = false
)
@TypeConverters({StringListConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "shikor_database.db";
    private static volatile AppDatabase sInstance;

    public abstract PersonDao personDao();
    public abstract StoryDao storyDao();
    public abstract PhotoDao photoDao();
    public abstract FamilyDao familyDao();
    public abstract HijriDao hijriDao();

    /**
     * CONCEPT: Demonstrable Room Migration from Version 1 to Version 2.
     * Alters 'people' table to safely append the phoneNumber column.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE people ADD COLUMN phoneNumber TEXT DEFAULT ''");
        }
    };

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return sInstance;
    }
}
