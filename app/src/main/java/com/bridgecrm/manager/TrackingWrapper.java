package com.bridgecrm.manager;


import com.bridgecrm.helper.analytics.VirtualScreenView;

/**
 * Wrapper manager provides common tracking actions.
 */
public class TrackingWrapper {

    private final GoogleTrackingManager googleTracker;

    public TrackingWrapper(GoogleTrackingManager googleTracker) {
        this.googleTracker = googleTracker;
        init();
    }

    /** One-time initialization, needed for some trackers to work properly */
    private void init() { }

    /** Track install, additionally if needed */
    public void trackInstall() {
        googleTracker.trackInstall();
    }

    /** Track registration */
    public void trackRegistration(boolean isSuccess) {
        if (isSuccess) {
            googleTracker.trackScreenView(VirtualScreenView.REGISTRATION_SUCCESS);
        } else {
            googleTracker.trackScreenView(VirtualScreenView.REGISTRATION_FAIL);
        }
    }

    /** Track login */
    public void trackLogin(boolean isSuccess) {
        if (isSuccess) {
            googleTracker.trackScreenView(VirtualScreenView.LOGIN_SUCCESS);
        } else {
            googleTracker.trackScreenView(VirtualScreenView.LOGIN_FAIL);
        }
    }

    /** Tracks additional user-related data */
    public void trackUserSession(String userId) {
        googleTracker.trackUserId(userId);
    }

    /** Reset extra data to start new session */
    public void trackNewSession() {
        googleTracker.trackNewSession();
    }

    /** Track purchasing of 1 item within single transaction */
    public void trackPurchase(String orderId, String name, String sku, double price, String currencyCode) {
        googleTracker.trackPurchase(orderId, name, sku, price, currencyCode);
    }

}
