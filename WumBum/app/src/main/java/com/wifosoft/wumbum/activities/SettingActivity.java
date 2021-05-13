package com.wifosoft.wumbum.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.ScrollView;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.orhanobut.hawk.Hawk;

import com.wifosoft.wumbum.R;
import com.wifosoft.wumbum.settings.GeneralSetting;

import com.wifosoft.wumbum.views.SettingWithSwitchView;
import org.horaapps.liz.ColorPalette;
import org.horaapps.liz.ThemedActivity;
import org.horaapps.liz.ViewUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * The Settings Activity used to select settings.
 */
public class SettingActivity extends ThemedActivity {
    private Toolbar toolbar;

    @BindView(R.id.option_max_brightness) SettingWithSwitchView optionMaxBrightness;
    @BindView(R.id.option_picture_orientation) SettingWithSwitchView optionOrientation;
    @BindView(R.id.option_full_resolution) SettingWithSwitchView optionDelayFullRes;

    @BindView(R.id.option_auto_update_media) SettingWithSwitchView optionAutoUpdateMedia;
    @BindView(R.id.option_include_video) SettingWithSwitchView optionIncludeVideo;
    @BindView(R.id.option_swipe_direction) SettingWithSwitchView optionSwipeDirection;

    @BindView(R.id.option_fab) SettingWithSwitchView optionShowFab;
    @BindView(R.id.option_statusbar) SettingWithSwitchView optionStatusbar;
    @BindView(R.id.option_colored_navbar) SettingWithSwitchView optionColoredNavbar;

    @BindView(R.id.option_sub_scaling) SettingWithSwitchView optionSubScaling;
    @BindView(R.id.option_disable_animations) SettingWithSwitchView optionDisableAnimations;

    private Unbinder unbinder;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        unbinder = ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        optionStatusbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTheme();
                setStatusBarColor();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ViewUtil.hasNavBar(this)) {
                optionColoredNavbar.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View view) {
                        updateTheme();
                        getWindow().setNavigationBarColor(isNavigationBarColored() ? getPrimaryColor() : ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
                    }
                });
            } else optionColoredNavbar.setVisibility(View.GONE);
        }
        ScrollView scrollView = findViewById(R.id.settingAct_scrollView);
        setScrollViewColor(scrollView);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        super.onDestroy();
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        findViewById(com.wifosoft.wumbum.R.id.setting_background).setBackgroundColor(getBackgroundColor());
        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(com.wifosoft.wumbum.R.string.settings));
    }

    @Override
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = getThemeHelper().getPrimaryColor();
            if (isTranslucentStatusBar())
                getWindow().setStatusBarColor(ColorPalette.getObscuredColor(color));
            else getWindow().setStatusBarColor(color);
            if (isNavigationBarColored()) getWindow().setNavigationBarColor(color);
            else
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.md_black_1000));
        }
    }





    @OnClick(R.id.ll_custom_icon_color)
    public void onChangedCustomIconClicked(View view) {
        updateTheme();
        updateUiElements();
    }

    @OnClick(R.id.ll_white_list)
    public void onWhiteListClicked(View view) {
        startActivity(new Intent(getApplicationContext(), BlackWhiteListActivity.class));
    }


    @OnClick(R.id.ll_n_columns)
    public void onChangeColumnsClicked(View view) {
        new GeneralSetting(SettingActivity.this).editNumberOfColumns();
    }

}
