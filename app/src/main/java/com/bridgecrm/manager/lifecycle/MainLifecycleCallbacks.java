package com.bridgecrm.manager.lifecycle;

import android.app.Activity;
import android.os.Bundle;

import com.bridgecrm.ui.ActivityMediator;
import com.bridgecrm.util.app.SimpleActivityLifecycleCallbacks;

public class MainLifecycleCallbacks extends SimpleActivityLifecycleCallbacks {

    private final ActivityMediator activityMediator;

    public MainLifecycleCallbacks(ActivityMediator activityMediator) {
        this.activityMediator = activityMediator;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activityMediator.swapActivity(activity);
    }
}
