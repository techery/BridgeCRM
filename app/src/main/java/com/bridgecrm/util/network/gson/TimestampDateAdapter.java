package com.bridgecrm.util.network.gson;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

public class TimestampDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement jsonElement, Type type,
                            JsonDeserializationContext jsonDeserializationContext) {
        Date result = null;
        if (jsonElement != null) {
            long timestamp = jsonElement.getAsLong() * 1000;
            result = new Date(timestamp);
        }
        return result;
    }

    @Override
    public JsonElement serialize(Date date, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        JsonElement result = null;
        if (date != null) {
            long l = date.getTime() / 1000;
            result = new JsonPrimitive(l);
        }
        return result;
    }
}