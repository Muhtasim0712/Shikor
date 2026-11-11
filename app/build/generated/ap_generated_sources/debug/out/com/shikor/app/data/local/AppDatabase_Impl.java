package com.shikor.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.shikor.app.data.local.daos.FamilyDao;
import com.shikor.app.data.local.daos.FamilyDao_Impl;
import com.shikor.app.data.local.daos.HijriDao;
import com.shikor.app.data.local.daos.HijriDao_Impl;
import com.shikor.app.data.local.daos.MemoryDao;
import com.shikor.app.data.local.daos.MemoryDao_Impl;
import com.shikor.app.data.local.daos.MemoryPhotoDao;
import com.shikor.app.data.local.daos.MemoryPhotoDao_Impl;
import com.shikor.app.data.local.daos.PersonDao;
import com.shikor.app.data.local.daos.PersonDao_Impl;
import com.shikor.app.data.local.daos.PhotoDao;
import com.shikor.app.data.local.daos.PhotoDao_Impl;
import com.shikor.app.data.local.daos.StoryDao;
import com.shikor.app.data.local.daos.StoryDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile PersonDao _personDao;

  private volatile StoryDao _storyDao;

  private volatile PhotoDao _photoDao;

  private volatile FamilyDao _familyDao;

  private volatile HijriDao _hijriDao;

  private volatile MemoryDao _memoryDao;

  private volatile MemoryPhotoDao _memoryPhotoDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `families` (`id` TEXT NOT NULL, `name` TEXT, `code` TEXT, `adminUid` TEXT, `updatedAt` INTEGER NOT NULL, `syncState` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `people` (`id` TEXT NOT NULL, `familyId` TEXT NOT NULL, `fullName` TEXT, `nickName` TEXT, `gender` TEXT, `birthDate` TEXT, `deathDate` TEXT, `isDeceased` INTEGER NOT NULL, `hometown` TEXT, `occupation` TEXT, `phoneNumber` TEXT, `biography` TEXT, `photoPath` TEXT, `fatherId` TEXT, `motherId` TEXT, `spouseId` TEXT, `marriageDate` TEXT, `createdByUid` TEXT, `generation` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_people_familyId` ON `people` (`familyId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_people_fatherId` ON `people` (`fatherId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_people_motherId` ON `people` (`motherId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_people_spouseId` ON `people` (`spouseId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stories` (`id` TEXT NOT NULL, `personId` TEXT NOT NULL, `familyId` TEXT NOT NULL, `title` TEXT, `body` TEXT, `audioPath` TEXT, `audioDurationMs` INTEGER NOT NULL, `photoPath` TEXT, `createdAt` INTEGER NOT NULL, `authorUid` TEXT, `updatedAt` INTEGER NOT NULL, `syncState` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`personId`) REFERENCES `people`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stories_personId` ON `stories` (`personId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stories_familyId` ON `stories` (`familyId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `photos` (`id` TEXT NOT NULL, `personId` TEXT NOT NULL, `familyId` TEXT NOT NULL, `path` TEXT, `caption` TEXT, `year` INTEGER NOT NULL, `taggedPersonIds` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`personId`) REFERENCES `people`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_photos_personId` ON `photos` (`personId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_photos_familyId` ON `photos` (`familyId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `hijri_cache` (`gregorianDate` TEXT NOT NULL, `hijriDay` TEXT, `hijriMonthEn` TEXT, `hijriMonthAr` TEXT, `hijriYear` TEXT, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`gregorianDate`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `memories` (`id` TEXT NOT NULL, `familyId` TEXT NOT NULL, `title` TEXT, `description` TEXT, `eventDate` INTEGER NOT NULL, `year` INTEGER NOT NULL, `taggedPersonIds` TEXT, `photoCount` INTEGER NOT NULL, `hasAudio` INTEGER NOT NULL, `audioDurationMs` INTEGER NOT NULL, `audioPath` TEXT, `uploaderUid` TEXT, `uploaderName` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` INTEGER NOT NULL, `failureReason` TEXT, `notified` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_memories_familyId` ON `memories` (`familyId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_memories_eventDate` ON `memories` (`eventDate`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_memories_year` ON `memories` (`year`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `memory_photos` (`id` TEXT NOT NULL, `memoryId` TEXT NOT NULL, `index` INTEGER NOT NULL, `localPath` TEXT, `remoteVersion` INTEGER NOT NULL, `downloadState` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`memoryId`) REFERENCES `memories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_memory_photos_memoryId` ON `memory_photos` (`memoryId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1aaf6c437625ef3f0bb4e8e24156becf')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `families`");
        db.execSQL("DROP TABLE IF EXISTS `people`");
        db.execSQL("DROP TABLE IF EXISTS `stories`");
        db.execSQL("DROP TABLE IF EXISTS `photos`");
        db.execSQL("DROP TABLE IF EXISTS `hijri_cache`");
        db.execSQL("DROP TABLE IF EXISTS `memories`");
        db.execSQL("DROP TABLE IF EXISTS `memory_photos`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsFamilies = new HashMap<String, TableInfo.Column>(6);
        _columnsFamilies.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilies.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilies.put("code", new TableInfo.Column("code", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilies.put("adminUid", new TableInfo.Column("adminUid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilies.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilies.put("syncState", new TableInfo.Column("syncState", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFamilies = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFamilies = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFamilies = new TableInfo("families", _columnsFamilies, _foreignKeysFamilies, _indicesFamilies);
        final TableInfo _existingFamilies = TableInfo.read(db, "families");
        if (!_infoFamilies.equals(_existingFamilies)) {
          return new RoomOpenHelper.ValidationResult(false, "families(com.shikor.app.data.local.entities.FamilyEntity).\n"
                  + " Expected:\n" + _infoFamilies + "\n"
                  + " Found:\n" + _existingFamilies);
        }
        final HashMap<String, TableInfo.Column> _columnsPeople = new HashMap<String, TableInfo.Column>(21);
        _columnsPeople.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("familyId", new TableInfo.Column("familyId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("fullName", new TableInfo.Column("fullName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("nickName", new TableInfo.Column("nickName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("gender", new TableInfo.Column("gender", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("birthDate", new TableInfo.Column("birthDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("deathDate", new TableInfo.Column("deathDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("isDeceased", new TableInfo.Column("isDeceased", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("hometown", new TableInfo.Column("hometown", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("occupation", new TableInfo.Column("occupation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("biography", new TableInfo.Column("biography", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("photoPath", new TableInfo.Column("photoPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("fatherId", new TableInfo.Column("fatherId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("motherId", new TableInfo.Column("motherId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("spouseId", new TableInfo.Column("spouseId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("marriageDate", new TableInfo.Column("marriageDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("createdByUid", new TableInfo.Column("createdByUid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("generation", new TableInfo.Column("generation", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("syncState", new TableInfo.Column("syncState", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPeople = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPeople = new HashSet<TableInfo.Index>(4);
        _indicesPeople.add(new TableInfo.Index("index_people_familyId", false, Arrays.asList("familyId"), Arrays.asList("ASC")));
        _indicesPeople.add(new TableInfo.Index("index_people_fatherId", false, Arrays.asList("fatherId"), Arrays.asList("ASC")));
        _indicesPeople.add(new TableInfo.Index("index_people_motherId", false, Arrays.asList("motherId"), Arrays.asList("ASC")));
        _indicesPeople.add(new TableInfo.Index("index_people_spouseId", false, Arrays.asList("spouseId"), Arrays.asList("ASC")));
        final TableInfo _infoPeople = new TableInfo("people", _columnsPeople, _foreignKeysPeople, _indicesPeople);
        final TableInfo _existingPeople = TableInfo.read(db, "people");
        if (!_infoPeople.equals(_existingPeople)) {
          return new RoomOpenHelper.ValidationResult(false, "people(com.shikor.app.data.local.entities.PersonEntity).\n"
                  + " Expected:\n" + _infoPeople + "\n"
                  + " Found:\n" + _existingPeople);
        }
        final HashMap<String, TableInfo.Column> _columnsStories = new HashMap<String, TableInfo.Column>(12);
        _columnsStories.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("personId", new TableInfo.Column("personId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("familyId", new TableInfo.Column("familyId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("body", new TableInfo.Column("body", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("audioPath", new TableInfo.Column("audioPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("audioDurationMs", new TableInfo.Column("audioDurationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("photoPath", new TableInfo.Column("photoPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("authorUid", new TableInfo.Column("authorUid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStories.put("syncState", new TableInfo.Column("syncState", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStories = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysStories.add(new TableInfo.ForeignKey("people", "CASCADE", "NO ACTION", Arrays.asList("personId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesStories = new HashSet<TableInfo.Index>(2);
        _indicesStories.add(new TableInfo.Index("index_stories_personId", false, Arrays.asList("personId"), Arrays.asList("ASC")));
        _indicesStories.add(new TableInfo.Index("index_stories_familyId", false, Arrays.asList("familyId"), Arrays.asList("ASC")));
        final TableInfo _infoStories = new TableInfo("stories", _columnsStories, _foreignKeysStories, _indicesStories);
        final TableInfo _existingStories = TableInfo.read(db, "stories");
        if (!_infoStories.equals(_existingStories)) {
          return new RoomOpenHelper.ValidationResult(false, "stories(com.shikor.app.data.local.entities.StoryEntity).\n"
                  + " Expected:\n" + _infoStories + "\n"
                  + " Found:\n" + _existingStories);
        }
        final HashMap<String, TableInfo.Column> _columnsPhotos = new HashMap<String, TableInfo.Column>(10);
        _columnsPhotos.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("personId", new TableInfo.Column("personId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("familyId", new TableInfo.Column("familyId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("path", new TableInfo.Column("path", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("caption", new TableInfo.Column("caption", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("taggedPersonIds", new TableInfo.Column("taggedPersonIds", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotos.put("syncState", new TableInfo.Column("syncState", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPhotos = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPhotos.add(new TableInfo.ForeignKey("people", "CASCADE", "NO ACTION", Arrays.asList("personId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesPhotos = new HashSet<TableInfo.Index>(2);
        _indicesPhotos.add(new TableInfo.Index("index_photos_personId", false, Arrays.asList("personId"), Arrays.asList("ASC")));
        _indicesPhotos.add(new TableInfo.Index("index_photos_familyId", false, Arrays.asList("familyId"), Arrays.asList("ASC")));
        final TableInfo _infoPhotos = new TableInfo("photos", _columnsPhotos, _foreignKeysPhotos, _indicesPhotos);
        final TableInfo _existingPhotos = TableInfo.read(db, "photos");
        if (!_infoPhotos.equals(_existingPhotos)) {
          return new RoomOpenHelper.ValidationResult(false, "photos(com.shikor.app.data.local.entities.PhotoEntity).\n"
                  + " Expected:\n" + _infoPhotos + "\n"
                  + " Found:\n" + _existingPhotos);
        }
        final HashMap<String, TableInfo.Column> _columnsHijriCache = new HashMap<String, TableInfo.Column>(6);
        _columnsHijriCache.put("gregorianDate", new TableInfo.Column("gregorianDate", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHijriCache.put("hijriDay", new TableInfo.Column("hijriDay", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHijriCache.put("hijriMonthEn", new TableInfo.Column("hijriMonthEn", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHijriCache.put("hijriMonthAr", new TableInfo.Column("hijriMonthAr", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHijriCache.put("hijriYear", new TableInfo.Column("hijriYear", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHijriCache.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHijriCache = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHijriCache = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHijriCache = new TableInfo("hijri_cache", _columnsHijriCache, _foreignKeysHijriCache, _indicesHijriCache);
        final TableInfo _existingHijriCache = TableInfo.read(db, "hijri_cache");
        if (!_infoHijriCache.equals(_existingHijriCache)) {
          return new RoomOpenHelper.ValidationResult(false, "hijri_cache(com.shikor.app.data.local.entities.HijriCacheEntity).\n"
                  + " Expected:\n" + _infoHijriCache + "\n"
                  + " Found:\n" + _existingHijriCache);
        }
        final HashMap<String, TableInfo.Column> _columnsMemories = new HashMap<String, TableInfo.Column>(18);
        _columnsMemories.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("familyId", new TableInfo.Column("familyId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("eventDate", new TableInfo.Column("eventDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("taggedPersonIds", new TableInfo.Column("taggedPersonIds", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("photoCount", new TableInfo.Column("photoCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("hasAudio", new TableInfo.Column("hasAudio", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("audioDurationMs", new TableInfo.Column("audioDurationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("audioPath", new TableInfo.Column("audioPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("uploaderUid", new TableInfo.Column("uploaderUid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("uploaderName", new TableInfo.Column("uploaderName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("syncState", new TableInfo.Column("syncState", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("failureReason", new TableInfo.Column("failureReason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemories.put("notified", new TableInfo.Column("notified", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMemories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMemories = new HashSet<TableInfo.Index>(3);
        _indicesMemories.add(new TableInfo.Index("index_memories_familyId", false, Arrays.asList("familyId"), Arrays.asList("ASC")));
        _indicesMemories.add(new TableInfo.Index("index_memories_eventDate", false, Arrays.asList("eventDate"), Arrays.asList("ASC")));
        _indicesMemories.add(new TableInfo.Index("index_memories_year", false, Arrays.asList("year"), Arrays.asList("ASC")));
        final TableInfo _infoMemories = new TableInfo("memories", _columnsMemories, _foreignKeysMemories, _indicesMemories);
        final TableInfo _existingMemories = TableInfo.read(db, "memories");
        if (!_infoMemories.equals(_existingMemories)) {
          return new RoomOpenHelper.ValidationResult(false, "memories(com.shikor.app.data.local.entities.MemoryEntity).\n"
                  + " Expected:\n" + _infoMemories + "\n"
                  + " Found:\n" + _existingMemories);
        }
        final HashMap<String, TableInfo.Column> _columnsMemoryPhotos = new HashMap<String, TableInfo.Column>(6);
        _columnsMemoryPhotos.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemoryPhotos.put("memoryId", new TableInfo.Column("memoryId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemoryPhotos.put("index", new TableInfo.Column("index", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemoryPhotos.put("localPath", new TableInfo.Column("localPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemoryPhotos.put("remoteVersion", new TableInfo.Column("remoteVersion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMemoryPhotos.put("downloadState", new TableInfo.Column("downloadState", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMemoryPhotos = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMemoryPhotos.add(new TableInfo.ForeignKey("memories", "CASCADE", "NO ACTION", Arrays.asList("memoryId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMemoryPhotos = new HashSet<TableInfo.Index>(1);
        _indicesMemoryPhotos.add(new TableInfo.Index("index_memory_photos_memoryId", false, Arrays.asList("memoryId"), Arrays.asList("ASC")));
        final TableInfo _infoMemoryPhotos = new TableInfo("memory_photos", _columnsMemoryPhotos, _foreignKeysMemoryPhotos, _indicesMemoryPhotos);
        final TableInfo _existingMemoryPhotos = TableInfo.read(db, "memory_photos");
        if (!_infoMemoryPhotos.equals(_existingMemoryPhotos)) {
          return new RoomOpenHelper.ValidationResult(false, "memory_photos(com.shikor.app.data.local.entities.MemoryPhotoEntity).\n"
                  + " Expected:\n" + _infoMemoryPhotos + "\n"
                  + " Found:\n" + _existingMemoryPhotos);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "1aaf6c437625ef3f0bb4e8e24156becf", "2fccaf168c72fc3ca3ebb3660a5f5300");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "families","people","stories","photos","hijri_cache","memories","memory_photos");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `families`");
      _db.execSQL("DELETE FROM `people`");
      _db.execSQL("DELETE FROM `stories`");
      _db.execSQL("DELETE FROM `photos`");
      _db.execSQL("DELETE FROM `hijri_cache`");
      _db.execSQL("DELETE FROM `memories`");
      _db.execSQL("DELETE FROM `memory_photos`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PersonDao.class, PersonDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StoryDao.class, StoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PhotoDao.class, PhotoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FamilyDao.class, FamilyDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HijriDao.class, HijriDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MemoryDao.class, MemoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MemoryPhotoDao.class, MemoryPhotoDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PersonDao personDao() {
    if (_personDao != null) {
      return _personDao;
    } else {
      synchronized(this) {
        if(_personDao == null) {
          _personDao = new PersonDao_Impl(this);
        }
        return _personDao;
      }
    }
  }

  @Override
  public StoryDao storyDao() {
    if (_storyDao != null) {
      return _storyDao;
    } else {
      synchronized(this) {
        if(_storyDao == null) {
          _storyDao = new StoryDao_Impl(this);
        }
        return _storyDao;
      }
    }
  }

  @Override
  public PhotoDao photoDao() {
    if (_photoDao != null) {
      return _photoDao;
    } else {
      synchronized(this) {
        if(_photoDao == null) {
          _photoDao = new PhotoDao_Impl(this);
        }
        return _photoDao;
      }
    }
  }

  @Override
  public FamilyDao familyDao() {
    if (_familyDao != null) {
      return _familyDao;
    } else {
      synchronized(this) {
        if(_familyDao == null) {
          _familyDao = new FamilyDao_Impl(this);
        }
        return _familyDao;
      }
    }
  }

  @Override
  public HijriDao hijriDao() {
    if (_hijriDao != null) {
      return _hijriDao;
    } else {
      synchronized(this) {
        if(_hijriDao == null) {
          _hijriDao = new HijriDao_Impl(this);
        }
        return _hijriDao;
      }
    }
  }

  @Override
  public MemoryDao memoryDao() {
    if (_memoryDao != null) {
      return _memoryDao;
    } else {
      synchronized(this) {
        if(_memoryDao == null) {
          _memoryDao = new MemoryDao_Impl(this);
        }
        return _memoryDao;
      }
    }
  }

  @Override
  public MemoryPhotoDao memoryPhotoDao() {
    if (_memoryPhotoDao != null) {
      return _memoryPhotoDao;
    } else {
      synchronized(this) {
        if(_memoryPhotoDao == null) {
          _memoryPhotoDao = new MemoryPhotoDao_Impl(this);
        }
        return _memoryPhotoDao;
      }
    }
  }
}
