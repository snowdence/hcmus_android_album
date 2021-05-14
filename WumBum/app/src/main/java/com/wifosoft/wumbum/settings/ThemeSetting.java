package com.wifosoft.wumbum.settings;
import org.horaapps.liz.ThemedActivity;


public class ThemeSetting {

    private ThemedActivity activity;

    ThemeSetting(ThemedActivity activity) {
        this.activity = activity;
    }

    public ThemeSetting() {
    }

    public ThemedActivity getActivity() {
        return activity;
    }

}
