package com.bridgecrm.util.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bridgecrm.helper.EnumStringHelper;


/**
 * Simple adapter for list of TextView items
 *
 * @param <T> implements StringResHolder, instance of Enum
 */
public class EnumWithStringAdapter<T extends Enum> extends BindableAdapter<T> {

    private final T[] items;
    private final int resource;
    private final EnumStringHelper enumStringHelper;
    private int dropDownResource;

    /**
     * Simple adapter for list of strings by res id
     *
     * @param context
     * @param items
     * @param resource TextView widget
     *                 @param enumStringHelper
     */
    public <U extends EnumStringHelper> EnumWithStringAdapter(Context context, T[] items, int resource, U enumStringHelper) {
        this(context, items, resource, resource, enumStringHelper);
    }

    public <U extends EnumStringHelper> EnumWithStringAdapter(Context context, T[] items, int resource, int dropDownResource, U enumStringHelper) {
        super(context);
        this.items = items;
        this.resource = resource;
        this.dropDownResource = dropDownResource;
        this.enumStringHelper = enumStringHelper;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        return inflater.inflate(resource, container, false);
    }

    @Override
    public View newDropDownView(LayoutInflater inflater, int position, ViewGroup container) {
        return inflater.inflate(dropDownResource, container, false);
    }

    @Override
    public void bindView(T item, int position, View view) {
        TextView textView = (TextView) view;
        textView.setText(enumStringHelper.getEnumStringRes(item));
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public T getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
