package com.bridgecrm.util.ui;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

public abstract class GenericOnItemSelectedListener<T> implements AdapterView.OnItemSelectedListener {

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ListAdapter adapter = (ListAdapter) parent.getAdapter();
        final Object item = adapter.getItem(position);
        if (item != null) {
            onGenericItemSelected(parent, view, position, (T) item);
        }
    }

    public abstract void onGenericItemSelected(AdapterView<?> parent, View view, int position, T item);
}