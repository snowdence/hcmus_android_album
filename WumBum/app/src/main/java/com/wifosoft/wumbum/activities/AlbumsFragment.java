package com.wifosoft.wumbum.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.orhanobut.hawk.Hawk;

import com.wifosoft.wumbum.R;
import com.wifosoft.wumbum.adapters.AlbumsAdapter;
import com.wifosoft.wumbum.fragments.BaseMediaGridFragment;
import com.wifosoft.wumbum.helper.AlbumsHelper;
import com.wifosoft.wumbum.helper.AlertDialogsHelper;
import com.wifosoft.wumbum.helper.MediaHelper;
import com.wifosoft.wumbum.helper.QueryAlbums;
import com.wifosoft.wumbum.helper.QueryHelper;
import com.wifosoft.wumbum.model.Album;
import com.wifosoft.wumbum.model.Media;
import com.wifosoft.wumbum.sort.SortingMode;
import com.wifosoft.wumbum.sort.SortingOrder;
import com.wifosoft.wumbum.util.AnimationUtils;
import com.wifosoft.wumbum.util.DeviceUtils;
import com.wifosoft.wumbum.util.Measure;
import com.wifosoft.wumbum.util.StringUtils;
import com.wifosoft.wumbum.util.preferences.Prefs;
import com.wifosoft.wumbum.views.GridSpacingItemDecoration;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;


public class AlbumsFragment extends BaseMediaGridFragment {

    public static final String TAG = "AlbumsFragment";

    @BindView(R.id.albums) RecyclerView rv;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout refresh;

    private AlbumsAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;
    private AlbumClickListener listener;

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
        //menu.findItem(R.id.search_action).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_search)));

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

        }

        return super.onOptionsItemSelected(item);
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
