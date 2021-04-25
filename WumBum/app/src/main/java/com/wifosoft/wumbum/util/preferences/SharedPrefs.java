package com.wifosoft.wumbum.util.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import javax.annotation.Nonnull;

public class SharedPrefs {
    private static String PREF_NAME = "com.wifosoft.wumbum.SHARED_PREFS";
    private static int PREF_MODE = Context.MODE_PRIVATE;
    private final SharedPreferences sharedPrefs;

    SharedPrefs(@NonNull Context context) {
        sharedPrefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, PREF_MODE);
    }

    @Nonnull
    private SharedPreferences.Editor getEditor() {
        return sharedPrefs.edit();
    }


    int get(@NonNull String key, int defaultValue) {
        return sharedPrefs.getInt(key, defaultValue);
    }

    void put(@NonNull String key, int value) {
        getEditor().putInt(key, value).commit();
    }

    boolean get(@NonNull String key, boolean defaultValue) {
        return sharedPrefs.getBoolean(key, defaultValue);
    }

    void put(@NonNull String key, boolean value) {
        getEditor().putBoolean(key, value).commit();
    }

    public void clearAll(){
        getEditor().clear().commit();
    }
}
