# telecom-mediation-system
# Telecom Mediation System

A Java and Docker based telecom mediation project that receives CDR files, filters and encodes records, then forwards the processed data to downstream systems.

---

## Overview

This project simulates a simplified telecom mediation flow used between network nodes and billing systems.

The system is responsible for:

- Receiving CDR (Call Detail Record) files
- Filtering invalid or unwanted records
- Encoding and formatting data
- Forwarding processed CDRs to the target system

---

## Technologies Used

- Java
- Docker
- OOP
- File Handling

---

## Project Structure

```text
src/
 ├── main/
 │     └── Main.java
 │
 ├── model/
 │     ├── CDR.java
 │     ├── Node.java
 │     └── MediationRule.java
 │
 ├── collector/
 │     └── NodeCollector.java
 │
 ├── processor/
 │     └── CDRProcessor.java
 │
 ├── encoder/
 │     └── CDREncoder.java
 │
 └── exporter/
       └── CDRExporter.java
```

---

## Features

- CDR collection
- CDR filtering
- Data encoding
- File processing
- Modular architecture
- Dockerized environment

---

## Future Enhancements

- Database integration
- Real-time streaming
- Kafka integration
- Multi-threading
- REST API support

---

## Team Members

- Youssef Amgad
- Team Members Names

---

## How to Run

### Clone Repository

```bash
git clone https://github.com/your-username/telecom-mediation-system.git
```

### Run with Docker

```bash
docker build -t mediation-system .
docker run mediation-system
```

---

## Telecom Concepts Used

- CDR (Call Detail Records)
- Mediation
- Encoding
- Filtering
- Telecom Billing Flow
- Network Nodes

---

## License

This project is for educational purposes.
