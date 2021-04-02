package com.thugdroid.libs.collagegrid.model;

import android.net.Uri;

public class MyImage {
    Uri uri;
    String mimeType;
    String extension;
    Uri webUri;
    String filename;
    Long size;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Uri getWebUri() {
        return webUri;
    }

    public void setWebUri(Uri webUri) {
        this.webUri = webUri;
    }
}
