package com.bridgecrm;

import android.app.Application;
import android.content.Context;

import com.bridgecrm.di.AppGraph;
import com.bridgecrm.manager.ParseManager;
import com.bridgecrm.manager.SyncManager;
import com.bridgecrm.manager.lifecycle.AppComponentCallback;
import com.bridgecrm.manager.lifecycle.MainLifecycleCallbacks;
import com.bridgecrm.util.app.ActivityHierarchyServer;

import javax.inject.Inject;

import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class App extends Application {

    private AppGraph component;

    @Inject
    ActivityHierarchyServer activityHierarchyServer;
    @Inject
    MainLifecycleCallbacks mainLifecycleCallbacks;
    @Inject
    AppComponentCallback appComponentCallback;

    @Inject
    SyncManager syncManager;
    @Inject
    ParseManager parseManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }

        buildComponentAndInject();

        registerActivityLifecycleCallbacks(activityHierarchyServer);
        registerActivityLifecycleCallbacks(mainLifecycleCallbacks);
        registerComponentCallbacks(appComponentCallback);

        parseManager.initialize();
    }

    ///////////////////////////////////////////////////////////////////////////
    // DI
    ///////////////////////////////////////////////////////////////////////////

    public void buildComponentAndInject() {
        component = AppComponent.Initializer.init(this);
        component.inject(this);
    }

    public AppGraph component() {
        return component;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static helper
    ///////////////////////////////////////////////////////////////////////////

    public static App from(Context context) { return (App) context.getApplicationContext(); }

    private static App instance;

    public static App instance() {
        return instance;
    }
}
