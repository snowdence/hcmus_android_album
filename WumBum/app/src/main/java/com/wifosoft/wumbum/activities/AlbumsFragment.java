package com.wifosoft.wumbum.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.orhanobut.hawk.Hawk;
import com.wifosoft.wumbum.R;
import com.wifosoft.wumbum.adapters.AlbumsAdapter;
import com.wifosoft.wumbum.fragments.BaseMediaGridFragment;
import com.wifosoft.wumbum.helper.AlbumsHelper;
import com.wifosoft.wumbum.helper.AlertDialogsHelper;
import com.wifosoft.wumbum.helper.QueryAlbums;
import com.wifosoft.wumbum.helper.QueryHelper;
import com.wifosoft.wumbum.model.Album;
import com.wifosoft.wumbum.sort.SortingMode;
import com.wifosoft.wumbum.sort.SortingOrder;
import com.wifosoft.wumbum.util.AnimationUtils;
import com.wifosoft.wumbum.util.DeviceUtils;
import com.wifosoft.wumbum.util.Measure;
import com.wifosoft.wumbum.util.preferences.Prefs;
import com.wifosoft.wumbum.views.GridSpacingItemDecoration;

import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static android.app.Activity.RESULT_OK;

/**
 * Created by dnld on 3/13/17.
 */

public class AlbumsFragment extends BaseMediaGridFragment {

    public static final String TAG = "AlbumsFragment";

    @BindView(R.id.albums) RecyclerView rv;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout refresh;

    private AlbumsAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;
    private AlbumClickListener listener;
    private double latitude = 0;
    private double longitude = 0;
    private int PERMISSION_ID = 55;
    public static final int REQUEST_IMAGE_CAPTURE = 1512;
    public static final int REQUEST_VIDEO_CAPTURE = 1513;
    FusedLocationProviderClient mFusedLocationClient;
    private String fileName;
    private File tempFile;

    private boolean hidden = false;
    ArrayList<String> excuded = new ArrayList<>();

    public interface AlbumClickListener {
        void onAlbumClick(Album album);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        excuded = db().getExcludedFolders(getContext());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        getLastLocation();

    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);


        if (Prefs.vietnameseEnabled()) {
            Locale locale = new Locale("vi", "VN");
            Locale.setDefault(locale);
            Resources resources = this.getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
        else{
            Locale.setDefault(Locale.getDefault());
            Resources resources = this.getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(Locale.getDefault());
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {
                if (mFusedLocationClient == null) {mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());}

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AlbumClickListener) listener = (AlbumClickListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!clearSelected())
            updateToolbar();
        setUpColumns();
    }

    public void displayAlbums(boolean hidden) {
        this.hidden = hidden;
        displayAlbums();
    }

