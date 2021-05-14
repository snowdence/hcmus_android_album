package com.wifosoft.wumbum.util;

import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;

import com.wifosoft.wumbum.util.preferences.Prefs;



public class AnimationUtils {

    public static RecyclerView.ItemAnimator getItemAnimator(RecyclerView.ItemAnimator itemAnimator) {
        if(Prefs.animationsEnabled()) {
            return itemAnimator;
        }
        return null;
    }

    public static ViewPager.PageTransformer getPageTransformer(ViewPager.PageTransformer pageTransformer) {
        if(Prefs.animationsEnabled()) {
            return pageTransformer;
        }
        return null;
    }
}
