package com.wifosoft.wumbum.fragments;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import com.wifosoft.wumbum.R;
import com.wifosoft.wumbum.adapters.MediaAdapter;

import com.wifosoft.wumbum.exception.progress.ProgressBottomSheet;
import com.wifosoft.wumbum.filter.FilterMode;
import com.wifosoft.wumbum.filter.MediaFilter;
import com.wifosoft.wumbum.helper.AlertDialogsHelper;
import com.wifosoft.wumbum.helper.MediaHelper;
import com.wifosoft.wumbum.helper.QueryAlbums;
import com.wifosoft.wumbum.helper.QueryHelper;
import com.wifosoft.wumbum.interfaces.IMediaClickListener;
import com.wifosoft.wumbum.model.Album;
import com.wifosoft.wumbum.model.Media;
import com.wifosoft.wumbum.sort.SortingMode;
import com.wifosoft.wumbum.sort.SortingOrder;
import com.wifosoft.wumbum.util.AnimationUtils;
import com.wifosoft.wumbum.util.DeviceUtils;
import com.wifosoft.wumbum.util.Measure;
import com.wifosoft.wumbum.util.MediaUtils;
import com.wifosoft.wumbum.util.StringUtils;
import com.wifosoft.wumbum.util.preferences.Prefs;
import com.wifosoft.wumbum.views.GridSpacingItemDecoration;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class AllMediaFragment extends BaseMediaGridFragment {

    public static final String TAG = "AllMediaFragment";
    private static final String BUNDLE_ALBUM = "album";

    @BindView(R.id.media) RecyclerView rv;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout refresh;

    private MediaAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;

    private Album album;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            album = getArguments().getParcelable(BUNDLE_ALBUM);
            return;
        }

        album = savedInstanceState.getParcelable(BUNDLE_ALBUM);
    }

    public static AllMediaFragment make(Album album) {
        AllMediaFragment fragment = new AllMediaFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_ALBUM, album);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!clearSelected())
            updateToolbar();
        setUpColumns();
    }

    private void reload() {
        loadAlbum(album);
    }

    private void loadAlbum(Album album) {
        this.album = album;
        adapter.setupFor(album);
        QueryHelper.getMedia(getContext(), album)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(media -> MediaFilter.getFilter(album.filterMode()).accept(media))
                .subscribe(media -> adapter.add(media),
                        throwable -> {
                            refresh.setRefreshing(false);
                            Log.wtf("asd", throwable);
                        },
                        () -> {
                            album.setCount(getCount());
                            if (getNothingToShowListener() != null)
                                getNothingToShowListener().changedNothingToShow(getCount() == 0);
                            refresh.setRefreshing(false);
                        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(BUNDLE_ALBUM, album);
        super.onSaveInstanceState(outState);
    }

    private IMediaClickListener listener;

    public void setListener(IMediaClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rv_media, container, false);
        ButterKnife.bind(this, v);

        int spanCount = columnsCount();
        spacingDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(spacingDecoration);
        rv.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        rv.setItemAnimator(
                AnimationUtils.getItemAnimator(
                        new LandingAnimator(new OvershootInterpolator(1f))
                ));

        adapter = new MediaAdapter(getContext(), album.settings.getSortingMode(), album.settings.getSortingOrder(), this);

        refresh.setOnRefreshListener(this::reload);
        rv.setAdapter(adapter);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reload();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpColumns();
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rv.getLayoutManager()).getSpanCount()) {
            ((GridLayoutManager) rv.getLayoutManager()).getSpanCount();
            rv.removeItemDecoration(spacingDecoration);
            spacingDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getContext()), true);
            rv.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
            rv.addItemDecoration(spacingDecoration);
        }
    }

    public int columnsCount() {
        return DeviceUtils.isPortrait(getResources())
                ? Prefs.getMediaColumnsPortrait()
                : Prefs.getMediaColumnsLandscape();
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
        return editMode() ? null : album.getName();
    }

    public SortingMode sortingMode() {
        return album.settings.getSortingMode();
    }

    public SortingOrder sortingOrder() {
        return album.settings.getSortingOrder();
    }

    private QueryAlbums db() {
        return QueryAlbums.getInstance(getContext().getApplicationContext());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.grid_media, menu);

        menu.findItem(R.id.select_all).setIcon(ThemeHelper.getToolbarIcon(getContext(), GoogleMaterial.Icon.gmd_select_all));
        menu.findItem(R.id.delete).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_delete)));
        menu.findItem(R.id.sharePhotos).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_share)));
        menu.findItem(R.id.sort_action).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_sort)));
        menu.findItem(R.id.filter_menu).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_filter_list)));

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
            menu.findItem(R.id.filter_menu).setVisible(false);
            menu.findItem(R.id.sort_action).setVisible(false);
        } else {
            menu.findItem(R.id.filter_menu).setVisible(true);
            menu.findItem(R.id.sort_action).setVisible(true);

            menu.findItem(R.id.ascending_sort_order).setChecked(sortingOrder() == SortingOrder.ASCENDING);
            switch (sortingMode()) {
                case NAME:  menu.findItem(R.id.name_sort_mode).setChecked(true); break;
                case SIZE:  menu.findItem(R.id.size_sort_mode).setChecked(true); break;
                case DATE: default:
                    menu.findItem(R.id.date_taken_sort_mode).setChecked(true); break;
                case NUMERIC:  menu.findItem(R.id.numeric_sort_mode).setChecked(true); break;
            }
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.all_media_filter:
                album.setFilterMode(FilterMode.ALL);
                item.setChecked(true);
                reload();
                return true;

            case R.id.video_media_filter:
                album.setFilterMode(FilterMode.VIDEO);
                item.setChecked(true);
                reload();
                return true;

            case R.id.image_media_filter:
                album.setFilterMode(FilterMode.IMAGES);
                item.setChecked(true);
                reload();
                return true;

            case R.id.gifs_media_filter:
                album.setFilterMode(FilterMode.GIF);
                item.setChecked(true);
                reload();
                return true;

            case R.id.sharePhotos:
                MediaUtils.shareMedia(getContext(), adapter.getSelected());
                return true;

            case R.id.set_as_cover:
                String path = adapter.getFirstSelected().getPath();
                album.setCover(path);
                db().setCover(album.getPath(), path);
                adapter.clearSelected();
                return true;

            case R.id.action_palette:
                Toast.makeText(getContext(), "Pallette not support", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.rename:
                final EditText editTextNewName = new EditText(getActivity());
                editTextNewName.setText(StringUtils.getPhotoNameByPath(adapter.getFirstSelected().getPath()));

                AlertDialog renameDialog = AlertDialogsHelper.getInsertTextDialog(((ThemedActivity) getActivity()), editTextNewName, R.string.rename_photo_action);

                renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), (dialog, which) -> {
                    if (editTextNewName.length() != 0) {
                        boolean b = MediaHelper.renameMedia(getActivity(), adapter.getFirstSelected(), editTextNewName.getText().toString());
                        if (!b) {
                            StringUtils.showToast(getActivity(), getString(R.string.rename_error));
                            //adapter.notifyDataSetChanged();
                        } else
                            adapter.clearSelected(); // Deselect media if rename successful
                    } else
                        StringUtils.showToast(getActivity(), getString(R.string.nothing_changed));
                });
                renameDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), (dialog, which) -> dialog.dismiss());
                renameDialog.show();
                return true;

            case R.id.select_all:
                if (adapter.getSelectedCount() == adapter.getItemCount())
                    adapter.clearSelected();
                else adapter.selectAll();
                return true;

            case R.id.name_sort_mode:
                adapter.changeSortingMode(SortingMode.NAME);
                QueryAlbums.getInstance(getContext()).setSortingMode(album.getPath(), SortingMode.NAME.getValue());
                album.setSortingMode(SortingMode.NAME);
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_mode:
                adapter.changeSortingMode(SortingMode.DATE);
                QueryAlbums.getInstance(getContext()).setSortingMode(album.getPath(), SortingMode.DATE.getValue());
                album.setSortingMode(SortingMode.DATE);
                item.setChecked(true);
                return true;

            case R.id.size_sort_mode:
                adapter.changeSortingMode(SortingMode.SIZE);
                QueryAlbums.getInstance(getContext()).setSortingMode(album.getPath(), SortingMode.SIZE.getValue());
                album.setSortingMode(SortingMode.SIZE);
                item.setChecked(true);
                return true;

            case R.id.numeric_sort_mode:
                adapter.changeSortingMode(SortingMode.NUMERIC);
                QueryAlbums.getInstance(getContext()).setSortingMode(album.getPath(), SortingMode.NUMERIC.getValue());
                album.setSortingMode(SortingMode.NUMERIC);
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_order:
                item.setChecked(!item.isChecked());
                SortingOrder sortingOrder = SortingOrder.fromValue(item.isChecked());
                adapter.changeSortingOrder(sortingOrder);
                QueryAlbums.getInstance(getContext()).setSortingOrder(album.getPath(), sortingOrder.getValue());
                album.setSortingOrder(sortingOrder);
                return true;

            case R.id.delete:

                showDeleteBottomSheet();
                return true;

            case R.id.affix:
                Toast.makeText(getContext() , "Affix not support", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteBottomSheet() {
        MediaUtils.deleteMedia(getContext(), adapter.getSelected(), getChildFragmentManager(),
                new ProgressBottomSheet.Listener<Media>() {
                    @Override
                    public void onCompleted() {
                        adapter.invalidateSelectedCount();
                    }

                    @Override
                    public void onProgress(Media item) {
                        adapter.removeSelectedMedia(item);
                    }
                });
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
    public void onItemSelected(int position) {
        if (listener != null) listener.onMediaClick(AllMediaFragment.this.album, adapter.getMedia(), position);
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
    public boolean clearSelected() {
        return adapter.clearSelected();
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        rv.setBackgroundColor(t.getBackgroundColor());
        adapter.refreshTheme(t);
        refresh.setColorSchemeColors(t.getAccentColor());
        refresh.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }
}
