package com.application.baselibrary.core.media.audio.model;

import java.util.Objects;

public class AudioInfo {
    public final String name;
    public final String pathOrUri;
    public final long size;
    public final long dateModified;
    public long uploadTime;

    public AudioInfo(String name, String pathOrUri, long size, long dateModified) {
        this.name = name;
        this.pathOrUri = pathOrUri;
        this.size = size;
        this.dateModified = dateModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioInfo)) return false;
        AudioInfo that = (AudioInfo) o;
        return Objects.equals(pathOrUri, that.pathOrUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathOrUri);
    }
}
