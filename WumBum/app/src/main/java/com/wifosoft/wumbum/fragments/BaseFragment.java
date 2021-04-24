package com.wifosoft.wumbum.fragments;
import android.content.Context;

import com.wifosoft.wumbum.interfaces.INothingToShowListener;

import org.horaapps.liz.ThemedFragment;

public abstract class BaseFragment extends ThemedFragment {

    private INothingToShowListener nothingToShowListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof INothingToShowListener)
            nothingToShowListener = (INothingToShowListener) context;
    }

    public INothingToShowListener getNothingToShowListener() {
        return nothingToShowListener;
    }

    public void setNothingToShowListener(INothingToShowListener nothingToShowListener) {
        this.nothingToShowListener = nothingToShowListener;
    }
}
