# NeoWs Service

The **NeoWs Service** is a Spring Boot microservice that monitors Near-Earth Objects (NEOs) using NASA's NeoWs API. It fetches asteroid data, identifies hazardous objects, and publishes alerts to Kafka for downstream processing.

## Purpose

* Fetch real-time asteroid data from NASA NeoWs API
* Detect and filter hazardous asteroids
* Publish events to Kafka (`asteroid-alert`)
* Provide health monitoring and API documentation

## Architecture

### Components

* **AlertController**: REST endpoints (`/alert`, `/health`), error handling, Swagger docs
* **AlertService**: Business logic, NASA API integration, Kafka publishing
* **NasaClient**: HTTP client for NASA API, response mapping, error handling
* **Kafka Producer**: Publishes `AsteroidCollisionEvent` messages

### Infrastructure

* **Kafka** (with Docker Compose) for event streaming
* **NASA API** for asteroid data (7-day range, 1000 req/hr free tier)

## Features

* Real-time asteroid monitoring
* Hazard detection and validation
* Async + batch Kafka publishing with error recovery
* Health monitoring via Spring Boot Actuator
* Detailed logging for debugging

## Prerequisites

* Java 21+
* Maven
* NASA API key ([https://api.nasa.gov/](https://api.nasa.gov/))
* Kafka/Zookeeper (via Docker Compose)

## Quick Start

1. **Setup Environment**

   ```bash
   cp env.template .env
   echo "NASA_API_KEY=your_key_here" > .env
   ```

2. **Start Infrastructure**

   ```bash
   docker-compose up -d
   # Kafka UI at http://localhost:8084
   ```

3. **Run Application**

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Test**

   ```bash
   curl http://localhost:8080/api/v1/asteroid-alerts/health
   curl -X POST http://localhost:8080/api/v1/asteroid-alerts/alert
   ```

## API Endpoints

* `GET /api/v1/asteroid-alerts/health` → Service health
* `POST /api/v1/asteroid-alerts/alert` → Trigger asteroid check
* Swagger UI: `http://localhost:8080/swagger-ui.html`

## Configuration

**application.properties**

```properties
nasa.neo.api.url=https://api.nasa.gov/neo/rest/v1/feed
nasa.api.key=${NASA_API_KEY}
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.template.default-topic=asteroid-alert
```

**Environment Variables**

* `NASA_API_KEY` (required)

## Workflow

1. Client triggers `/alert`
2. Service fetches 7-day asteroid data from NASA API
3. Filters hazardous asteroids
4. Creates and publishes events to Kafka
5. Returns success/error response

## Monitoring

* Health check: `http://localhost:8080/api/v1/asteroid-alerts/health`
* Actuator: `http://localhost:8080/actuator/health`
* Logs: console + Kafka UI

## Troubleshooting

* **Invalid API key** → Update `.env` with valid NASA key
* **Kafka issues** → Ensure Kafka/Zookeeper containers are running
* **Date range error** → API max 7-day window

Enable debug logging:

```properties
logging.level.com.onion.NeoWs=DEBUG
```

## Development

```bash
./mvnw clean package   # Build
./mvnw test            # Run tests
./mvnw spring-boot:run # Run in dev
```

**Code Structure**

```
controller/   # REST endpoints
service/      # Business logic
client/       # NASA API client
dto/          # Data objects
event/        # Kafka event models
config/       # Config classes
exception/    # Custom exceptions
```

## Integration

* **Downstream**: Email Notification Service (consumes Kafka events)
* **Upstream**: NASA NeoWs API, Kafka infrastructure