package com.bridgecrm.util.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class SimpleBaseAdapter<T> extends BaseAdapter {

    protected final Context context;
    protected final LayoutInflater inflater;
    protected List<T> items;

    public SimpleBaseAdapter(Context context, List<T> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}