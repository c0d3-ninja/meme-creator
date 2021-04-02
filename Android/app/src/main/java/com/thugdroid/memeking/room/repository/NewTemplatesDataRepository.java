package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.TemplateDao;
import com.thugdroid.memeking.room.entity.AllCategoryTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.FavTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.FeedTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.MyTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.List;

public class NewTemplatesDataRepository extends AndroidViewModel {
    TemplateDao templateDao;

    public NewTemplatesDataRepository(@NonNull Application application) {
        super(application);
        AppDatabase db= AppDatabase.getInstance(application);
        this.templateDao=db.getTemplateDao();
    }

    public void insertAllTemplateData(List<TemplateEntity> templateEntities){
        new InsertAllTemplateData(templateEntities).execute();
    }


    class InsertAllTemplateData extends AsyncTask {
        List<TemplateEntity> templateEntities;

        public InsertAllTemplateData(List<TemplateEntity> templateEntities) {
            this.templateEntities = templateEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertAllTemplateData(templateEntities);
            return null;
        }
    }
}
