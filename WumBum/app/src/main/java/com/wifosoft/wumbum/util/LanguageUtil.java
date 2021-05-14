package com.wifosoft.wumbum.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import com.wifosoft.wumbum.App;
import com.wifosoft.wumbum.R;
import com.wifosoft.wumbum.util.preferences.SharedPrefs;

import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.internal.Constants;

public class LanguageUtil {
    public static String VI_LANGUAGUGE= "vi-rVN";
    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}