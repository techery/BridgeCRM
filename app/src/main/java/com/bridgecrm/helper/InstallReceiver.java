package com.bridgecrm.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bridgecrm.App;
import com.bridgecrm.manager.TrackingWrapper;
import com.google.android.gms.analytics.CampaignTrackingReceiver;

import javax.inject.Inject;

import timber.log.Timber;

public class InstallReceiver extends BroadcastReceiver {

    @Inject
    TrackingWrapper trackingWrapper;
    CampaignTrackingReceiver campaignTrackingReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive with intent = %s", intent);
        initTrackers(context);
        trackingWrapper.trackInstall();
        campaignTrackingReceiver.onReceive(context, intent);
    }

    private void initTrackers(Context context) {
        if (trackingWrapper == null) { // for testing purpose
            App.from(context).component().inject(this);
            campaignTrackingReceiver = new CampaignTrackingReceiver();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Testing purpose
    ///////////////////////////////////////////////////////////////////////////

    public InstallReceiver() {
        super();
    }

    public InstallReceiver(TrackingWrapper trackingWrapper, CampaignTrackingReceiver campaignTrackingReceiver) {
        this.trackingWrapper = trackingWrapper;
        this.campaignTrackingReceiver = campaignTrackingReceiver;
    }
}
