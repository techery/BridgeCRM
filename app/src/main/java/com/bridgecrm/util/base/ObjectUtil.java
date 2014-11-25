package com.bridgecrm.util.base;

import android.text.TextUtils;

public class ObjectUtil {

    public static boolean getPrimitive(Boolean value) {
        return value == null ? false : value.booleanValue();
    }

    public static long getPrimitive(Long value) {
        return value == null ? 0 : value;
    }

    public static int getPrimitive(Integer value) {
        return value == null ? 0 : value;
    }

    /** @return <code>true</code> if <code>newData</code> is not null and doesn't equal <code>target</code> */
    public static <U extends Object, V extends U> boolean isObjectUpdatable(U target, V newData) {
        return (target == null && newData != null) || (target != null && newData != null && !target.equals(newData));
    }

    /** @return <code>true</code> if <code>newData</code> is not null (and not empty) and doesn't equal <code>target</code> */
    public static <U extends CharSequence, V extends U> boolean isObjectUpdatable(U target, V newData) {
        return (target == null && !TextUtils.isEmpty(newData)) || (target != null && newData != null && !target.equals(newData));
    }
}
