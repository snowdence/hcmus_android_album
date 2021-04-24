package com.wifosoft.wumbum.interfaces;

import com.wifosoft.wumbum.model.Album;
import com.wifosoft.wumbum.model.Media;

import java.util.ArrayList;

public interface IMediaClickListener {
    void onMediaClick(Album album, ArrayList<Media> media, int position);
}

