package com.bridgecrm.manager;

import android.content.Context;

import com.bridgecrm.util.app.PreferenceHelper;

public class PreferenceWrapper extends PreferenceHelper.GsonPreferenceHelper {

    private GsonPreferenceHelper persistentStorage;

    public PreferenceWrapper(Context context) {
        super(context);
        persistentStorage = new GsonPreferenceHelper(context, "persistent", Context.MODE_PRIVATE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Session
    ///////////////////////////////////////////////////////////////////////////

    /** Is app have ever started before */
    public boolean isLaunchedBefore() {
        String key = "launched_before";
        boolean launchedBefore = persistentStorage.getBooleanValue(key, false);
        if (!launchedBefore) {
            persistentStorage.storeValue(key, true, false);
        }
        return launchedBefore;
    }

    /** Is app have ever had session before */
    public boolean isAuthenticatedBefore() {
        return persistentStorage.getBooleanValue("authenticated_before", false);
    }

    /** Save user had session before on this device */
    public void putAuthenticatedBefore(boolean authenticated) {
        persistentStorage.storeValue("authenticated_before", authenticated, false);
    }

    public boolean isAfterRegistration() {
        return getBooleanValue("after_reg", false);
    }

    public void putAfterRegistration(boolean afterReg) {
        storeValue("after_reg", afterReg, false);
    }


}
