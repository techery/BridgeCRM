package com.bridgecrm.di;

import dagger.Module;
import dagger.Provides;

import static com.bridgecrm.manager.GoogleTrackingManager.GoogleAnalyticsCredentialsHolder;

@Module
public class CredentialsModule {

    @Provides
    GoogleAnalyticsCredentialsHolder provideGoogleAnalyticsCredentialsHolder() {
        return () -> new String[0];
    }
}
