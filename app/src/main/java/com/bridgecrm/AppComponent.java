package com.bridgecrm;

import com.bridgecrm.di.AppGraph;
import com.bridgecrm.di.AppModule;
import com.bridgecrm.di.ManagerModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * The core debug component for u2020 applications
 */
@Singleton
@Component(modules = {AppModule.class, ManagerModule.class /*, DebugUiModule.class, DebugDataModule.class*/})
public interface AppComponent extends AppGraph {

    /**
     * An initializer that creates the graph from an application.
     */
    final static class Initializer {

        static AppGraph init(App app) {
            return Dagger_AppComponent.builder()
                .appModule(new AppModule(app))
                .build();
        }

        private Initializer() {} // No instances.
    }
}
