package com.bridgecrm.helper.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.bridgecrm.App;
import com.bridgecrm.helper.location.event.LocationProviderNeededEvent;
import com.bridgecrm.helper.location.event.LocationUpdateEvent;
import com.bridgecrm.helper.location.event.LocationUpdateFailedEvent;
import com.bridgecrm.util.base.ListUtils;
import com.bridgecrm.util.location.geocoder.AddressComponent;
import com.bridgecrm.util.location.geocoder.AddressExtraKeys;
import com.bridgecrm.util.location.geocoder.GeocodeResults;
import com.bridgecrm.util.location.geocoder.Result;
import com.bridgecrm.util.play.event.GooglePlayServicesRetryEvent;
import com.bridgecrm.util.play.event.GooglePlayServicesUnavailable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.GsonBuilder;
import com.halfbit.tinybus.TinyBus;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static android.provider.Settings.Secure;

public class LocationHelperManager implements GooglePlayServicesClient.ConnectionCallbacks,
    GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private final Context context;
    private final LocationClient locationClient;

    private Location cachedLocation;
    private LocationType pendingRequest;

    private RequestTimeoutHandler timeoutHandler;
    private static final int REQUEST_EXPIRATION = 30; // seconds

    public enum LocationType {
        COARSE, FINE
    }

    /**
     * Define a request code to send to Google Play services.
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public LocationHelperManager(Context context) {
        this.context = context;
        locationClient = new LocationClient(context, this, this);
        timeoutHandler = new RequestTimeoutHandler();
        checkServiceConnected();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cycle related
    ///////////////////////////////////////////////////////////////////////////

    public void onStart() {
        locationClient.connect();
    }

    public void onStop() {
        locationClient.disconnect();
    }

    public boolean handleOnActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /* If the result code is Activity.RESULT_OK, try to connect again */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                    /* Try the request again */
                        if (!locationClient.isConnected() && !locationClient.isConnecting()) {
                            locationClient.connect();
                        }
                        break;
                }
                return true;
            default:
                return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Main logic
    ///////////////////////////////////////////////////////////////////////////

    /** Check if google play services is ready, and {@link LocationClient} is connected */
    private boolean checkServiceConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Timber.d("Google Play services is available.");
            return locationClient.isConnected();
        } else {
            Timber.d("Google Play services is unavailable.");
            // Google Play services was not available for some reason
            // Get the error code
            TinyBus.from(context).post(new GooglePlayServicesUnavailable(resultCode));
            return false;
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Timber.d("LocationClient is connected");
        cachedLocation = locationClient.getLastLocation();
        if (pendingRequest != null) {
            askLocation(pendingRequest);
        }
    }

    @Override
    public void onDisconnected() {
        Timber.d("LocationClient is disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            TinyBus.from(context).post(new GooglePlayServicesRetryEvent(connectionResult));
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            TinyBus.from(context).post(new GooglePlayServicesUnavailable(connectionResult.getErrorCode()));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Timber.d("Location changed: %s", location);
        pendingRequest = null;
        cachedLocation = location;
        timeoutHandler.trackRequestProcessed();
        TinyBus.from(context).post(new LocationUpdateEvent(location));
    }

    /** Get last cached location or last location from {@link LocationClient} if is connected or null otherwise */
    public Location getCachedLocation() {
        return cachedLocation != null ? cachedLocation : locationClient.isConnected() ? locationClient.getLastLocation() : null;
    }

    /**
     * Ask {@link LocationClient} to respond with location of proper {@link LocationType}.
     * If client is not connected, will try to connect and re-run request. If no provider available for location type, will post {@link LocationProviderNeededEvent}.
     * If client is ok, will respond with {@link LocationUpdateEvent}, see {@link #onLocationChanged(Location)}.
     */
    public void askLocation(LocationType type) {
        LocationRequest request = buildLocationRequest(type);
        if (checkServiceConnected()) {
            if (providerAvailable(context, type)) {
                requestLocationUpdate(request);
            } else {
                Timber.d("Provider for type %s is not available", type);
                TinyBus.from(context).post(new LocationProviderNeededEvent(type));
            }
        } else {
            pendingRequest = type;
            Timber.d("Location client is not connected");
            if (!locationClient.isConnecting()) {
                Timber.d("Connecting...");
                locationClient.connect();
            }
        }
    }

    /**
     * Build default request for only one update with expiration duration of {@link #REQUEST_EXPIRATION}
     *
     * @param type decides priority:
     *             {@link LocationRequest#PRIORITY_LOW_POWER} for {@link LocationType#COARSE} and
     *             {@link LocationRequest#PRIORITY_HIGH_ACCURACY} for {@link LocationType#FINE}
     */
    public LocationRequest buildLocationRequest(LocationType type) {
        int priority = type == LocationType.COARSE ? LocationRequest.PRIORITY_LOW_POWER : LocationRequest.PRIORITY_HIGH_ACCURACY;
        return LocationRequest.create()
            .setPriority(priority)
            .setNumUpdates(1)
            .setExpirationDuration(REQUEST_EXPIRATION * DateUtils.SECOND_IN_MILLIS);
    }

    /** Ask {@link LocationClient} for update, start request timeout handler */
    private void requestLocationUpdate(LocationRequest request) {
        Timber.d("Requesting location with: %s", request);
        locationClient.requestLocationUpdates(request, this);
        timeoutHandler.trackRequestSent(request.getExpirationTime() - SystemClock.elapsedRealtime());
    }

    /**
     * Check if provider is available via settings or location manager
     *
     * @param type of location to look for provider
     */
    public static boolean providerAvailable(Context context, LocationType type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode;
            try {
                locationMode = Secure.getInt(context.getContentResolver(), Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                locationMode = Secure.LOCATION_MODE_OFF;
                e.printStackTrace();
            }
            boolean isOn = locationMode != Secure.LOCATION_MODE_OFF;
            boolean isCoarseModeMatches = type == LocationType.COARSE && locationMode == Secure.LOCATION_MODE_BATTERY_SAVING;
            boolean isFineModeMatches = type == LocationType.FINE && (locationMode == Secure.LOCATION_MODE_HIGH_ACCURACY || locationMode == Secure.LOCATION_MODE_SENSORS_ONLY);
            return isOn && (isCoarseModeMatches || isFineModeMatches);
        } else {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            String providerName = type == LocationType.FINE ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
            return providerName != null && locationManager.isProviderEnabled(providerName);
        }
    }

    /** Simple handler to signal timeout for location request */
    public static class RequestTimeoutHandler extends Handler {

        private static final int REQUEST_NO_ANSWER = 0;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REQUEST_NO_ANSWER:
                    Timber.w("Location request timeout");
                    TinyBus.from(App.instance()).post(new LocationUpdateFailedEvent());
            }
        }

        /** Start watch for request expiration */
        public void trackRequestSent(long requestExpiration) {
            long timeoutDelay = requestExpiration + 1000;
            Timber.d("Request is sent, starting timeout handler for %d ms", timeoutDelay);
            removeMessages(REQUEST_NO_ANSWER);
            sendEmptyMessageDelayed(REQUEST_NO_ANSWER, timeoutDelay);
        }

        /** Reset expiration handling, in case it's successful */
        public void trackRequestProcessed() {
            removeMessages(REQUEST_NO_ANSWER);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Misc helpers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Make a request to {@link Geocoder} to provide {@link Address}.
     * Should be run from background.
     *
     * @deprecated 'cause it has no way to get short names for region and subregion...
     */
    @Deprecated
    public static Address getAddressViaGeocoder(Context context, double latitude, double longitude) {
        Timber.d("Requesting reverse geocoding with Geocoder lib");
        Address address = null;
        if (Geocoder.isPresent()) {
            try {
                List<Address> addressList = new Geocoder(context).getFromLocation(latitude, longitude, 1);
                address = addressList.isEmpty() ? null : addressList.get(0);
            } catch (IOException e) {
                Timber.w(e, "Geocoder lib failed");
            }
        } else {
            Timber.w("Geocoder is not preset");
        }
        return address;

    }

    /**
     * Make a request to maps api and convert response to {@link Address} if status == OK.
     * Should be run from background.
     */
    public static Address getAddressViaNetwork(double latitude, double longitude) {
        Locale defaultLocale = Locale.getDefault();
        OkHttpClient client = new OkHttpClient();
        // create url with query params
        String query = String.format(
            "http://maps.googleapis.com/maps/api/geocode/json?latlng=%s&sensor=true&language=%s",
            Double.toString(latitude) + "," + Double.toString(longitude),
            defaultLocale.getLanguage()
        );
        // make request
        Request request = new Request.Builder().url(query).build();
        String responseString;
        try {
            Timber.d("Requesting geocode network api with url: %s", query);
            Response response = client.newCall(request).execute();
            responseString = response.body().string();
        } catch (IOException e) {
            Timber.e(e, "Can't parse address");
            return null;
        }
        // process response
        GeocodeResults results = new GsonBuilder().create().fromJson(responseString, GeocodeResults.class);
        if (results == null || !results.getStatus().equals("OK") || ListUtils.isEmpty(results.getResults())) {
            Timber.w("Geocode responded with null or notOK result");
            return null;
        }
        AddressComponent postalComponent = findComponentByType(results, "postal_code");
        AddressComponent cityComponent = findComponentByType(results, "locality");
        AddressComponent regionComponent = findComponentByType(results, "administrative_area_level_1");
        AddressComponent subregionComponent = findComponentByType(results, "administrative_area_level_2");
        if (subregionComponent == null || TextUtils.isEmpty(subregionComponent.getLongName())) {
            subregionComponent = findComponentByType(results, "administrative_area_level_3");
        }
        AddressComponent countryComponent = findComponentByType(results, "country");

        // to Address
        Address address = new Address(defaultLocale);
        address.setExtras(new Bundle());
        if (postalComponent != null) address.setPostalCode(postalComponent.getLongName());
        if (cityComponent != null) address.setLocality(cityComponent.getLongName());
        if (regionComponent != null) {
            address.setAdminArea(regionComponent.getLongName());
            address.getExtras().putString(AddressExtraKeys.KEY_ADMIN_AREA_SHORT, regionComponent.getShortName());
        }
        if (subregionComponent != null) {
            address.setSubAdminArea(subregionComponent.getLongName());
            address.getExtras().putString(AddressExtraKeys.KEY_SUBADMIN_AREA_SHORT, subregionComponent.getShortName());
        }
        if (countryComponent != null) {
            address.setCountryName(countryComponent.getLongName());
            address.setCountryCode(countryComponent.getShortName());
        }
        //
        address.setAddressLine(0, results.getResults().get(0).getFormattedAddress());
        address.setLatitude(latitude);
        address.setLongitude(longitude);

        return address;
    }

    private static AddressComponent findComponentByType(GeocodeResults results, String type) {
        for (Result result : results.getResults()) {
            AddressComponent componentByType = findComponentByType(result, type);
            if (componentByType != null) return componentByType;
        }
        return null;
    }

    private static AddressComponent findComponentByType(Result result, String type) {
        if (result.getAddressComponents() == null) return null;
        for (AddressComponent component : result.getAddressComponents()) {
            if (!ListUtils.isEmpty(component.getTypes())) {
                if (type.equals(component.getTypes().get(0))) {
                    return component;
                }
            }
        }
        return null;
    }

}