    private void displayAlbums() {
        adapter.clear();
        SQLiteDatabase db = QueryAlbums.getInstance(getContext().getApplicationContext()).getReadableDatabase();
        QueryHelper.getAlbums(getContext(), hidden, excuded, sortingMode(), sortingOrder())
                .subscribeOn(Schedulers.io())
                .map(album -> album.withSettings(QueryAlbums.getSettings(db, album.getPath())))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        album -> adapter.add(album),
                        throwable -> {
                            refresh.setRefreshing(false);
                            throwable.printStackTrace();
                        },
                        () -> {
                            db.close();
                            if (getNothingToShowListener() != null)
                                getNothingToShowListener().changedNothingToShow(getCount() == 0);
                            refresh.setRefreshing(false);

                            Hawk.put(hidden ? "h" : "albums", adapter.getAlbumsPaths());
                        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayAlbums();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpColumns();
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rv.getLayoutManager()).getSpanCount()) {
            rv.removeItemDecoration(spacingDecoration);
            spacingDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getContext()), true);
            rv.addItemDecoration(spacingDecoration);
            rv.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
        }
    }

    public int columnsCount() {
        return DeviceUtils.isPortrait(getResources())
                ? Prefs.getFolderColumnsPortrait()
                : Prefs.getFolderColumnsLandscape();
    }

    @Override
    public int getTotalCount() {
        return adapter.getItemCount();
    }

    @Override
    public View.OnClickListener getToolbarButtonListener(boolean editMode) {
        if (editMode) return null;
        else return v -> adapter.clearSelected();
    }

    @Override
    public String getToolbarTitle() {
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_albums, container, false);
        ButterKnife.bind(this, v);

        int spanCount = columnsCount();
        spacingDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(spacingDecoration);
        rv.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        if(Prefs.animationsEnabled()) {
            rv.setItemAnimator(
                    AnimationUtils.getItemAnimator(
                            new LandingAnimator(new OvershootInterpolator(1f))
                    ));
        }

        adapter = new AlbumsAdapter(getContext(), this);

        refresh.setOnRefreshListener(this::displayAlbums);
        rv.setAdapter(adapter);
        return v;
    }

    public SortingMode sortingMode() {
        return adapter.sortingMode();
    }

    public SortingOrder sortingOrder() {
        return adapter.sortingOrder();
    }

    private QueryAlbums db() {
        return QueryAlbums.getInstance(getContext().getApplicationContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.grid_albums, menu);

        menu.findItem(R.id.select_all).setIcon(ThemeHelper.getToolbarIcon(getContext(), GoogleMaterial.Icon.gmd_select_all));
        menu.findItem(R.id.delete).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_delete)));
        menu.findItem(R.id.sort_action).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_sort)));
        menu.findItem(R.id.search_action).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_search)));
        menu.findItem(R.id.action_camera).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_photo_camera)));
        menu.findItem(R.id.action_video).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_videocam)));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean editMode = editMode();
        boolean oneSelected = getSelectedCount() == 1;

        menu.setGroupVisible(R.id.general_album_items, !editMode);
        menu.setGroupVisible(R.id.edit_mode_items, editMode);
        menu.setGroupVisible(R.id.one_selected_items, oneSelected);

        menu.findItem(R.id.select_all).setTitle(
                getSelectedCount() == getCount()
                        ? R.string.clear_selected
                        : R.string.select_all);

        if (editMode) {
            menu.findItem(R.id.hide).setTitle(hidden ? R.string.unhide : R.string.hide);
        } else {
            menu.findItem(R.id.ascending_sort_order).setChecked(sortingOrder() == SortingOrder.ASCENDING);
            switch (sortingMode()) {
                case NAME:  menu.findItem(R.id.name_sort_mode).setChecked(true); break;
                case SIZE:  menu.findItem(R.id.size_sort_mode).setChecked(true); break;
                case DATE: default:
                    menu.findItem(R.id.date_taken_sort_mode).setChecked(true); break;
                case NUMERIC:  menu.findItem(R.id.numeric_sort_mode).setChecked(true); break;
            }
        }

        if (oneSelected) {
            Album selectedAlbum = adapter.getFirstSelectedAlbum();
            menu.findItem(R.id.pin_album).setTitle(selectedAlbum.isPinned() ? getString(R.string.un_pin) : getString(R.string.pin));
            menu.findItem(R.id.clear_album_cover).setVisible(selectedAlbum.hasCover());
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Album selectedAlbum = adapter.getFirstSelectedAlbum();
        switch (item.getItemId()) {

            case R.id.select_all:
                if (adapter.getSelectedCount() == adapter.getItemCount())
                    adapter.clearSelected();
                else adapter.selectAll();
                return true;

            case R.id.pin_album:
                if (selectedAlbum != null) {
                    boolean b = selectedAlbum.togglePinAlbum();
                    db().setPined(selectedAlbum.getPath(), b);
                    adapter.clearSelected();
                    adapter.sort();
                }
                return true;

            case R.id.clear_album_cover:
                if (selectedAlbum != null) {
                    selectedAlbum.removeCoverAlbum();
                    db().setCover(selectedAlbum.getPath(), null);
                    adapter.clearSelected();
                    adapter.notifyItemChanaged(selectedAlbum);
                    // TODO: 4/5/17 updateui
                    return true;
                }

                return false;

            case R.id.hide:
                final EditText editText = new EditText(getContext());
                AlertDialog insertTextDialog = AlertDialogsHelper.getInsertTextDialog(((ThemedActivity) getActivity()), editText, R.string.set_password);
                insertTextDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String password  = editText.getText().toString();
                        db().secureAlbum(selectedAlbum.getPath(),password);

                        final AlertDialog textDialog = AlertDialogsHelper.getTextDialog( (ThemedActivity)getActivity(), R.string.status_secure_password, R.string.detail_secure_password);
                        textDialog.setButton(DialogInterface.BUTTON_NEGATIVE,  getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                textDialog.dismiss();
                            }
                        });
                        textDialog.show();

                    }
                });
                insertTextDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
                insertTextDialog.show();

