package com.bridgecrm.util.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Patterns;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

public class TelephonyHelper {

    /**
     * Gets first found e-mail from AccountManager
     * @return e-mail or empty string
     */
    public static String getFirstAccountEmail(Context context) {
        StringBuilder result = new StringBuilder();
        Account[] accounts = AccountManager.get(context.getApplicationContext()).getAccounts();
        for (Account acc : accounts)
        {
            if (Patterns.EMAIL_ADDRESS.matcher(acc.name).matches())
            {
                result.append(acc.name);
                break;
            }
        }
        return result.toString();
    }

    /**
     * Gets array of found e-mails from AccountManager
     * @return e-mail string array (length()==0 if no email accounts were found)
     */
    public static String[] getAllAccountEmails(Context context) {
        Set<String> result = new HashSet<String>();
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account acc : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(acc.name).matches()) {
                result.add(acc.name);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static boolean isEmailFromAccounts(Context context, String email) {
        for (String curEmail : getAllAccountEmails(context)) {
            if (curEmail.equals(email)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmail(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Gets phone number from TelephonyManager
     * @return phone number or null
     */
    public static String getPhoneNumber(Context context) {
        TelephonyManager phoneState = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return phoneState.getLine1Number();
    }

    public static boolean isPhoneNumberFromSim(Context context, CharSequence phone) {
        return getPhoneNumber(context) != null && getPhoneNumber(context).equals(phone);
    }

    public static boolean isPhoneNumber(CharSequence phone) {
        return Patterns.PHONE.matcher(phone).matches();
    }

    public static String getUniqueDiviceId(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = ""
                + android.provider.Settings.Secure.getString(context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }

    public static String getDeviceId(Context context) {
        final TelephonyManager tm =(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static String getSystemLanguageCode() {
        return Locale.getDefault().getLanguage();
    }

    public static int getTimeZoneHoursOffset() {
        return TimeZone.getDefault().getRawOffset() / (int) DateUtils.HOUR_IN_MILLIS;
    }

}
