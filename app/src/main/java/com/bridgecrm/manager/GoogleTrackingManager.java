package com.bridgecrm.manager;

import android.content.Context;
import android.text.format.DateUtils;

import com.bridgecrm.BuildConfig;
import com.bridgecrm.helper.analytics.CustomUncaughtExceptionHandler;
import com.bridgecrm.util.base.StringUtils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.HitBuilders.AppViewBuilder;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.google.android.gms.analytics.HitBuilders.EventBuilder;

public class GoogleTrackingManager {

    private HashMap<String, Tracker> trackers;
    /** Play services docs say it would be deprecated, and works only if no Play Services are available */
    private static final int DISPATCH_PERIOD = (int) (DateUtils.MINUTE_IN_MILLIS / 1000); // seconds

    /** Real-time usage constructor */
    public GoogleTrackingManager(Context context, GoogleAnalyticsCredentialsHolder holder) {
        this(context, GoogleAnalytics.getInstance(context), holder);
    }

    /**
     * This constructor is used for testing only.<br>
     * While it's safe to used it, for simplicity of real-time usage pick {@link GoogleTrackingManager#GoogleTrackingManager(Context, GoogleAnalyticsCredentialsHolder)}
     */
    public GoogleTrackingManager(Context context, GoogleAnalytics googleAnalytics, GoogleAnalyticsCredentialsHolder holder) {
        String[] analyticKeys = holder.getTrackersKeys();
        if (analyticKeys == null) {
            throw new IllegalArgumentException(
                "Error analyticKeys can not be null!" +
                    "Please, provide proper credentials holder"
            );
        } else if (analyticKeys.length == 0) {
            Timber.w("Analytics keys are empty, no tracking involved");
            return;
        }
        // init GA
        googleAnalytics.setLocalDispatchPeriod(DISPATCH_PERIOD);
        googleAnalytics.getLogger().setLogLevel(BuildConfig.DEBUG ? Logger.LogLevel.VERBOSE : Logger.LogLevel.ERROR);
        googleAnalytics.setDryRun(BuildConfig.DEBUG ? true : false);
        // init trackers
        trackers = new HashMap<>(analyticKeys.length);
        for (String key : analyticKeys) {
            Tracker tracker = googleAnalytics.newTracker(key);
            trackers.put(key, tracker);
        }
        // support uncaught exception handling
        Thread.UncaughtExceptionHandler exceptionHandler = new CustomUncaughtExceptionHandler(
            context,
            trackers.values(),
            Thread.getDefaultUncaughtExceptionHandler()
        );
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tracking methods
    ///////////////////////////////////////////////////////////////////////////

    /** Track install via screen view */
    public void trackInstall() {

    }

    /**
     * Set and track screen to all available trackers. Screen name won't be normalized,
     * as it's often too complex and should be prepared outside. '/' is added as prefix if not exists
     */
    public void trackScreenView(Enum<?> screen) {
        trackScreenView(screen.toString());
    }

    /**
     * Set and track screen to all available trackers. Screen name won't be normalized,
     * as it's often too complex and should be prepared outside. '/' is added as prefix if not exists
     */
    public void trackScreenView(String key) {
        String screenName = formatScreenView(key);
        for (Tracker t : trackers.values()) {
            t.setScreenName(screenName);
            t.send(new AppViewBuilder().build());
        }
    }

    /** Reset screen view. Call this when leaving some screen to be sure */
    public void resetScreenView() {
        for (Tracker t : trackers.values()) {
            t.setScreenName(null);
        }
    }

    /** Track action to all available trackers */
    public void trackEvent(Enum<?> category, Enum<?> action) {
        sendItem(
            new EventBuilder(
                normalizeName(category),
                normalizeName(action)
            ).build()
        );
    }

    /** Set {@link Account#oid} to bind all further tracks */
    public void trackUserId(String oid) {
        for (Tracker t : trackers.values()) {
            t.set("&uid", oid);
        }
    }

    /** Reset session */
    public void trackNewSession() {
        sendItem(new AppViewBuilder().setNewSession().build());
    }

    /** Track purchasing of 1 item within single transaction */
    public void trackPurchase(String orderId, String name, String sku, double price, String currencyCode) {
        sendItem(
            new HitBuilders.TransactionBuilder()
                .setTransactionId(orderId)
                .setRevenue(price)
                .setCurrencyCode(currencyCode)
                .build()
        );
        sendItem(
            new HitBuilders.ItemBuilder()
                .setTransactionId(orderId)
                .setName(name)
                .setSku(sku)
                .setPrice(price)
                .setCurrencyCode(currencyCode)
                .setQuantity(1l)
                .build()
        );
    }

    ///////////////////////////////////////////////////////////////////////////
    // shared
    ///////////////////////////////////////////////////////////////////////////

    private void sendItem(Map<String, String> params) {
        for (Tracker tracker : trackers.values()) {
            tracker.send(params);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Misc helpers
    ///////////////////////////////////////////////////////////////////////////

    public static String formatScreenView(String screenView) {
        return screenView.startsWith("/") ? screenView : "/" + screenView;
    }

    public static String normalizeName(Enum<?> e) {
        return normalizeName(StringUtils.enumToSentence(e));
    }

    public static String normalizeName(String trackEntity) {
        String titleCase = StringUtils.titleCase(trackEntity);
        String titleCaseWithNoSpaces = StringUtils.removeSpaces(titleCase);
        return titleCaseWithNoSpaces;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Additional
    ///////////////////////////////////////////////////////////////////////////

    public interface GoogleAnalyticsCredentialsHolder {

        public String[] getTrackersKeys();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Testing purpose
    ///////////////////////////////////////////////////////////////////////////


    public HashMap<String, Tracker> getTrackers() {
        return trackers;
    }
}
