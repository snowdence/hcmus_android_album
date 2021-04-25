package com.wifosoft.wumbum.filter;


import com.wifosoft.wumbum.interfaces.IMediaFilter;
import com.wifosoft.wumbum.model.Media;

public class MediaFilter {
    public static IMediaFilter getFilter(FilterMode mode) {
        switch (mode) {
            case ALL: default:
                return media -> true;
            case GIF:
                return Media::isGif;
            case VIDEO:
                return Media::isVideo;
            case IMAGES: return Media::isImage;
        }
    }
}
