package com.wifosoft.wumbum.views.themeable;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.Themed;

/**
 * Created by darken (darken@darken.eu) on 04.03.2017.
 */
public class ThemedSettingsCaption extends androidx.appcompat.widget.AppCompatTextView implements Themed {

    public ThemedSettingsCaption(Context context) {
        this(context, null);
    }

    public ThemedSettingsCaption(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThemedSettingsCaption(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        setTextColor(themeHelper.getSubTextColor());
    }
}
