package com.bridgecrm.util.network.gson;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UnixTimeTypeAdapterFactory<U> extends CustomizedTypeAdapterFactory<U> {

    private final String[] timeFields;

    public UnixTimeTypeAdapterFactory(Class<U> clazz, String... timeFields) {
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
                    Long unixtime = jsonElement.getAsLong();
                    jsonObject.addProperty(field, unixtime * 1000);
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
                    Long unixtime = jsonElement.getAsLong() / 1000;
                    jsonObject.addProperty(field, unixtime);
                }
            }
        }
    }
}
