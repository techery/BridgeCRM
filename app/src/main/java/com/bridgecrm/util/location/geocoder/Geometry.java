
package com.bridgecrm.util.location.geocoder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Geometry {

    @Expose
    private AddressComponent.Bounds bounds;
    @Expose
    private AddressComponent.Location location;
    @SerializedName("location_type")
    @Expose
    private String locationType;
    @Expose
    private AddressComponent.Viewport viewport;

    public AddressComponent.Bounds getBounds() {
        return bounds;
    }

    public AddressComponent.Location getLocation() {
        return location;
    }

    public String getLocationType() {
        return locationType;
    }

    public AddressComponent.Viewport getViewport() {
        return viewport;
    }

}
