package com.wifosoft.wumbum.settings;

import org.horaapps.liz.ThemedActivity;


class BaseSetting {

    private ThemedActivity activity;

    BaseSetting(ThemedActivity activity) {
        this.activity = activity;
    }

    public BaseSetting() {
    }

    public ThemedActivity getActivity() {
        return activity;
    }

}
