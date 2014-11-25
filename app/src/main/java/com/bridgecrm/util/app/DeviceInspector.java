package com.bridgecrm.util.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.Locale;

import timber.log.Timber;

public class DeviceInspector {

    private static boolean batteryStatus;
    private static String networkType;

    private DeviceInspector() {
    }

    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceID(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /** Should be called from background thread */
    public static String getAdvertisingID(Context context) {
        AdvertisingIdClient.Info info = null;
        try {
            info = AdvertisingIdClient.getAdvertisingIdInfo(context);
        } catch (IOException | GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
            Timber.e(e, "Can't get advertising info");
        }
        return info != null ? info.getId() : null;
    }

    public static String getPlatformVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return String.valueOf(metrics.widthPixels) + "x" + String.valueOf(metrics.heightPixels);
    }

    public static String getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape";
    }

    public static int getBatteryStatus(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50;
        }

        return level / scale * 100;
    }

    public static String getNetworkType(Context context) {
        String network_type = "UNKNOWN";// maybe usb reverse tethering
        NetworkInfo active_network = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (active_network != null && active_network.isConnectedOrConnecting()) {
            if (active_network.getType() == ConnectivityManager.TYPE_WIFI) {
                network_type = "WIFI";
            } else if (active_network.getType() == ConnectivityManager.TYPE_MOBILE) {
                network_type = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo().getSubtypeName();
            }
        }
        return network_type;
    }

    public static int getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Can't get app version 0_o impossible!");
            return -1;
        }
    }
}
