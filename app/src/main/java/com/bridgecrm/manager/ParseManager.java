package com.bridgecrm.manager;


import android.content.Context;

import com.parse.Parse;

public class ParseManager {

    private final Context context;
    private final String appId;
    private final String clientId;

    public ParseManager(Context context, String appId, String clientId) {
        this.context = context;
        this.appId = appId;
        this.clientId = clientId;
    }

    public void initialize() {
        Parse.initialize(context, appId, clientId);
    }
}
