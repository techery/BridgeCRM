package com.bridgecrm.di;

import com.bridgecrm.manager.lifecycle.AppComponentCallback;
import com.bridgecrm.manager.lifecycle.MainLifecycleCallbacks;
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
    MainLifecycleCallbacks provideLifeCycleCallbacks() {
        return new MainLifecycleCallbacks();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Component Callback
    ///////////////////////////////////////////////////////////////////////////

    @Provides
    AppComponentCallback provideComponentCallback(){
        return new AppComponentCallback();
    }
}
