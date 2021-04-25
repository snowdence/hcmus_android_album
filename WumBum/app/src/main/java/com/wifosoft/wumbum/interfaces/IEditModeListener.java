package com.wifosoft.wumbum.interfaces;

import androidx.annotation.Nullable;
import android.view.View;

public interface IEditModeListener {

    /**
     * Propagate Edit Mode switches to listeners.
     *
     * @param editMode Whether we are in Edit Mode or not.
     * @param selected The number of items selected.
     * @param total    The total number of items.
     * @param listener The listener for Toolbar Back Button presses.
     * @param title    The Toolbar's title.
     */
    void changedEditMode(boolean editMode, int selected, int total, @Nullable View.OnClickListener listener, @Nullable String title);

    /**
     * Propagate the selected item count to listeners.
     *
     * @param count The number of items selected.
     * @param total The total number of items.
     */
    void onItemsSelected(int count, int total);
}
