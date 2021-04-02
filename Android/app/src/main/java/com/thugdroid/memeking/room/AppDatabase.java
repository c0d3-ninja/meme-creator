package com.thugdroid.memeking.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;

import com.thugdroid.memeking.room.dao.AppPrefsDao;
import com.thugdroid.memeking.room.dao.CategoryDao;
import com.thugdroid.memeking.room.dao.GTsLastApiCallTimeDao;
import com.thugdroid.memeking.room.dao.GroupTemplatesDao;
import com.thugdroid.memeking.room.dao.MemesDao;
import com.thugdroid.memeking.room.dao.RegionDao;
import com.thugdroid.memeking.room.dao.LoggedInUserDao;
import com.thugdroid.memeking.room.dao.SocialUsernameDao;
import com.thugdroid.memeking.room.dao.TemplateDao;
import com.thugdroid.memeking.room.dao.TemplatesGroupDataDao;
import com.thugdroid.memeking.room.entity.AllCategoryTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.FavTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.GTsLastApiCallTimeEntity;
import com.thugdroid.memeking.room.entity.GroupTemplatesIdsEntity;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.entity.MemesDataEntity;
import com.thugdroid.memeking.room.entity.MemesFeedIdsEntity;
import com.thugdroid.memeking.room.entity.MyMemesIdsEntity;
import com.thugdroid.memeking.room.entity.MyTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.RegionEntity;
import com.thugdroid.memeking.room.entity.SocialUsernameEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.entity.FeedTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.TemplatesGroupDataEntity;

@Database(entities = {LoggedInUserEntity.class, RegionEntity.class,
        CategoryEntity.class, AppPrefsEntity.class, TemplateEntity.class,
        FeedTemplateIdsEntity.class, MyTemplateIdsEntity.class, FavTemplateIdsEntity.class,
        AllCategoryTemplateIdsEntity.class, MemesFeedIdsEntity.class, MyMemesIdsEntity.class,
        MemesDataEntity.class, TemplatesGroupDataEntity.class,
        GroupTemplatesIdsEntity.class, GTsLastApiCallTimeEntity.class, SocialUsernameEntity.class},version = 4,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
public abstract LoggedInUserDao getUserDao();
public abstract RegionDao getRegionDao();
public abstract CategoryDao getCategoryDao();
public abstract AppPrefsDao getAppPrefsDao();
public abstract TemplateDao getTemplateDao();
public abstract MemesDao getMemesDao();
public abstract TemplatesGroupDataDao getTemplatesGroupDataDao();
public abstract GroupTemplatesDao getGroupTemplatesDao();
public abstract GTsLastApiCallTimeDao getGTsLastApiCallTimeDao();
public abstract SocialUsernameDao getSocialUsernameDao();

public static volatile AppDatabase INSTANCE;

public static AppDatabase getInstance(final Context context){
    if(INSTANCE==null){
        synchronized (AppDatabase.class){
            if(INSTANCE==null){
                INSTANCE= Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,"app_db")
                        .addMigrations(Migrations.getMigration1_2())
                        .addMigrations(Migrations.getMigration2_3())
                        .addMigrations(Migrations.getMigration3_4())
                        .build();
            }
        }
    }
    return INSTANCE;
}

public interface DbOperationCallbackListener{
    void onSuccess();
}
}
