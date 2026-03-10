package org.vhugo.data.pipeline;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import java.time.Duration;
import org.vhugo.data.model.EnrichedSale;

public class CitySalesPipeline {

    public static void build(DataStream<EnrichedSale> enrichedStream) {

        enrichedStream
                .keyBy(new KeySelector<EnrichedSale, String>() {
                    @Override
                    public String getKey(EnrichedSale sale) throws Exception {
                        return sale.cityName;
                    }
                })
                .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(30)))
                // 3. Sum the sales amount within this window
                .reduce(new ReduceFunction<EnrichedSale>() {
                    @Override
                    public EnrichedSale reduce(EnrichedSale v1, EnrichedSale v2) {
                        EnrichedSale aggregated = new EnrichedSale();
                        aggregated.cityName = v1.cityName;
                        aggregated.saleAmount = v1.saleAmount + v2.saleAmount;
                        // Mocking other fields just to keep the object structure
                        aggregated.sellerId = "MULTIPLE";
                        aggregated.saleId = "AGGREGATED";
                        return aggregated;
                    }
                })
                // 4. Output the result
                .map(agg -> "PIPELINE A (City): " + agg.cityName + " -> Total: $" + agg.saleAmount)
                .print();
    }
}
