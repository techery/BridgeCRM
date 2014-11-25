package com.bridgecrm.util.base;

import java.lang.reflect.Array;

public class ArrayUtils {

    public static <U> U[] removeIndex(U[] original, Class<U> originalClass, int element) {
        U[] n = (U[]) Array.newInstance(originalClass, original.length - 1);
        System.arraycopy(original, 0, n, 0, element);
        System.arraycopy(original, element + 1, n, element, original.length - element - 1);
        return n;
    }

}
