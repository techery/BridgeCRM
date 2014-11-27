package com.bridgecrm.manager;


import android.content.Context;

import timber.log.Timber;

public class SyncManager {

    private final Context context;
    private final SessionManager sessionManager;

    public SyncManager(Context context, SessionManager sessionManager) {
        this.context = context;
        this.sessionManager = sessionManager;
        connectToSubjects();
    }

    private void connectToSubjects() {
        sessionManager.getAuthPipe().subscribe(result -> Timber.d("Sync is ready to start"));
    }

}
