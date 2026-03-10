package org.vhugo.data.pipeline;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.vhugo.data.model.EnrichedSale;

import java.time.Duration;

public class SellerSalesPipeline {

    public static void build(DataStream<EnrichedSale> enrichedStream) {

        enrichedStream
                // 1. Group by Seller ID
                .keyBy(new KeySelector<EnrichedSale, String>() {
                    @Override
                    public String getKey(EnrichedSale sale) throws Exception {
                        return sale.sellerId;
                    }
                })
                // 2. Open a 30-second window
                .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(30)))
                // 3. Sum the sales amount and calculate average performance
                .reduce(new ReduceFunction<EnrichedSale>() {
                    @Override
                    public EnrichedSale reduce(EnrichedSale v1, EnrichedSale v2) {
                        EnrichedSale aggregated = new EnrichedSale();
                        aggregated.sellerId = v1.sellerId;
                        aggregated.saleAmount = v1.saleAmount + v2.saleAmount;

                        // Simple average for the performance in this window
                        aggregated.performance = (v1.performance + v2.performance) / 2;

                        // Mocking other fields
                        aggregated.cityName = "MULTIPLE";
                        aggregated.saleId = "AGGREGATED";
                        return aggregated;
                    }
                })
                // 4. Output the result of Pipeline B
                .map(agg -> String.format("PIPELINE B (Seller): %s -> Total: $%.2f | Avg Performance: %.2f%%",
                        agg.sellerId, agg.saleAmount, agg.performance))
                .print();
    }
}