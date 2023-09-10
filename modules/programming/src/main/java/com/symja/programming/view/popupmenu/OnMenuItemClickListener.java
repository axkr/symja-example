package com.symja.programming.view.popupmenu;

/**
 * Interface responsible for receiving menu item click events if the items
 * themselves do not have individual item click listeners.
 */
public interface OnMenuItemClickListener {
    /**
     * This method will be invoked when a menu item is clicked if the item
     * itself did not already handle the event.
     *
     * @param item the menu item that was clicked
     * @return {@code true} if the event was handled, {@code false}
     * otherwise
     */
    boolean onMenuItemClick(MenuItem item);
}