package com.bridgecrm.helper.location.event;

import android.location.Location;

import com.bridgecrm.util.base.DataEvent;

public class LocationUpdateEvent extends DataEvent<Location> {

    public LocationUpdateEvent(Location data) {
        super(data);
    }
}
