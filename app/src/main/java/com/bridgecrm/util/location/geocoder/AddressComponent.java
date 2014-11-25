
package com.bridgecrm.util.location.geocoder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AddressComponent {

    @SerializedName("long_name")
    @Expose
    private String longName;
    @SerializedName("short_name")
    @Expose
    private String shortName;
    @Expose
    private List<String> types = new ArrayList<String>();

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public List<String> getTypes() {
        return types;
    }

    public static class Bounds {

        @Expose
        private Location northeast;
        @Expose
        private Location southwest;

        public Location getNortheast() {
            return northeast;
        }

        public Location getSouthwest() {
            return southwest;
        }

    }

    public static class Viewport {

        @Expose
        private Location northeast;
        @Expose
        private Location southwest;

        public Location getNortheast() {
            return northeast;
        }

        public Location getSouthwest() {
            return southwest;
        }

    }

    public static class Location {

        @Expose
        private Double lat;
        @Expose
        private Double lng;

        public Double getLat() {
            return lat;
        }

        public Double getLng() {
            return lng;
        }

    }
}
