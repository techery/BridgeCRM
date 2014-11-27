package com.bridgecrm.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.bridgecrm.App;
import com.bridgecrm.R;
import com.bridgecrm.manager.SessionManager;
import com.bridgecrm.ui.ActivityMediator;

import javax.inject.Inject;

import me.tatarka.rxloader.RxLoaderManager;
import me.tatarka.rxloader.RxLoaderManagerCompat;
import me.tatarka.rxloader.RxLoaderObserver;
import timber.log.Timber;


public abstract class BaseSessionActivity extends BaseActivity {

    @Inject
    SessionManager sessionManager;
    @Inject
    ActivityMediator activityMediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(this).component().inject(this);
        //
        RxLoaderManager rxLoaderManager = RxLoaderManagerCompat.get(this);
        rxLoaderManager.create("logout", sessionManager.getLogoutPipe(), new RxLoaderObserver<Boolean>() {
                @Override
                public void onNext(Boolean byUser) {
                    if (!byUser) {
                        Timber.w("Session was terminated somehow");
                    }
                    activityMediator.showAuth();
                    finish();
                }
            }
        ).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                sessionManager.logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}