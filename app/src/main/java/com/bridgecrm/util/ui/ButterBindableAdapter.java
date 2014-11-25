package com.bridgecrm.util.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class ButterBindableAdapter<T, K extends ButterViewHolder> extends BindableAdapter<T> {

    private final List<T> items;
    private final Class<T> modelClass;
    private Class<K> holderClass;

    public ButterBindableAdapter(Context context, Class<T> modelClass, Class<K> holderClass, List<T> items) {
        super(context);
        this.items = items;
        this.modelClass = modelClass;
        this.holderClass = holderClass;
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

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        int layoutId = getViewLayoutId();
        View view = inflater.inflate(layoutId, container, false);
        K holder = buildButterViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(T item, int position, View view) {
        K butterViewHolder = (K) view.getTag();
        bindViewHolder(item, butterViewHolder, position);
    }

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

    protected abstract void bindViewHolder(T item, K holder, int position);

}