//                final AlertDialog hideDialog = AlertDialogsHelper.getTextDialog(((ThemedActivity) getActivity()),
//                        hidden ? R.string.unhide : R.string.hide,
//                        hidden ? R.string.unhide_album_message : R.string.hide_album_message);
//
//                hideDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(hidden ? R.string.unhide : R.string.hide).toUpperCase(), (dialog, id) -> {
//                    ArrayList<String> hiddenPaths = AlbumsHelper.getLastHiddenPaths();
//
//                    for (Album album : adapter.getSelectedAlbums()) {
//                        if (hidden) { // unhide
//                            AlbumsHelper.unHideAlbum(album.getPath(), getContext());
//                            hiddenPaths.remove(album.getPath());
//                        } else { // hide
//                            AlbumsHelper.hideAlbum(album.getPath(), getContext());
//                            hiddenPaths.add(album.getPath());
//                        }
//                    }
//                    AlbumsHelper.saveLastHiddenPaths(hiddenPaths);
//                    adapter.removeSelectedAlbums();
//                    updateToolbar();
//                });
//
//                if (!hidden) {
//                    hideDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.exclude).toUpperCase(), (dialog, which) -> {
//                        for (Album album : adapter.getSelectedAlbums()) {
//                            db().excludeAlbum(album.getPath());
//                            excuded.add(album.getPath());
//                        }
//                        adapter.removeSelectedAlbums();
//                    });
//                }
//                hideDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.cancel).toUpperCase(), (dialogInterface, i) -> hideDialog.dismiss());
//                hideDialog.show();
                return true;

            case R.id.shortcut:
                AlbumsHelper.createShortcuts(getContext(), adapter.getSelectedAlbums());
                adapter.clearSelected();
                return true;

            case R.id.name_sort_mode:
                adapter.changeSortingMode(SortingMode.NAME);
                AlbumsHelper.setSortingMode(SortingMode.NAME);
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_mode:
                adapter.changeSortingMode(SortingMode.DATE);
                AlbumsHelper.setSortingMode(SortingMode.DATE);
                item.setChecked(true);
                return true;

            case R.id.size_sort_mode:
                adapter.changeSortingMode(SortingMode.SIZE);
                AlbumsHelper.setSortingMode(SortingMode.SIZE);
                item.setChecked(true);
                return true;

            case R.id.numeric_sort_mode:
                adapter.changeSortingMode(SortingMode.NUMERIC);
                AlbumsHelper.setSortingMode(SortingMode.NUMERIC);
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_order:
                item.setChecked(!item.isChecked());
                SortingOrder sortingOrder = SortingOrder.fromValue(item.isChecked());
                adapter.changeSortingOrder(sortingOrder);
                AlbumsHelper.setSortingOrder(sortingOrder);
                return true;

