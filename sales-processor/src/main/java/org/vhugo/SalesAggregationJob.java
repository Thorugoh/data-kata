package org.vhugo;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SalesAggregationJob {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String kafkaBrokers = "localhost:9092";

        KafkaSource<String> salesSource = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaBrokers)
                .setTopics("raw_ftp_sales")
                .setGroupId("flink-sales-group")
                .setStartingOffsets(OffsetsInitializer.earliest()) // Reads from the beginning
                .setValueOnlyDeserializer(new SimpleStringSchema()) // Reads raw JSON as String
                .build();

        KafkaSource<String> locationsSource = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaBrokers)
                .setTopics("raw_erp_locations")
                .setGroupId("flink-locations-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaSource<String> targetsSource = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaBrokers)
                .setTopics("raw_ws_targets")
                .setGroupId("flink-targets-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> salesStream = env.fromSource(salesSource, WatermarkStrategy.noWatermarks(), "Kafka Sales Source");
        DataStream<String> locationsStream = env.fromSource(locationsSource, WatermarkStrategy.noWatermarks(), "Kafka Locations Source");
        DataStream<String> targetsStream = env.fromSource(targetsSource, WatermarkStrategy.noWatermarks(), "Kafka Targets Source");

        salesStream.print("SALES -> ");
        locationsStream.print("LOCATIONS -> ");
        targetsStream.print("TARGETS -> ");

        env.execute("POC: Flink Sales Aggregation and Enrichment");
    }
}