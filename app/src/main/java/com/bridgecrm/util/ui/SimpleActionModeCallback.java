package com.bridgecrm.util.ui;

import android.view.ActionMode;
import android.view.Menu;

/**
 * Author: Aleksey Malevaniy, a.malevaniy@gmail.com
 */
public abstract class SimpleActionModeCallback<T> implements ActionMode.Callback {

    protected final T data;
    protected final int[] menus;

    public SimpleActionModeCallback(T data, int... menus) {
	this.data = data;
	this.menus = menus;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	for (int menuResource : menus)
	    mode.getMenuInflater().inflate(menuResource, menu);
	return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }
}
