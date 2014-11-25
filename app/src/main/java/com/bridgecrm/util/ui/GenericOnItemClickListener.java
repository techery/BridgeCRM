package com.bridgecrm.util.ui;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

public abstract class GenericOnItemClickListener<T> implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListAdapter adapter = (ListAdapter) parent.getAdapter();
        final Object item = adapter.getItem(position);
        if (item != null) {
            onGenericItemClicked(parent, view, position, (T) item);
        }
    }

    public abstract void onGenericItemClicked(AdapterView<?> parent, View view, int position, T item);
}