package com.bridgecrm.helper;

import android.util.SparseArray;

public abstract class EnumStringHelper {

    ///////////////////////////////////////////////////////////////////////////
    // Enums-Strings dict
    ///////////////////////////////////////////////////////////////////////////

    protected SparseArray<Enum> enumStringResDict = new SparseArray<>();

    /**
     * Get string that represent Enum
     *
     * @param value enum
     * @return String binded to enum
     * @throws IllegalArgumentException if enum not binded to any string
     */
    public <U extends Enum<?>> int getEnumStringRes(U value) throws IllegalArgumentException {
        final int index = enumStringResDict.indexOfValue(value);
        if (index == -1) {
            throw new IllegalArgumentException("Can't provide string res for enum " + value + " – check if dict initialized properly");
        }
        return enumStringResDict.keyAt(index);
    }

}
