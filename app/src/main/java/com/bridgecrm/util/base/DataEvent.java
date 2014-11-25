package com.bridgecrm.util.base;

/**
 * Author: Aleksey Malevaniy, a.malevaniy@gmail.com
 */
public abstract class DataEvent<T> {

    public final T data;

    public DataEvent(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " => " + data;
    }
}
