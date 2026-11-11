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
import com.shikor.app.data.local.daos.MemoryDao;
import com.shikor.app.data.local.daos.MemoryPhotoDao;
import com.shikor.app.data.local.daos.PersonDao;
import com.shikor.app.data.local.daos.PhotoDao;
import com.shikor.app.data.local.daos.StoryDao;
import com.shikor.app.data.local.entities.FamilyEntity;
import com.shikor.app.data.local.entities.HijriCacheEntity;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.MemoryPhotoEntity;
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
        HijriCacheEntity.class,
        MemoryEntity.class,
        MemoryPhotoEntity.class
    },
    version = 3,
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
    public abstract MemoryDao memoryDao();
    public abstract MemoryPhotoDao memoryPhotoDao();

    /**
     * CONCEPT: Room Migration from Version 1 to Version 2.
     * Alters 'people' table to safely append the phoneNumber column.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE people ADD COLUMN phoneNumber TEXT DEFAULT ''");
        }
    };

    /**
     * CONCEPT: Room Migration from Version 2 to Version 3 for new Memories feature tables.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `memories` (`id` TEXT NOT NULL, `familyId` TEXT NOT NULL, `title` TEXT, `description` TEXT, `eventDate` INTEGER NOT NULL, `year` INTEGER NOT NULL, `taggedPersonIds` TEXT, `photoCount` INTEGER NOT NULL, `hasAudio` INTEGER NOT NULL, `audioDurationMs` INTEGER NOT NULL, `audioPath` TEXT, `uploaderUid` TEXT, `uploaderName` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` INTEGER NOT NULL, `failureReason` TEXT, `notified` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_memories_familyId` ON `memories` (`familyId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_memories_eventDate` ON `memories` (`eventDate`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_memories_year` ON `memories` (`year`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `memory_photos` (`id` TEXT NOT NULL, `memoryId` TEXT NOT NULL, `index` INTEGER NOT NULL, `localPath` TEXT, `remoteVersion` INTEGER NOT NULL, `downloadState` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`memoryId`) REFERENCES `memories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_memory_photos_memoryId` ON `memory_photos` (`memoryId`)");
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return sInstance;
    }
}
