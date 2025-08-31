# NeoWs Tracking Application

A **Near-Earth Object (NEO) monitoring and alerting system** built with Spring Boot. It tracks potentially hazardous asteroids using NASA's NeoWs API and sends real-time email notifications.

## Features

### Core

- Real-time asteroid monitoring (NASA NeoWs API)
- Hazardous asteroid detection
- Event-driven architecture with Kafka
- Email alerts for registered users
- User management & preferences
- Health checks and monitoring

### Technical

- Microservices architecture (Asteroid & Email services)
- Kafka-based asynchronous processing
- MySQL persistence (users, notifications)
- REST APIs with OpenAPI/Swagger docs
- Scheduled monitoring & alerting
- Robust error handling/logging

## Architecture

### Services

**1. NeoWs Service (8080)**

- Fetches asteroid data, detects hazards, publishes alerts
- Components: `AlertController`, `AlertService`, `NasaClient`, Kafka producer

**2. Email Notification Service (8082)**

- Consumes alerts, manages users, sends emails
- Components: `UserController`, `NotificationService`, `EmailService`, `UserService`

### Infrastructure

- Apache Kafka for events
- MySQL for persistence
- Kafka UI (8084) for monitoring

## Tech Stack

- Java 21, Spring Boot 3.x
- Spring Data JPA, MySQL
- Apache Kafka
- Java Mail Sender
- Docker Compose
- Lombok
- OpenAPI/Swagger

## Prerequisites

- Java 21+
- Docker & Docker Compose
- NASA API Key ([https://api.nasa.gov/](https://api.nasa.gov/))
- MySQL or Dockerized DB
- Email credentials (MailTrap/SMTP)

## Quick Start

1. **Setup Environment**

   ```bash
   cp NeoWs/env.template NeoWs/.env
   cp emailnotificationservice/env.template emailnotificationservice/.env
   ```

2. **Configure Variables**

   - `NASA_API_KEY`, DB creds, SMTP creds

3. **Start Infrastructure**

   ```bash
   cd NeoWs
   docker-compose up -d
   # Kafka UI at http://localhost:8084
   ```

4. **Run Services**

   ```bash
   cd NeoWs && ./mvnw spring-boot:run
   cd emailnotificationservice && ./mvnw spring-boot:run
   ```

5. **Test**

   ```bash
   # Trigger alert
   curl -X POST http://localhost:8080/api/v1/asteroid-alerts/alert
   # Register user
   curl -X POST http://localhost:8082/api/users -H "Content-Type: application/json" -d '{"fullName":"John Doe","email":"john@example.com"}'
   ```

## APIs

**NeoWs Service (8080)**

- `GET /api/v1/asteroid-alerts/health`
- `POST /api/v1/asteroid-alerts/alert`
- Swagger: `/swagger-ui.html`

**Email Service (8082)**

- `POST /api/users` (register)
- `GET /api/users` (list)
- `GET /api/users/{id}`
- `PUT /api/users/{id}/notification`
- `POST /api/users/send-alerts`
- `DELETE /api/users/{id}`
- Swagger: `/swagger-ui.html`

## Workflow

1. NeoWs fetches 7-day asteroid data
2. Filters hazardous asteroids
3. Publishes events to Kafka (`asteroid-alert`)
4. Email service consumes, stores, processes events
5. Scheduled job sends emails to users with alerts enabled

## Monitoring

- Kafka UI: [http://localhost:8084](http://localhost:8084)
- Health checks:

  - NeoWs: [http://localhost:8080/api/v1/asteroid-alerts/health](http://localhost:8080/api/v1/asteroid-alerts/health)
  - Email: [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health)

- Logs: console + Kafka topic inspection

## Configuration

- NASA API: `https://api.nasa.gov/neo/rest/v1/feed` (7-day range, 1000 req/hr free tier)
- Kafka: `localhost:9092`, topic `asteroid-alert`
- Email: MailTrap/SMTP, scheduled \~10s (configurable)

## Troubleshooting

- Invalid NASA API key → update `.env`
- Kafka issues → check container status and UI
- DB issues → verify MySQL creds
- Email failures → check SMTP settings/network

Logs and Kafka UI are primary debugging tools.
