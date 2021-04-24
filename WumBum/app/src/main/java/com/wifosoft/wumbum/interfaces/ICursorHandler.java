package com.wifosoft.wumbum.interfaces;

import android.database.Cursor;

public interface ICursorHandler <T>{
    T handle(Cursor cur);
    static String[] getProjection(){
        return new String[0];
    }
}
