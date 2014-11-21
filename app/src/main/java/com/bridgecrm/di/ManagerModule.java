package com.bridgecrm.di;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;

import com.bridgecrm.util.app.ActivityHierarchyServer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ManagerModule {

    @Provides
    ActivityHierarchyServer provideActivityHierarchyServer() {
        return ActivityHierarchyServer.NONE;
    }

    @Provides @Singleton
    LocationManager provideLocationManager(Application app) {
        return (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
    }
}
