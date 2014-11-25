package com.bridgecrm.helper.media.event;

import com.bridgecrm.util.base.DataEvent;

public class PhotoSelectedEvent extends DataEvent<String> {

    public PhotoSelectedEvent(String data) {
        super(data);
    }
}
