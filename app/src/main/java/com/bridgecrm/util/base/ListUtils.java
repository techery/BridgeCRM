package com.bridgecrm.util.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Simple helper for list-related common operations.
 */
public class ListUtils {

    private ListUtils() {
    }

    public static <U> void appendIfUnique(List<U> source, U modifier) {
        if (!source.contains(modifier)) {
            source.add(modifier);
        }
    }

    public static <U> void appendUnique(List<U> source, List<U> modifier) {
        List<U> uniqueItems = new ArrayList<U>();
        uniqueItems.addAll(modifier);
        uniqueItems.removeAll(source);
        source.addAll(uniqueItems);
    }

    public static <U> void removeMissing(List<U> source, List<U> modifier) {
        List<U> missingItems = new ArrayList<U>();
        missingItems.addAll(source);
        missingItems.removeAll(modifier);
        source.removeAll(missingItems);
    }

    public static List replace(List source, List modifier) {
        source.removeAll(modifier);
        source.addAll(modifier);
        return source;
    }

    public static <U> List<U> replace(List<U> source, U modifier) {
        source.remove(modifier);
        source.add(modifier);
        return source;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static <U> List<U> filter(List<U> list, Filter<U> filter) {
        List<U> result = new ArrayList<>();
        Iterator<U> iterator = list.iterator();
        while (iterator.hasNext()) {
            U next = iterator.next();
            if (filter.isAllowed(next)) {
                result.add(next);
            }
        }
        return result;
    }

    public interface Filter<U> {

        public boolean isAllowed(U value);
    }
}
