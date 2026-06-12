package com.application.baselibrary.core.media.image.model;

import android.net.Uri;
import java.util.Objects;

public class ImageInfo {
    public final String uriString;
    public final String name;
    public long uploadTime;

    public ImageInfo(Uri uri, String name) {
        this.uriString = uri.toString();
        this.name = name;
    }

    public Uri getUri() {
        return Uri.parse(uriString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageInfo)) return false;
        ImageInfo that = (ImageInfo) o;
        return Objects.equals(uriString, that.uriString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uriString);
    }
}
