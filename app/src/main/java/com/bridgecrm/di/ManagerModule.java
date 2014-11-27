package com.bridgecrm.di;

import android.content.Context;

import com.bridgecrm.api.AccountApi;
import com.bridgecrm.manager.GoogleTrackingManager;
import com.bridgecrm.manager.PreferenceWrapper;
import com.bridgecrm.manager.SessionManager;
import com.bridgecrm.manager.SyncManager;
import com.bridgecrm.manager.TrackingWrapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.bridgecrm.manager.GoogleTrackingManager.GoogleAnalyticsCredentialsHolder;

@Module(includes = {ApiModule.class, CredentialsModule.class})
public class ManagerModule {

    ///////////////////////////////////////////////////////////////////////////
    // Persistence
    ///////////////////////////////////////////////////////////////////////////

    @Provides
    @Singleton
    PreferenceWrapper providePreferenceWrapper(Context context) {
        return new PreferenceWrapper(context);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Api
    ///////////////////////////////////////////////////////////////////////////

    /** See {@link ApiModule} */

    ///////////////////////////////////////////////////////////////////////////
    // Main managers
    ///////////////////////////////////////////////////////////////////////////

    @Provides
    @Singleton
    SyncManager provideSyncManager(Context context, SessionManager sessionManager) {
        return new SyncManager(context, sessionManager);
    }

    @Provides
    @Singleton
    SessionManager provideSessionManager(Context context, AccountApi accountApi, PreferenceWrapper preferenceWrapper, TrackingWrapper trackingWrapper) {
        return new SessionManager(context, preferenceWrapper, trackingWrapper, accountApi);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tracking
    ///////////////////////////////////////////////////////////////////////////

    @Provides
    @Singleton
    TrackingWrapper provideTrackingWrapper(GoogleTrackingManager googleTrackingManager) {
        return new TrackingWrapper(googleTrackingManager);
    }

    @Provides
    @Singleton
    GoogleTrackingManager provideGoogleTrackingManager(Context context, GoogleAnalyticsCredentialsHolder credentials) {
        return new GoogleTrackingManager(context, credentials);
    }
}
