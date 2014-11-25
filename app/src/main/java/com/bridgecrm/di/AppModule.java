package com.bridgecrm.di;

import android.app.Application;
import android.content.Context;

import com.bridgecrm.App;
import com.bridgecrm.di.qualifier.ForApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class AppModule {
    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return app;
    }

    @Provides
    @Singleton
    @ForApplication
    Context provideContextWithQualifier() {
        return app;
    }
}
