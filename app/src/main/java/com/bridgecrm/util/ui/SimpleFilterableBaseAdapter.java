package com.bridgecrm.util.ui;

import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleFilterableBaseAdapter<T> extends SimpleBaseAdapter<T> implements Filterable {

    protected final List<T> rawItems;
    private CharSequence savedConstraint;

    public SimpleFilterableBaseAdapter(Context context, List<T> items) {
        super(context, items);
        rawItems = items;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                savedConstraint = constraint;
                List<T> itemsToFilter;
                itemsToFilter = new ArrayList<T>(rawItems);
                doFiltering(itemsToFilter, constraint);
                FilterResults results = new FilterResults();
                results.values = itemsToFilter;
                results.count = itemsToFilter.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items = (List<T>) results.values;
                if (results.count > 0)
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
            }
        };
    }

    protected abstract void doFiltering(List<T> itemsToFilter, CharSequence constraint);

}
