package org.vhugo.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    @JsonProperty("city_id")
    public String cityId;

    @JsonProperty("city_name")
    public String cityName;

    @Override
    public String toString() {
        return "Location{cityId='" + cityId + "', cityName='" + cityName + "'}";
    }
}