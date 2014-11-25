package com.bridgecrm.util.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ButterCursorAdapter<T, K extends ButterViewHolder> extends CursorAdapter {

    protected final Context context;
    private final LayoutInflater inflater;
    private final Class<T> modelClass;
    private Class<K> holderClass;

    public ButterCursorAdapter(Context context, Class<T> modelClass, Class<K> holderClass) {
        super(context, null, false);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.modelClass = modelClass;
        this.holderClass = holderClass;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = getViewLayoutId();
        View view = inflater.inflate(layoutId, parent, false);
        K holder = buildButterViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        T item = getItemFrom(cursor);
        K butterViewHolder = (K) view.getTag();
        bindViewHolder(item, butterViewHolder, cursor);
    }

    protected T getItemFrom(Cursor cursor) { throw new IllegalStateException("Must be implemented to convert cursor to item"); }

    protected abstract int getViewLayoutId();

    private K buildButterViewHolder(View view) {
        try {
            final K holder = holderClass.newInstance();
            holder.init(view);
            return holder;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    };

    protected abstract void bindViewHolder(T item, K holder, Cursor cursor);

}
