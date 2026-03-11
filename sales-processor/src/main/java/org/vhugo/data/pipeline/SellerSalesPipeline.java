package org.vhugo.data.pipeline;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.core.datastream.sink.JdbcSinkBuilder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.vhugo.data.model.EnrichedSale;

import java.sql.PreparedStatement;
import java.time.Duration;

public class SellerSalesPipeline {

    public static void build(DataStream<EnrichedSale> enrichedStream) {
        String upsertQuery = "INSERT INTO top_sales_seller (seller_id, total_amount, avg_performance) VALUES (?, ?, ?) " +
                "ON CONFLICT (seller_id) DO UPDATE SET total_amount = top_sales_seller.total_amount + EXCLUDED.total_amount, avg_performance = EXCLUDED.avg_performance, last_updated = CURRENT_TIMESTAMP";

        enrichedStream
            .keyBy(new KeySelector<EnrichedSale, String>() {
                @Override
                public String getKey(EnrichedSale sale) throws Exception {
                    return sale.sellerId;
                }
            })
            .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(30)))
            .reduce(new ReduceFunction<EnrichedSale>() {
                @Override
                public EnrichedSale reduce(EnrichedSale v1, EnrichedSale v2) {
                    EnrichedSale aggregated = new EnrichedSale();
                    aggregated.sellerId = v1.sellerId;
                    aggregated.saleAmount = v1.saleAmount + v2.saleAmount;

                    aggregated.performance = (v1.performance + v2.performance) / 2;

                    aggregated.cityName = "MULTIPLE";
                    aggregated.saleId = "AGGREGATED";
                    return aggregated;
                }
            }).sinkTo(new JdbcSinkBuilder<EnrichedSale>()
                .withQueryStatement(
                    upsertQuery,
                    (PreparedStatement statement, EnrichedSale agg) -> {
                        statement.setString(1, agg.sellerId);
                        statement.setDouble(2, agg.saleAmount);
                        statement.setDouble(3, agg.performance);
                    }
                )
                .withExecutionOptions(
                    JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build()
                )
                .buildAtLeastOnce(
                    new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:postgresql://localhost:5434/analytics")
                        .withDriverName("org.postgresql.Driver")
                        .withUsername("admin")
                        .withPassword("admin_password")
                        .build()
                )
            );
    }
}