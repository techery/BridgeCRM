package com.bridgecrm.manager;

import android.content.Context;

import com.bridgecrm.api.AccountApi;

public class SessionManager {

    private final Context context;
    private final PreferenceWrapper preferences;
    private final TrackingWrapper trackingWrapper;
    private final AccountApi accountApi;

    public SessionManager(Context context, PreferenceWrapper preferences, TrackingWrapper trackingWrapper, AccountApi accountApi) {
        this.context = context;
        this.preferences = preferences;
        this.trackingWrapper = trackingWrapper;
        this.accountApi = accountApi;
    }

    public void tryLogin(String user, String password) {

    }


}
