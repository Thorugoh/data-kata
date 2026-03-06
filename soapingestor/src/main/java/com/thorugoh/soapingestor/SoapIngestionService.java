package com.thorugoh.soapingestor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class SoapIngestionService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;
    private final ObjectMapper jsonMapper;

    private static final String SOAP_ENDPOINT = "http://localhost:8080/ws/sales";
    private static final String KAFKA_TOPIC = "raw_ws_targets";

    public SoapIngestionService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = new RestTemplate();
        this.xmlMapper = new XmlMapper();
        this.jsonMapper = new JsonMapper();
    }

    @Scheduled(fixedDelay = 30000)
    public void ingestSoapData() {
        try {
            String requestPayload = """
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://example.com/sales/service">
       <soapenv:Header/>
       <soapenv:Body>
          <ser:GetSalesTargetsRequest/>
       </soapenv:Body>
    </soapenv:Envelope>
    """;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            HttpEntity<String> request = new HttpEntity<>(requestPayload, headers);

            String xmlResponse = restTemplate.postForObject(SOAP_ENDPOINT, request, String.class);

            if (xmlResponse != null) {
                JsonNode rootNode = xmlMapper.readTree(xmlResponse);
                JsonNode targets = rootNode.at("/Body/GetSalesTargetsResponse/Target");

                if (targets.isArray()) {
                    for (JsonNode target : targets) {
                        String jsonString = jsonMapper.writeValueAsString(target);
                        kafkaTemplate.send(KAFKA_TOPIC, jsonString);
                    }
                } else if (!targets.isMissingNode()) {
                    String jsonString = jsonMapper.writeValueAsString(targets);
                    kafkaTemplate.send(KAFKA_TOPIC, jsonString);
                }

                System.out.println("Successfully published SOAP data to Kafka.");
            }
        } catch (Exception e) {
            System.err.println("Error fetching SOAP data: " + e.getMessage());
        }
    }
}
