package com.bridgecrm.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.bridgecrm.App;
import com.bridgecrm.manager.SessionManager;
import com.bridgecrm.ui.ActivityMediator;

import javax.inject.Inject;

/**
 * Redirects first launch to auth or main flow.
 */
public class LaunchActivity extends Activity {

    @Inject
    SessionManager sessionManager;
    @Inject
    ActivityMediator activityMediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(this).component().inject(this);
        //
        if (sessionManager.isSessionExist()) {
            activityMediator.showDashboard();
        } else {
            activityMediator.showAuth();
        }
        finish();
    }

}
