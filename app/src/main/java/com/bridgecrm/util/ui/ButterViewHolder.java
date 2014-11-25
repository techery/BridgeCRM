package com.bridgecrm.util.ui;

import android.view.View;

import butterknife.ButterKnife;

public abstract class ButterViewHolder {

    public View root;

    void init(View root) {
        this.root = root;
        ButterKnife.inject(this, root);
    }
}
