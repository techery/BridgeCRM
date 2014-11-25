package com.bridgecrm.util.ui;

import android.view.View;
import android.widget.AdapterView;

public abstract class ActivatedGenericOnItemSelectedListener<T> extends GenericOnItemSelectedListener<T> {

    @Override
    public void onGenericItemSelected(AdapterView<?> parent, View view, int position, T item) {
        // assume 0 – is nothing selected
        if (position > 0) {
            parent.setActivated(true);
            if (view != null) {
                view.setActivated(true);
            }
        }
    }

}
