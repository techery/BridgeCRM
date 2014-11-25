package com.bridgecrm.manager.lifecycle;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;

import com.bridgecrm.App;
import com.bridgecrm.manager.lifecycle.event.ApplicationLeftEvent;
import com.halfbit.tinybus.TinyBus;

public class AppComponentCallback implements ComponentCallbacks2 {

    @Override
    public void onTrimMemory(int level) {
        boolean probablyAppLeaving = level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN;
        if (probablyAppLeaving) {
            TinyBus.from(App.instance()).post(new ApplicationLeftEvent());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {

    }
}
