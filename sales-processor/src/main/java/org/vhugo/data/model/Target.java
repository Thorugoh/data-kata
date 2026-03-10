package org.vhugo.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Target {
    @JsonProperty("SellerId")
    public String sellerId;

    @JsonProperty("MonthlyTarget")
    public Double monthlyTarget;

    @Override
    public String toString() {
        return "Target{sellerId='" + sellerId + "', monthlyTarget=" + monthlyTarget + "}";
    }
}