//            case R.id.exclude:
//                final AlertDialog.Builder excludeDialogBuilder = new AlertDialog.Builder(getActivity(), getDialogStyle());
//
//                final View excludeDialogLayout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_exclude, null);
//                TextView textViewExcludeTitle = excludeDialogLayout.findViewById(R.id.text_dialog_title);
//                TextView textViewExcludeMessage = excludeDialogLayout.findViewById(R.id.text_dialog_message);
//                final Spinner spinnerParents = excludeDialogLayout.findViewById(R.id.parents_folder);
//
//                spinnerParents.getBackground().setColorFilter(getIconColor(), PorterDuff.Mode.SRC_ATOP);
//
//                ((CardView) excludeDialogLayout.findViewById(R.id.message_card)).setCardBackgroundColor(getCardBackgroundColor());
//                textViewExcludeTitle.setBackgroundColor(getPrimaryColor());
//                textViewExcludeTitle.setText(getString(R.string.exclude));
//
//                if(adapter.getSelectedCount() > 1) {
//                    textViewExcludeMessage.setText(R.string.exclude_albums_message);
//                    spinnerParents.setVisibility(View.GONE);
//                } else {
//                    textViewExcludeMessage.setText(R.string.exclude_album_message);
//                    spinnerParents.setAdapter(getThemeHelper().getSpinnerAdapter(adapter.getFirstSelectedAlbum().getParentsFolders()));
//                }
//
//                textViewExcludeMessage.setTextColor(getTextColor());
//                excludeDialogBuilder.setView(excludeDialogLayout);
//
//                excludeDialogBuilder.setPositiveButton(this.getString(R.string.exclude).toUpperCase(), (dialog, id) -> {
//
//                    if (adapter.getSelectedCount() > 1) {
//                        for (Album album : adapter.getSelectedAlbums()) {
//                            db().excludeAlbum(album.getPath());
//                            excuded.add(album.getPath());
//                        }
//                        adapter.removeSelectedAlbums();
//
//                    } else {
//                        String path = spinnerParents.getSelectedItem().toString();
//                        db().excludeAlbum(path);
//                        excuded.add(path);
//                        adapter.removeAlbumsThatStartsWith(path);
//                        adapter.forceSelectedCount(0);
//                    }
//                    updateToolbar();
//                });
//                excludeDialogBuilder.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
//                excludeDialogBuilder.show();
//                return true;

            case R.id.delete:


                return true;
            case R.id.action_camera:
                if (checkCameraHardware(getContext())) {
                    dispatchCamera();
                }
                return true;
            case R.id.action_video:
                if (checkCameraHardware(getContext())) {
                    dispatchVideo();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final String convert(double latitude) {
        latitude=Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude*1000.0d);

        StringBuilder sb = new StringBuilder(20);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000");
        return sb.toString();
    }
    private void geoTag(){
        ExifInterface exif;

        try {
            exif = new ExifInterface(tempFile.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convert(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convert(longitude));


            if (latitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (longitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }

            exif.saveAttributes();

            Log.d("TAG", "getGeo latitude " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            Log.d("TAG", "getGeo longitude " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                geoTag();
            } else {
                File f = new File(Environment.getExternalStoragePublicDirectory("DCIM").toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals(fileName)) {
                        f = temp;
                        break;
                    }
                }
                f.delete();
            }
        }
    }
    private void dispatchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        fileName = "WUMBUM_" + timeStamp + ".jpg";
        tempFile = new File(android.os.Environment.getExternalStoragePublicDirectory("DCIM"), fileName);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        getActivity().startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }
    private void dispatchVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        fileName = "WUMBUM_" + timeStamp + ".mp4";
        tempFile = new File(android.os.Environment.getExternalStoragePublicDirectory("DCIM"), fileName);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        getActivity().startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }


    public int getCount() {
        return adapter.getItemCount();
    }

    public int getSelectedCount() {
        return adapter.getSelectedCount();
    }

    @Override
    public boolean editMode() {
        return adapter.selecting();
    }

    @Override
    public boolean clearSelected() {
        return adapter.clearSelected();
    }

    @Override
    public void onItemSelected(int position) {
        if (listener != null) listener.onAlbumClick(adapter.get(position));
    }

    @Override
    public void onSelectMode(boolean selectMode) {
        refresh.setEnabled(!selectMode);
        updateToolbar();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onSelectionCountChanged(int selectionCount, int totalCount) {
        getEditModeListener().onItemsSelected(selectionCount, totalCount);
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        rv.setBackgroundColor(t.getBackgroundColor());
        adapter.refreshTheme(t);
        refresh.setColorSchemeColors(t.getAccentColor());
        refresh.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }
}
