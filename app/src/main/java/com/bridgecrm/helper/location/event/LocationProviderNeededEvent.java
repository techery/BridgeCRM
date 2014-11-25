package com.bridgecrm.helper.location.event;


import com.bridgecrm.helper.location.LocationHelperManager;
import com.bridgecrm.util.base.DataEvent;

import static com.bridgecrm.helper.location.LocationHelperManager.LocationType;

public class LocationProviderNeededEvent extends DataEvent<LocationHelperManager.LocationType> {

    public LocationProviderNeededEvent(LocationType data) {
        super(data);
    }
}
