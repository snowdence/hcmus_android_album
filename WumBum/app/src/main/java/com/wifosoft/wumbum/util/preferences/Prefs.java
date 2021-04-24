package com.wifosoft.wumbum.util.preferences;

import android.content.Context;
import android.support.annotation.NonNull;

public class Prefs {
    private static SharedPrefs sharedPrefs;

    public static void init(@NonNull Context context){
        if(sharedPrefs!= null){
                throw  new RuntimeException("Share pref inited");
         }
        sharedPrefs = new SharedPrefs(context);
    }

    @NonNull
    private static SharedPrefs getPrefs() {
        if (sharedPrefs == null) {
            throw new RuntimeException("Prefs has not been instantiated. Call init() with context");
        }
        return sharedPrefs;
    }

    public static int getFolderColumnsPortrait() {
        return getPrefs().get(Keys.FOLDER_COLUMNS_PORTRAIT, DefaultPrefs.FOLDER_COLUMNS_PORTRAIT);
    }


    public static int getFolderColumnsLandscape() {
        return getPrefs().get(Keys.FOLDER_COLUMNS_LANDSCAPE, DefaultPrefs.FOLDER_COLUMNS_LANDSCAPE);
    }


    public static int getMediaColumnsPortrait() {
        return getPrefs().get(Keys.MEDIA_COLUMNS_PORTRAIT, DefaultPrefs.MEDIA_COLUMNS_PORTRAIT);
    }

    public static int getMediaColumnsLandscape() {
        return getPrefs().get(Keys.MEDIA_COLUMNS_LANDSCAPE, DefaultPrefs.MEDIA_COLUMNS_LANDSCAPE);
    }

}
