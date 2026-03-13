package org.vhugo;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.vhugo.data.model.EnrichedSale;
import org.vhugo.data.model.Location;
import org.vhugo.data.model.Sale;
import org.vhugo.data.model.Target;
import org.vhugo.data.pipeline.CitySalesPipeline;
import org.vhugo.data.pipeline.SellerSalesPipeline;

public class SalesAggregationJob {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000);
        String kafkaBrokers = "localhost:9092";

        KafkaSource<String> salesSource = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaBrokers).setTopics("raw_ftp_sales").setGroupId("flink-sales-group")
                .setStartingOffsets(OffsetsInitializer.earliest()).setValueOnlyDeserializer(new SimpleStringSchema()).build();

        KafkaSource<String> locationsSource = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaBrokers).setTopics("raw_erp_locations").setGroupId("flink-locations-group")
                .setStartingOffsets(OffsetsInitializer.earliest()).setValueOnlyDeserializer(new SimpleStringSchema()).build();

        KafkaSource<String> targetsSource = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaBrokers).setTopics("raw_ws_targets").setGroupId("flink-targets-group")
                .setStartingOffsets(OffsetsInitializer.earliest()).setValueOnlyDeserializer(new SimpleStringSchema()).build();

        DataStream<Sale> sales = env.fromSource(salesSource, WatermarkStrategy.noWatermarks(), "Sales")
                .map(json -> new ObjectMapper().readValue(json, Sale.class));

        DataStream<Location> locations = env.fromSource(locationsSource, WatermarkStrategy.noWatermarks(), "Locations")
                .map(json -> new ObjectMapper().readValue(json, Location.class));

        DataStream<Target> targets = env.fromSource(targetsSource, WatermarkStrategy.noWatermarks(), "Targets")
                .map(json -> new ObjectMapper().readValue(json, Target.class));

        MapStateDescriptor<String, String> locationStateDesc = new MapStateDescriptor<>("locationState", BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO);
        MapStateDescriptor<String, Double> targetStateDesc = new MapStateDescriptor<>("targetState", BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.DOUBLE_TYPE_INFO);

        BroadcastStream<Location> broadcastLocations = locations.broadcast(locationStateDesc);
        BroadcastStream<Target> broadcastTargets = targets.broadcast(targetStateDesc);

        // 4. Pipeline Step 1: Join Sales with Locations
        DataStream<EnrichedSale> salesWithCity = sales
                .connect(broadcastLocations)
                .process(new BroadcastProcessFunction<Sale, Location, EnrichedSale>() {
                    @Override
                    public void processElement(Sale sale, ReadOnlyContext ctx, Collector<EnrichedSale> out) throws Exception {
                        EnrichedSale enriched = new EnrichedSale(sale);
                        String cityName = ctx.getBroadcastState(locationStateDesc).get(sale.cityId);
                        if (cityName != null) {
                            enriched.cityName = cityName;
                        }
                        out.collect(enriched);
                    }

                    @Override
                    public void processBroadcastElement(Location loc, Context ctx, Collector<EnrichedSale> out) throws Exception {
                        // Saves the city in Flink's memory
                        ctx.getBroadcastState(locationStateDesc).put(loc.cityId, loc.cityName);
                    }
                });

        // 5. Pipeline Step 2: Join result with Targets and Calculate Performance
        DataStream<EnrichedSale> finalStream = salesWithCity
                .connect(broadcastTargets)
                .process(new BroadcastProcessFunction<EnrichedSale, Target, EnrichedSale>() {
                    @Override
                    public void processElement(EnrichedSale sale, ReadOnlyContext ctx, Collector<EnrichedSale> out) throws Exception {
                        Double target = ctx.getBroadcastState(targetStateDesc).get(sale.sellerId);
                        if (target != null && target > 0) {
                            sale.targetAmount = target;
                            sale.performance = (sale.saleAmount / target) * 100;
                        }
                        out.collect(sale);
                    }

                    @Override
                    public void processBroadcastElement(Target target, Context ctx, Collector<EnrichedSale> out) throws Exception {
                        // Saves the target in Flink's memory
                        ctx.getBroadcastState(targetStateDesc).put(target.sellerId, target.monthlyTarget);
                    }
                });


        CitySalesPipeline.build(finalStream);
        SellerSalesPipeline.build(finalStream);
        env.execute("POC: Flink Sales Data Enrichment");
    }
}