package org.vhugo.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sale {
    @JsonProperty("sale_id")
    public String saleId;

    @JsonProperty("seller_id")
    public String sellerId;

    @JsonProperty("city_id")
    public String cityId;

    @JsonProperty("sale_amount")
    public Double saleAmount;

    @Override
    public String toString() {
        return "Sale{saleId='" + saleId + "', sellerId='" + sellerId + "', cityId='" + cityId + "', amount=" + saleAmount + "}";
    }
}