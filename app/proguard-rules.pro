# Room Database Proguard Rules
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Firebase Models & App Data Classes
-keepclassmembers class * {
    @androidx.room.Entity *;
    @androidx.room.Dao *;
    @androidx.room.TypeConverter *;
}
-keep class com.shikor.app.data.local.entities.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
