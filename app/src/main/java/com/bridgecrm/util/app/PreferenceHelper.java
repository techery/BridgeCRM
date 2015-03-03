package com.bridgecrm.util.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bridgecrm.util.base.SecurityHelper;
import com.bridgecrm.util.network.gson.CollectionDeserializer;
import com.bridgecrm.util.network.gson.LowercaseEnumTypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;

public class PreferenceHelper {

    protected SharedPreferences prefs;

    public PreferenceHelper(Context context) {
        this(context, null, Context.MODE_PRIVATE);
    }

    public PreferenceHelper(Context context, String name, int mode) {
        prefs = createPrefs(context, name, mode);
    }

    protected static SharedPreferences createPrefs(Context context, String name, int mode) {
        if (TextUtils.isEmpty(name)) {
            return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        } else {
            return context.getSharedPreferences(name, mode);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////

    public String getStringValue(String key, boolean asEncrypted) {
        return getStringValue(key, null, asEncrypted);
    }

    public String getStringValue(String key, String defValue, boolean asEncrypted) {
        String value = prefs.getString(key, defValue);
        if (asEncrypted && value != null && !value.equals(defValue))
            try {
                return SecurityHelper.decrypt(value);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        else
            return value;
    }

    public long getLongValue(String key, boolean asEncrypted) {
        return getLongValue(key, 0l, asEncrypted);
    }

    public long getLongValue(String key, long defValue, boolean asEncrypted) {
        String stringValue = getStringValue(key, asEncrypted);
        if (stringValue == null)
            return defValue;
        else
            return Long.valueOf(stringValue);
    }

    public int getIntValue(String key, boolean asEncrypted) {
        return getIntValue(key, 0, asEncrypted);
    }

    public int getIntValue(String key, int defValue, boolean asEncrypted) {
        String stringValue = getStringValue(key, asEncrypted);
        if (stringValue == null)
            return defValue;
        else
            return Integer.valueOf(stringValue);
    }

    public boolean getBooleanValue(String key, boolean asEncrypted) {
        return getBooleanValue(key, false, asEncrypted);
    }

    public boolean getBooleanValue(String key, boolean defValue, boolean asEncrypted) {
        String stringValue = getStringValue(key, asEncrypted);
        if (stringValue == null)
            return defValue;
        else
            return Boolean.valueOf(stringValue);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Setter
    ///////////////////////////////////////////////////////////////////////////

    public <U> void storeValue(String key, U value, boolean asEncrypted) {
        String resultValue;
        if (asEncrypted)
            try {
                resultValue = SecurityHelper.encrypt(String.valueOf(value));
            } catch (Exception e) {
                e.printStackTrace();
                resultValue = "";
            }
        else
            resultValue = String.valueOf(value);
        prefs.edit().putString(key, resultValue).commit();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Has
    ///////////////////////////////////////////////////////////////////////////

    public boolean hasValue(String key) {
        return prefs.contains(key);
    }

    public boolean hasValue(Class clazz) {
        return prefs.contains(getClassKey(clazz));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clear
    ///////////////////////////////////////////////////////////////////////////

    public void clearValue(String key) {
        prefs.edit().remove(key).commit();
    }

    public void clearAll() {
        prefs.edit().clear().commit();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Gson addition
    ///////////////////////////////////////////////////////////////////////////

    public static class GsonPreferenceHelper extends PreferenceHelper {

        private Gson gson = new Gson();

        public GsonPreferenceHelper(Context context) {
            super(context);
            initGson();
        }

        public GsonPreferenceHelper(Context context, String name, int mode) {
            super(context, name, mode);
            initGson();
        }

        public GsonPreferenceHelper(Context context, String name, int mode, Gson gson) {
            super(context, name, mode);
            this.gson = gson;
        }

        private void initGson() {
            gson = new GsonBuilder()
                .registerTypeAdapter(Collection.class, new CollectionDeserializer())
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
                .create();
        }

        ///////////////////////////////////////////////////////////////////////////
        // Object Helpers
        ///////////////////////////////////////////////////////////////////////////

        public <U> U getObjectFromGson(String key, TypeToken<U> typeToken, boolean asEncrypted) {
            return gson.fromJson(getStringValue(key, asEncrypted), typeToken.getType());
        }

        public <Z> Z getObjectFromGson(String key, Class<Z> type, boolean asEncrypted) {
            return gson.fromJson(getStringValue(key, asEncrypted), type);
        }

        public <Z> Z getObjectFromGson(Class<Z> type, boolean asEncrypted) {
            return gson.fromJson(getStringValue(getClassKey(type), asEncrypted), type);
        }

        public <U> U getObjectFromGson(TypeToken<U> typeToken, boolean asEncrypted) {
            return gson.fromJson(getStringValue(getClassKey(typeToken), asEncrypted), typeToken.getType());
        }

        public <Z> void storeObjectAsGson(String key, Z object, boolean asEncrypted) {
            Type type = new TypeToken<Z>() {
            }.getType();
            storeValue(key, gson.toJson(object, type), asEncrypted);
        }

        public <Z> void storeObjectAsGson(Z object, boolean asEncrypted) {
            Type type = new TypeToken<Z>() {
            }.getType();
            storeValue(getClassKey(object.getClass()), gson.toJson(object, type), asEncrypted);
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // misc
    ///////////////////////////////////////////////////////////////////////////

    private static String getClassKey(Class clazz) {
        return clazz.getName();
    }

    private static <U> String getClassKey(TypeToken<U> typeToken) {
        return typeToken.getRawType().getName();
    }

}
