package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GroupTemplatesIdsEntity {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String searchStr;
    private String templateId;

    public GroupTemplatesIdsEntity(String searchStr, String templateId) {
        this.searchStr = searchStr;
        this.templateId = templateId;
    }

    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
