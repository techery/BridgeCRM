package com.bridgecrm.util.network.gson;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UnixInMillisTimeTypeAdapterFactory<U> extends CustomizedTypeAdapterFactory<U> {

    private final String[] timeFields;

    public UnixInMillisTimeTypeAdapterFactory(Class<U> clazz, String... timeFields) {
        super(clazz);
        this.timeFields = timeFields;
    }

    @Override
    protected void afterRead(JsonElement deserialized) {
        JsonObject jsonObject = deserialized.getAsJsonObject();
        for (String field : timeFields) {
            if (jsonObject.has(field)) {
                final JsonElement jsonElement = jsonObject.get(field);
                if (!jsonElement.isJsonNull()) {
                    float unixtimeInMillis = jsonElement.getAsFloat();
                    jsonObject.addProperty(field, ((long) (unixtimeInMillis * 1000l)));

                }
            }
        }
    }

    @Override
    protected void beforeWrite(U source, JsonElement toSerialize) {
        JsonObject jsonObject = toSerialize.getAsJsonObject();
        for (String field : timeFields) {
            if (jsonObject.has(field)) {
                final JsonElement jsonElement = jsonObject.get(field);
                if (!jsonElement.isJsonNull()) {
                    float unixtimeInMillis = jsonElement.getAsFloat() / 1000.0f;
                    jsonObject.addProperty(field, unixtimeInMillis);
                }
            }
        }
    }
}
