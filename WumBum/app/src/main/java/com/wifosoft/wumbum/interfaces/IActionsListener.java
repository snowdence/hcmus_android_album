package com.wifosoft.wumbum.interfaces;

public interface IActionsListener {

    /**
     * Used when the user clicks on an item.
     *
     * @param position The position that was clicked.
     */
    void onItemSelected(int position);

    /**
     * Use to toggle Select Mode states
     *
     * @param selectMode Whether we want to be in select mode or not.
     */
    void onSelectMode(boolean selectMode);

    /**
     * Used to notify listeners about selection counts.
     *
     * @param selectionCount The number of selected items
     * @param totalCount     The number of total items
     */
    void onSelectionCountChanged(int selectionCount, int totalCount);
}
