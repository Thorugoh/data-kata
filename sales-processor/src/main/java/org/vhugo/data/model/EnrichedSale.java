package org.vhugo.data.model;

public class EnrichedSale {
    public String saleId;
    public String sellerId;
    public String cityId;
    public Double saleAmount;

    public String cityName = "Pending...";
    public Double targetAmount = 0.0;
    public Double performance = 0.0;

    public EnrichedSale() {}

    public EnrichedSale(Sale sale) {
        this.saleId = sale.saleId;
        this.sellerId = sale.sellerId;
        this.cityId = sale.cityId;
        this.saleAmount = sale.saleAmount;
    }

    @Override
    public String toString() {
        return String.format("✅ ENRICHED | Sale: %s | Seller: %s | City: %s | Amount: $%.2f | Target: $%.2f | Perf: %.2f%%",
                saleId, sellerId, cityName, saleAmount, targetAmount, performance);
    }
}