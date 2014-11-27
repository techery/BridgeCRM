package com.bridgecrm.ui;

import android.content.Context;
import android.content.Intent;

import com.bridgecrm.ui.activity.AuthActivity;
import com.bridgecrm.ui.activity.MainActivity;
import com.bridgecrm.util.app.BaseActivityMediator;


public class ActivityMediator extends BaseActivityMediator {
    /**
     * Initiate mediator with context to start activities. If context is not
     * instance of Activity (e.g. service, receiver, etc), activities will be
     * started with {@link Intent#FLAG_ACTIVITY_NEW_TASK}
     *
     * @param context
     */
    public ActivityMediator(Context context) {
        super(context);
    }

    public void showAuth() {
        startActivity(AuthActivity.class);
    }

    public void showDashboard() {
        startActivity(MainActivity.class, null, Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
}
