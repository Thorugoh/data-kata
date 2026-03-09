# Data Integration POC - Ingestion Phase

This project demonstrates a real-time data pipeline integrating three distinct sources into **Apache Kafka**.

## 🏗 Architecture
* **ERP (Postgres):** Store locations data via Kafka Connect JDBC.
* **Legacy (SOAP/API):** Sales targets via a custom Spring Boot producer.
* **Files (SFTP):** Transactional sales data from CSV files via Kafka Connect SFTP.

---

## 🚀 Setup Connectors

Kafka Connect exposes a REST API on port `8083`. You must send these `POST` requests to activate the ingestion routes for Postgres and SFTP.

### 1. Postgres (ERP) Connector
Monitors the `locations` table and streams changes to Kafka.

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "postgres-source",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
      "tasks.max": "1",
      "connection.url": "jdbc:postgresql://erp-db:5432/erp_sales",
      "connection.user": "admin",
      "connection.password": "password123",
      "table.whitelist": "locations",
      "mode": "incrementing",
      "incrementing.column.name": "city_id",
      "topic.prefix": "raw_erp_",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false"
    }
  }'
```

### 1. SFTP (CSV) Connector
Polls the SFTP server for new .csv files and moves processed files to /finished.

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "sftp-csv-source",
    "config": {
      "connector.class": "io.confluent.connect.sftp.SftpCsvSourceConnector",
      "tasks.max": "1",
      "sftp.host": "source_remote_ftp",
      "sftp.port": "22",
      "sftp.username": "datauser",
      "sftp.password": "pass",
      "input.path": "/sales_data",
      "finished.path": "/sales_data/finished",
      "error.path": "/sales_data/error",
      "input.file.pattern": ".*\\.csv",
      "kafka.topic": "raw_ftp_sales",
      "csv.first.row.as.header": "true",
      "schema.generation.enabled": "true",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter"
    }
  }'
```

## 🔍 Validation & Monitoring

### Check Connector Status

```bash
curl -s http://localhost:8083/connectors/sftp-csv-source/status | jq
curl -s http://localhost:8083/connectors/postgres-source/status | jq
```

### Monitor Topics (The 3 Sources)

```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic raw_ftp_sales --from-beginning
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic raw_ws_targets --from-beginning
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic raw_erp_locations --from-beginning
```

### 🛠 Troubleshooting
Connector 404: Ensure the Kafka Connect container is healthy and the plugin is installed.

Empty Topics: For Postgres, ensure you have performed an INSERT. For SFTP, ensure the .csv is in the correctly mapped volume.