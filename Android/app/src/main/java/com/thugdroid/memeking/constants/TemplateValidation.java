package com.thugdroid.memeking.constants;

import java.util.ArrayList;
import java.util.List;

public class TemplateValidation {
    public static boolean isValidSearchTag(String searchStr){
        if(searchStr==null){
            return false;
        }
        if(searchStr.trim().length()==0){
            return false;
        }
        String[] searchTags = searchStr.split(",");
        List<String> searchTagsResult = new ArrayList<>();
        for (int i = 0; i < searchTags.length; i++) {
            String currentSearchStr = searchTags[i];
            currentSearchStr=currentSearchStr.trim();
            if(currentSearchStr.length()>0){
                searchTagsResult.add(currentSearchStr);
            }
        }
        return searchTagsResult.size()>0;
    }
}
