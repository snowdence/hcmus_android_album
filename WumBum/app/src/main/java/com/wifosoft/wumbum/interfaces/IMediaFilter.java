package com.wifosoft.wumbum.interfaces;

import com.wifosoft.wumbum.model.Media;

public interface IMediaFilter {
    boolean accept(Media media);
}
