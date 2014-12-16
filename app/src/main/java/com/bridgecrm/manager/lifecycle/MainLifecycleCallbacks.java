package com.bridgecrm.manager.lifecycle;

import android.app.Activity;
import android.os.Bundle;

import com.bridgecrm.helper.location.LocationHelperManager;
import com.bridgecrm.ui.ActivityMediator;
import com.bridgecrm.util.app.SimpleActivityLifecycleCallbacks;

public class MainLifecycleCallbacks extends SimpleActivityLifecycleCallbacks {

    private final ActivityMediator activityMediator;
    private final LocationHelperManager locationManager;

    public MainLifecycleCallbacks(ActivityMediator activityMediator, LocationHelperManager locationManager) {
        this.activityMediator = activityMediator;
        this.locationManager = locationManager;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activityMediator.swapActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        super.onActivityStarted(activity);
        locationManager.onStart(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        super.onActivityStopped(activity);
        locationManager.onStop();
    }
}
