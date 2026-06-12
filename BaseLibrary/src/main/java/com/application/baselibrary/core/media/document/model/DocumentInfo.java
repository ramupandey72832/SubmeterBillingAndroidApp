package com.application.baselibrary.core.media.document.model;

import java.util.Objects;

public class DocumentInfo {
    public final String name;
    public final String pathOrUri;   // filesystem path or content Uri string
    public final long size;          // bytes
    public final long dateModified;  // epoch millis (0 if unknown)
    public long uploadTime;          // epoch millis (0 if not uploaded)

    public DocumentInfo(String name, String pathOrUri, long size, long dateModified) {
        this.name = name;
        this.pathOrUri = pathOrUri;
        this.size = size;
        this.dateModified = dateModified;
        this.uploadTime = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentInfo)) return false;
        DocumentInfo that = (DocumentInfo) o;
        // equality based on path + size (safer than path alone)
        return Objects.equals(pathOrUri, that.pathOrUri) && size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathOrUri, size);
    }
}
