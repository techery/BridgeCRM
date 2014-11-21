package com.bridgecrm.di;

import android.app.Application;

import com.bridgecrm.App;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class AppModule {
  private final App app;

  public AppModule(App app) {
    this.app = app;
  }

  @Provides @Singleton Application provideApplication() {
    return app;
  }
}
