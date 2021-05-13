package com.wifosoft.wumbum.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.orhanobut.hawk.Hawk;

import com.wifosoft.wumbum.BuildConfig;
import com.wifosoft.wumbum.R;
import com.wifosoft.wumbum.activities.base.SharedMediaActivity;
//
//import com.wifosoft.wumbum.fragments.AlbumsFragment;
//import com.wifosoft.wumbum.fragments.RvMediaFragment;
import com.wifosoft.wumbum.fragments.AllMediaFragment;
import com.wifosoft.wumbum.fragments.FavoriteMediaFragment;
import com.wifosoft.wumbum.fragments.TimelineFragment;
import com.wifosoft.wumbum.interfaces.IEditModeListener;
import com.wifosoft.wumbum.interfaces.IMediaClickListener;
import com.wifosoft.wumbum.interfaces.INothingToShowListener;
import com.wifosoft.wumbum.model.Album;
import com.wifosoft.wumbum.model.Media;

import com.wifosoft.wumbum.providers.LegacyCompatFileProvider;
import com.wifosoft.wumbum.util.FavoriteUtils;
import com.wifosoft.wumbum.util.Security;
import com.wifosoft.wumbum.util.preferences.Prefs;
import com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer;

import org.horaapps.liz.ThemedActivity;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.ItemListener;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ABOUT;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ALL_ALBUMS;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ALL_MEDIA;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_DONATE;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_FAVORITE;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_HIDDEN_FOLDERS;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_SETTINGS;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_TIMELINE;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_WALLPAPERS;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NavigationItem;

/**
 * The Main Activity used to display Albums / Media.
 */
