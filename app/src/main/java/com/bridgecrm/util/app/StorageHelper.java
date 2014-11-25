package com.bridgecrm.util.app;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class StorageHelper {

    public static boolean isExternalStorageAvailableWritable() {
        boolean externalStorageAvailable;
        boolean externalStorageWriteable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            externalStorageAvailable = externalStorageWriteable = false;
        }
        return externalStorageAvailable && externalStorageWriteable;
    }

    public static File getAvailableCacheDir(Context context) {
        if (isExternalStorageAvailableWritable()) {
            return context.getExternalCacheDir();
        } else {
            return context.getCacheDir();
        }
    }

}
