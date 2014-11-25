
package com.bridgecrm.util.location.geocoder;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class GeocodeResults {

    @Expose
    private List<Result> results = new ArrayList<Result>();
    @Expose
    private String status;

    public List<Result> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }

}