public class MainActivity extends SharedMediaActivity implements
        IMediaClickListener, AlbumsFragment.AlbumClickListener,
        INothingToShowListener, ItemListener, IEditModeListener {

    public static final String ARGS_PICK_MODE = "pick_mode";

    private static final String SAVE_FRAGMENT_MODE = "fragment_mode";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public void changedEditMode(boolean editMode, int selected, int total, @Nullable View.OnClickListener listener, @Nullable String title) {
        if (editMode) {
            updateToolbar(
                    getString(R.string.toolbar_selection_count, selected, total),
                    GoogleMaterial.Icon.gmd_check, listener);
        } else if (inAlbumMode()) {
            showDefaultToolbar();
        } else {
            updateToolbar(title, GoogleMaterial.Icon.gmd_arrow_back, v -> goBackToAlbums());
        }
    }

    @Override
    public void onItemsSelected(int count, int total) {
        toolbar.setTitle(getString(R.string.toolbar_selection_count, count, total));

    }

    public @interface FragmentMode {
        int MODE_ALBUMS = 1001;
        int MODE_MEDIA = 1002;
        int MODE_TIMELINE = 1003;
    }

    @BindView(R.id.fab_camera)
    FloatingActionButton fab;
    @BindView(R.id.drawer_layout)
    DrawerLayout navigationDrawer;
    @BindView(R.id.home_navigation_drawer)
    NavigationDrawer navigationDrawerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinator_main_layout)
    CoordinatorLayout mainLayout;

    @BindView(R.id.tabFragMode)
    TabLayout tabFragMode;

    private AlbumsFragment albumsFragment;
    private AllMediaFragment allMediaFragment;
    private TimelineFragment timelineFragment;
    private FavoriteMediaFragment favoriteMediaFragment;

    private boolean pickMode = false;
    private Unbinder unbinder;

    @FragmentMode
    private int fragmentMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        initUi();
        pickMode = getIntent().getBooleanExtra(ARGS_PICK_MODE, false);

        tabFragMode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tab_pos = tab.getPosition();
                switch (tab_pos) {
                    case 0:
                        unreferenceFragments();
                        albumsFragment = (AlbumsFragment) getSupportFragmentManager().findFragmentByTag(AlbumsFragment.TAG);

                        displayAlbums(false);
                        break;
                    case 1:
                        unreferenceFragments();
                        Toast.makeText(MainActivity.this, "All Image", Toast.LENGTH_SHORT).show();
                        displayMedia(Album.getAllMediaAlbum());
                        break;
                    case 2:
                        unreferenceFragments();
                        Toast.makeText(MainActivity.this, "Timeline", Toast.LENGTH_SHORT).show();
                        displayTimeline(Album.getAllMediaAlbum());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (savedInstanceState == null) {
            fragmentMode = FragmentMode.MODE_ALBUMS;
            initAlbumsFragment();
            setContentFragment();

            return;
        }

        /* We have some instance state */
        restoreState(savedInstanceState);

        switch (fragmentMode) {

            case FragmentMode.MODE_MEDIA:
                //TODO View all media
//                rvMediaFragment = (RvMediaFragment) getSupportFragmentManager().findFragmentByTag(RvMediaFragment.TAG);
//                rvMediaFragment.setListener(this);
//
                allMediaFragment = (AllMediaFragment) getSupportFragmentManager().findFragmentByTag(AllMediaFragment.TAG);
                allMediaFragment.setListener(this);
                break;

            case FragmentMode.MODE_ALBUMS:
                albumsFragment = (AlbumsFragment) getSupportFragmentManager().findFragmentByTag(AlbumsFragment.TAG);
                break;

            case FragmentMode.MODE_TIMELINE:
                setupUiForTimeline();
                break;
        }
    }

    private void setContentFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, albumsFragment, AlbumsFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    private void initAlbumsFragment() {
        unreferenceFragments();
        albumsFragment = new AlbumsFragment();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_FRAGMENT_MODE, fragmentMode);
        super.onSaveInstanceState(outState);
    }

    private void restoreState(@NonNull Bundle savedInstance) {
        fragmentMode = savedInstance.getInt(SAVE_FRAGMENT_MODE, FragmentMode.MODE_ALBUMS);
    }

    public void displayTimeline(Album album) {
        unreferenceFragments();
        timelineFragment = TimelineFragment.Companion.newInstance(album);

        fragmentMode = FragmentMode.MODE_TIMELINE;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, timelineFragment, TimelineFragment.TAG)
                .addToBackStack(null)
                .commit();

        setupUiForTimeline();
    }
    private void displayAlbums(boolean hidden) {

        fragmentMode = FragmentMode.MODE_ALBUMS;
        unlockNavigationDrawer();
        if (albumsFragment == null) initAlbumsFragment();
        albumsFragment.displayAlbums(hidden);
        setContentFragment();
    }


    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onMediaClick(Album album, ArrayList<Media> media, int position) {
        //TODO media click
        Toast.makeText(this, "onMediaClick()", Toast.LENGTH_SHORT).show();



        if (!pickMode) {
            Intent intent = new Intent(getApplicationContext(), SingleMediaActivity.class);
            intent.putExtra(SingleMediaActivity.EXTRA_ARGS_ALBUM, album);
            try {
                intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_POSITION, position);
                startActivity(intent);
            } catch (Exception e) { // Putting too much data into the Bundle
                // TODO: Find a better way to pass data between the activities - possibly a key to
                // access a HashMap or a unique value of a singleton Data Repository of some sort.
                intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM_LAZY);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media.get(position));
                startActivity(intent);
            }

        } else {

            Media m = media.get(position);
            Uri uri = LegacyCompatFileProvider.getUri(getApplicationContext(), m.getFile());
            Intent res = new Intent();
            res.setData(uri);
            res.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setResult(RESULT_OK, res);
            finish();
        }
    }

    @Override
    public void changedNothingToShow(boolean nothingToShow) {
        enableNothingToSHowPlaceHolder(nothingToShow);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fab.setVisibility(View.VISIBLE);
        fab.animate().translationY(fab.getHeight() * 2).start();

    }

    public void goBackToAlbums() {
        unreferenceFragments();
        fragmentMode = FragmentMode.MODE_ALBUMS;
        unlockNavigationDrawer();
        getSupportFragmentManager().popBackStack();

        albumsFragment = (AlbumsFragment) getSupportFragmentManager().findFragmentByTag(AlbumsFragment.TAG);
        selectNavigationItem(NAVIGATION_ITEM_ALL_ALBUMS);
        showDefaultToolbar();
    }

    private void unreferenceFragments() {
        // TODO Calvin: This is a hack for the current back button behavior.
        // Refactor the logic to avoid these member variables.
        // Allow the GC to reclaim the fragments for now
        albumsFragment = null;
        allMediaFragment = null;
        timelineFragment = null;
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        setupNavigationDrawer();
        setupFAB();
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle
                (this, navigationDrawer, toolbar,
                        R.string.drawer_open, R.string.drawer_close);

        navigationDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationDrawerView.setListener(this);
        navigationDrawerView.setAppVersion(BuildConfig.VERSION_NAME);
    }

    private void setupFAB() {
        fab.setImageDrawable(new IconicsDrawable(getApplicationContext()).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fab.setOnClickListener(v -> startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)));
    }

    private void closeDrawer() {
        navigationDrawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        //TODO: MUST BE FIXED
        tabFragMode.setBackgroundColor(getPrimaryColor());
        toolbar.setPopupTheme(getPopupToolbarStyle());
        toolbar.setBackgroundColor(getPrimaryColor());

        /**** SWIPE TO REFRESH ****/

        setStatusBarColor();
        setNavBarColor();

        fab.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        fab.setVisibility(Hawk.get(getString(R.string.preference_show_fab), false) ? View.VISIBLE : View.GONE);
        mainLayout.setBackgroundColor(getBackgroundColor());

//        setScrollViewColor(navigationDrawerView);
        setAllScrollbarsColor();

        navigationDrawerView.setTheme(getPrimaryColor(), getBackgroundColor(), getTextColor(), getIconColor());

        setRecentApp(getString(R.string.app_name));
    }

    private void setAllScrollbarsColor() {
        Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_scrollbar);
        drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));
    }

    private void updateToolbar(String title, IIcon icon, View.OnClickListener onClickListener) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(getToolbarIcon(icon));
        toolbar.setNavigationOnClickListener(onClickListener);
    }

    private void showDefaultToolbar() {
        updateToolbar(
                getString(R.string.app_name),
                GoogleMaterial.Icon.gmd_menu,
                v -> navigationDrawer.openDrawer(GravityCompat.START));
    }

    public void enableNothingToSHowPlaceHolder(boolean status) {
        findViewById(R.id.nothing_to_show_placeholder).setVisibility(status ? View.VISIBLE : View.GONE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        navigationDrawerView.refresh();
    }

    /**
     * region MENU
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {

            case R.id.settings:
                Toast.makeText(this, "Setting screen", Toast.LENGTH_SHORT);
                SettingActivity.startActivity(this);
                return true;

            default:
                /** If we got here, the user's action was not recognized.
                 *  Invoke the superclass to handle it. */
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (inAlbumMode()) {
            if (!albumsFragment.onBackPressed()) {
                if (navigationDrawer.isDrawerOpen(GravityCompat.START)) closeDrawer();
                else finish();
            }
        }
        else if (inTimelineMode() && !timelineFragment.onBackPressed()) {
            goBackToAlbums();

        }
        else if (inMediaMode() && !allMediaFragment.onBackPressed()) {
            goBackToAlbums();
        }
//        else if (inTimelineMode() && !timelineFragment.onBackPressed()) {
//            goBackToAlbums();
//        } else if (inMediaMode() && !rvMediaFragment.onBackPressed()) {
//            goBackToAlbums();
//        }
    }

    @Override
    public void onAlbumClick(Album album) {
        if (album.settings.hasPassword()) {
            Security.authenticateUser(MainActivity.this, album, new Security.AuthCallBack() {
                @Override
                public void onAuthenticated() {
                    displayMedia(album);
                }

                @Override
                public void onError() {
                    Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            displayMedia(album);
        }

        Toast.makeText(this, album.getName(), Toast.LENGTH_SHORT).show();
    }

    public void displayMedia(Album album) {
        unreferenceFragments();

        allMediaFragment = AllMediaFragment.make(album);
        fragmentMode = FragmentMode.MODE_MEDIA;
        lockNavigationDrawer();
        allMediaFragment.setListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, allMediaFragment, AllMediaFragment.TAG).addToBackStack(null).commit();

    }

    public void displayFavoriteMedia(Album album) {
        unreferenceFragments();

        favoriteMediaFragment = FavoriteMediaFragment.make(album);
        fragmentMode = FragmentMode.MODE_MEDIA;
        lockNavigationDrawer();
        favoriteMediaFragment.setListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, favoriteMediaFragment, AllMediaFragment.TAG).addToBackStack(null).commit();
    }

    public void onItemSelected(@NavigationItem int navigationItemSelected) {
        closeDrawer();
        switch (navigationItemSelected) {

            case NAVIGATION_ITEM_ALL_ALBUMS:
                displayAlbums(false);
                selectNavigationItem(navigationItemSelected);
                break;

            case NAVIGATION_ITEM_ALL_MEDIA:
                displayMedia(Album.getAllMediaAlbum());
                selectNavigationItem(navigationItemSelected);

                break;

            case NAVIGATION_ITEM_TIMELINE:
                displayTimeline(Album.getAllMediaAlbum());
                selectNavigationItem(navigationItemSelected);
                break;

            case NAVIGATION_ITEM_HIDDEN_FOLDERS:
//                if (Security.isPasswordOnHidden()) {
//                    askPassword();
//                } else {
//                    selectNavigationItem(navigationItemSelected);
//                    displayAlbums(true);
//                }
                break;

            case NAVIGATION_ITEM_WALLPAPERS:
                Toast.makeText(MainActivity.this, "Coming Soon WallPaper!", Toast.LENGTH_SHORT).show();
                break;

            case NAVIGATION_ITEM_DONATE:
                Toast.makeText(this, "Donate???", Toast.LENGTH_SHORT).show();
                Prefs.clearAllData();
                break;

            case NavigationDrawer.NAVIGATION_ITEM_AFFIX:
                // Intent i = new Intent(getBaseContext(), AffixActivity.class);
                //startActivity(i);
                //   AffixActivity.startActivity(this);
                Toast.makeText(this, "Affix", Toast.LENGTH_SHORT).show();
                break;
            case NAVIGATION_ITEM_SETTINGS:
                SettingActivity.startActivity(this);
                break;

            case NAVIGATION_ITEM_ABOUT:
                Toast.makeText(this, "About WumBum App", Toast.LENGTH_SHORT).show();
                break;
            case NAVIGATION_ITEM_FAVORITE:
                displayFavoriteMedia(Album.getAllMediaAlbum());
                selectNavigationItem(navigationItemSelected);
                Toast.makeText(this, String.valueOf(FavoriteUtils.getFavorites().size()), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void selectNavigationItem(@NavigationItem int navItem) {
        navigationDrawerView.selectNavItem(navItem);
    }

    private boolean inAlbumMode() {
        return fragmentMode == FragmentMode.MODE_ALBUMS;
    }

    private boolean inMediaMode() {
        return fragmentMode == FragmentMode.MODE_MEDIA;
    }

    private boolean inTimelineMode() {
        return fragmentMode == FragmentMode.MODE_TIMELINE;
    }

    private void lockNavigationDrawer() {
        navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void unlockNavigationDrawer() {
        navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void setupUiForTimeline() {
        lockNavigationDrawer();
        updateToolbar(getString(R.string.timeline_toolbar_title), GoogleMaterial.Icon.gmd_arrow_back, v -> goBackToAlbums());
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
//        if (requestCode == AllMediaFragment.REQUEST_IMAGE_CAPTURE || requestCode == AllMediaFragment.REQUEST_VIDEO_CAPTURE) {
//            if (resultCode == RESULT_OK) {
//                displayMedia(Album.getAllMediaAlbum());
//            }
//        }
    }
}
