package com.bridgecrm.di;

import com.bridgecrm.helper.location.LocationHelperManager;
import com.bridgecrm.manager.lifecycle.AppComponentCallback;
import com.bridgecrm.manager.lifecycle.MainLifecycleCallbacks;
import com.bridgecrm.ui.ActivityMediator;
import com.bridgecrm.util.app.ActivityHierarchyServer;

import dagger.Module;
import dagger.Provides;

@Module
public class LifecycleModule {

    @Provides
    ActivityHierarchyServer provideActivityHierarchyServer() {
        return ActivityHierarchyServer.NONE;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Activity LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Provides
    MainLifecycleCallbacks provideLifeCycleCallbacks(ActivityMediator activityMediator, LocationHelperManager locationHelperManager) {
        return new MainLifecycleCallbacks(activityMediator, locationHelperManager);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Component Callback
    ///////////////////////////////////////////////////////////////////////////

    @Provides
    AppComponentCallback provideComponentCallback(){
        return new AppComponentCallback();
    }
}
