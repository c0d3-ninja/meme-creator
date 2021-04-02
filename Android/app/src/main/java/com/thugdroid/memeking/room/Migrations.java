package com.thugdroid.memeking.room;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.thugdroid.memeking.room.entity.AppPrefsEntity;

public class Migrations {
    public static final Migration getMigration1_2(){
       final Migration MIGRATION_1_2=new Migration(1,2) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `MemesDataEntity` (`id` TEXT NOT NULL, `imageUrl` TEXT, `regionId` TEXT, `downloads` INTEGER NOT NULL, `shares` INTEGER NOT NULL, `createdBy` TEXT, `createdTime` INTEGER, PRIMARY KEY(`id`))");
                database.execSQL("CREATE TABLE IF NOT EXISTS `MemesFeedIdsEntity` (`id` TEXT NOT NULL, `createdTime` INTEGER, PRIMARY KEY(`id`))");
                database.execSQL("CREATE TABLE IF NOT EXISTS `MyMemesIdsEntity` (`id` TEXT NOT NULL, `createdTime` INTEGER, PRIMARY KEY(`id`))");
            }
        };
        return MIGRATION_1_2;
    }
    public static final Migration getMigration2_3(){
        final Migration MIGRATION_2_3=new Migration(2,3) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `GroupTemplatesIdsEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `searchStr` TEXT, `templateId` TEXT)");
                database.execSQL("CREATE TABLE IF NOT EXISTS `GTsLastApiCallTimeEntity` (`id` TEXT NOT NULL, `time` INTEGER, PRIMARY KEY(`id`))");
                database.execSQL("DELETE FROM CategoryEntity");
                database.execSQL("CREATE TABLE IF NOT EXISTS `TemplatesGroupDataEntity` (`id` TEXT NOT NULL, `name` TEXT, `imageUrl` TEXT, `searchStr` TEXT, `regionId` TEXT, `createdTime` INTEGER, PRIMARY KEY(`id`))");
                database.execSQL("DELETE FROM CategoryEntity");
                database.delete("AppPrefsEntity",  "AppPrefsEntity.id=?", new String[]{AppPrefsEntity.SELECTED_NAVDRAWER_MENU});
                database.delete("AppPrefsEntity",  "AppPrefsEntity.id=?", new String[]{AppPrefsEntity.SELECTED_CATEGORY});
            }
        };
        return MIGRATION_2_3;
    }

    public static final Migration getMigration3_4(){
        final Migration MIGRATION_3_4 =new Migration(3,4) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `SocialUsernameEntity` (`userId` TEXT NOT NULL, `instaUsername` TEXT, PRIMARY KEY(`userId`))");
                //authorid
                database.execSQL("DELETE FROM TemplateEntity");
                database.execSQL("DELETE FROM FavTemplateIdsEntity");
                database.execSQL("DELETE FROM FeedTemplateIdsEntity");
                database.execSQL("DELETE FROM MyTemplateIdsEntity");
                database.execSQL("DELETE FROM MyMemesIdsEntity");
                database.execSQL("DELETE FROM MemesDataEntity");
                database.execSQL("DELETE FROM MemesFeedIdsEntity");
                database.execSQL("ALTER TABLE `MemesDataEntity` add `instaUsername` TEXT");
                database.execSQL("ALTER TABLE `TemplateEntity` add `authorId` TEXT");
            }
        };
        return MIGRATION_3_4;
    }


}
