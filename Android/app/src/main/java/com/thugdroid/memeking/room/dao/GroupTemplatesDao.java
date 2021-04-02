package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.List;

@Dao
public interface GroupTemplatesDao {
    @Query("select TemplateEntity.* from GroupTemplatesIdsEntity inner join TemplateEntity on GroupTemplatesIdsEntity.templateId=TemplateEntity.id  where GroupTemplatesIdsEntity.searchStr=:searchStr AND  TemplateEntity.regionId=:regionId order by TemplateEntity.createdTime desc")
    LiveData<List<TemplateEntity>> getAllGroupTemplatesAsLiveData(String searchStr,String regionId);

    @Query("Insert into GroupTemplatesIdsEntity(searchStr,templateId) select * from (SELECT :searchStr as searchStr,:templateId as templateId) as tmp where not exists(select * from GroupTemplatesIdsEntity where GroupTemplatesIdsEntity.searchStr==:searchStr AND GroupTemplatesIdsEntity.templateId==:templateId LIMIT 1)")
    void insert(String searchStr,String templateId);

    @Query("Delete FROM GroupTemplatesIdsEntity Where GroupTemplatesIdsEntity.searchStr=:searchStr AND GroupTemplatesIdsEntity.templateId=:templateId")
    void delete(String searchStr,String templateId);

    @Query("Delete FROM GroupTemplatesIdsEntity Where GroupTemplatesIdsEntity.searchStr=:searchStr ")
    void deleteAllBySearchStr(String searchStr);

    @Query("Delete From GroupTemplatesIdsEntity")
    void deleteAll();
}
