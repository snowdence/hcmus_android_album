package com.wifosoft.wumbum.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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
import com.wifosoft.wumbum.fragments.TimelineFragment;
import com.wifosoft.wumbum.helper.AlertDialogsHelper;
import com.wifosoft.wumbum.interfaces.IEditModeListener;
import com.wifosoft.wumbum.interfaces.IMediaClickListener;
import com.wifosoft.wumbum.interfaces.INothingToShowListener;
import com.wifosoft.wumbum.model.Album;
import com.wifosoft.wumbum.model.Media;

import com.wifosoft.wumbum.util.StringUtils;
import com.wifosoft.wumbum.util.preferences.Prefs;
import com.wifosoft.wumbum.util.preferences.SharedPrefs;
import com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.ItemListener;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ABOUT;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ALL_ALBUMS;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ALL_MEDIA;
import static com.wifosoft.wumbum.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_DONATE;
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
                        if (inMediaMode()) {
                            goBackToAlbums();
                        }
                        displayAlbums(false);
                        break;
                    case 1:
                        Toast.makeText(MainActivity.this, "All Image", Toast.LENGTH_SHORT).show();
                        displayMedia(Album.getAllMediaAlbum());
                        break;
                    case 2:
                        Toast.makeText(MainActivity.this, "Timeline", Toast.LENGTH_SHORT).show();
                        if(inTimelineMode()){
                            goBackToAlbums();
                        }
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
                //setupUiForTimeline();
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
        //
//        if (!pickMode) {
//            Intent intent = new Intent(getApplicationContext(), SingleMediaActivity.class);
//            intent.putExtra(SingleMediaActivity.EXTRA_ARGS_ALBUM, album);
//            try {
//                intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
//                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media);
//                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_POSITION, position);
//                startActivity(intent);
//            } catch (Exception e) { // Putting too much data into the Bundle
//                // TODO: Find a better way to pass data between the activities - possibly a key to
//                // access a HashMap or a unique value of a singleton Data Repository of some sort.
//                intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM_LAZY);
//                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media.get(position));
//                startActivity(intent);
//            }
//
//        } else {
//
//            Media m = media.get(position);
//            Uri uri = LegacyCompatFileProvider.getUri(getApplicationContext(), m.getFile());
//            Intent res = new Intent();
//            res.setData(uri);
//            res.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            setResult(RESULT_OK, res);
//            finish();
//        }
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

        // TODO Calvin: This performs a NO-OP. Find out what this is used for
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
                //SettingsActivity.startActivity(this);
                return true;

            /*
            case R.id.action_move:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.move_to))
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                //TODo
                                //swipeRefreshLayout.setRefreshing(true);
                                /*if (getAlbum().moveSelectedMedia(getApplicationContext(), path) > 0) {
                                    if (getAlbum().getMedia().size() == 0) {
                                        //getAlbums().removeCurrentAlbum();
                                        //albumsAdapter.notifyDataSetChanged();
                                        displayAlbums(false);
                                    }
                                    //oldMediaAdapter.swapDataSet(getAlbum().getMedia());
                                    //finishEditMode();
                                    supportInvalidateOptionsMenu();
                                } else requestSdCardPermissions();

                                //swipeRefreshLayout.setRefreshing(false);
                            }
                        }).show();
                return true;
                */

            /*
            case R.id.action_copy:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.copy_to))
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                boolean success = getAlbum().copySelectedPhotos(getApplicationContext(), path);
                                //finishEditMode();

                                if (!success) // TODO: 11/21/16 handle in other way
                                    requestSdCardPermissions();
                            }
                        }).show();

                return true;

                */


            /*case R.id.rename:
                final EditText editTextNewName = new EditText(getApplicationContext());
                editTextNewName.setText(albumsMode ? firstSelectedAlbum.getName() : getAlbum().getName());

                final AlertDialog insertTextDialog = AlertDialogsHelper.getInsertTextDialog(MainActivity.this, editTextNewName, R.string.rename_album);

                insertTextDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onMediaClick(DialogInterface dialogInterface, int i) {
                        insertTextDialog.dismiss();
                    }
                });

                insertTextDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onMediaClick(DialogInterface dialogInterface, int i) {
                        if (editTextNewName.length() != 0) {
                            swipeRefreshLayout.setRefreshing(true);
                            boolean success;
                            if (albumsMode) {

                                int index = getAlbums().albums.indexOf(firstSelectedAlbum);
                                getAlbums().getAlbum(index).updatePhotos(getApplicationContext());
                                success = getAlbums().getAlbum(index).renameAlbum(getApplicationContext(),
                                        editTextNewName.getText().toString());
                                albumsAdapter.notifyItemChanged(index);
                            } else {
                                success = getAlbum().renameAlbum(getApplicationContext(), editTextNewName.getText().toString());
                                toolbar.setTitle(getAlbum().getName());
                                oldMediaAdapter.notifyDataSetChanged();
                            }
                            insertTextDialog.dismiss();
                            if (!success) requestSdCardPermissions();
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            StringUtils.showToast(getApplicationContext(), getString(R.string.insert_something));
                            editTextNewName.requestFocus();
                        }
                    }
                });

                insertTextDialog.show();
                return true;*/


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
                //SettingsActivity.startActivity(this);
                break;

            case NAVIGATION_ITEM_ABOUT:
                Toast.makeText(this, "About WumBum App", Toast.LENGTH_SHORT).show();
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
}
