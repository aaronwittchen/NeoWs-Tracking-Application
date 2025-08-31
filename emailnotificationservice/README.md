# Email Notification Service

A Spring Boot microservice that manages user registrations, consumes asteroid alerts from Kafka, and sends email notifications about potentially hazardous asteroids.

## Purpose

* User registration & management
* Notification preference handling
* Consume asteroid alerts from Kafka (`asteroid-alert`)
* Send HTML email notifications via SMTP
* Persist users & notifications in MySQL

## Architecture

### Components

* **UserController** → REST API for user CRUD + preferences
* **UserService** → Business logic + validation
* **NotificationService** → Kafka consumer + scheduled email processing
* **EmailService** → SMTP integration + HTML template emails

### Infrastructure

* **Kafka** for alert events
* **MySQL** for persistence
* **SMTP** (MailTrap for dev, real SMTP for prod)

## Features

* User CRUD (with email uniqueness validation)
* Enable/disable notification preferences
* HTML email alerts (scheduled every 10s or manual trigger)
* Kafka event consumption with error handling
* Audit history of notifications

## Prerequisites

* Java 21+
* Maven
* MySQL 8+ (local or Docker)
* Kafka (via Docker Compose)
* SMTP credentials (MailTrap or production)

## Quick Start

1. **Setup Environment**

   ```bash
   cp env.template .env
   # Update with DB, SMTP, and NASA_API_KEY
   ```

2. **Start MySQL (Docker)**

   ```bash
   docker run -d --name mysql-asteroid \
     -e MYSQL_ROOT_PASSWORD=rootpassword \
     -e MYSQL_DATABASE=asteroidalert \
     -e MYSQL_USER=asteroiduser \
     -e MYSQL_PASSWORD=asteroidpass \
     -p 3306:3306 mysql:8.0
   ```

3. **Run Service**

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Test**

   ```bash
   # Register user
   curl -X POST http://localhost:8082/api/users \
     -H "Content-Type: application/json" \
     -d '{"fullName":"John Doe","email":"john@example.com"}'

   # Enable notifications
   curl -X PUT http://localhost:8082/api/users/1/notification \
     -H "Content-Type: application/json" \
     -d '{"notificationEnabled":true}'
   ```

## API Endpoints

* `POST /api/users` → Register user
* `GET /api/users` → List all users
* `GET /api/users/{id}` → Get user by ID
* `PUT /api/users/{id}/notification` → Toggle notifications
* `DELETE /api/users/{id}` → Delete user
* `POST /api/users/send-alerts` → Manually send emails
* Swagger UI: `http://localhost:8082/swagger-ui.html`

## Configuration

**application.properties**

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/asteroidalert
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update

spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-service

spring.mail.host=${SPRING_MAIL_HOST:sandbox.smtp.mailtrap.io}
spring.mail.port=${SPRING_MAIL_PORT:2525}
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}
```

**Environment Variables**

* `DB_USERNAME`, `DB_PASSWORD` (MySQL)
* `SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` (SMTP)
* `MAIL_FROM_EMAIL` (sender address)

## Workflow

1. User registers via API → stored in MySQL
2. Kafka publishes asteroid alert → consumed by service
3. Event validated & stored as notification
4. Scheduled job (every 10s) fetches pending notifications
5. HTML emails sent to opted-in users

## Monitoring

* Actuator: `http://localhost:8082/actuator/health`
* Swagger: `http://localhost:8082/swagger-ui.html`
* Kafka UI (via Docker Compose)
* Logs: console + structured output

## Development

```bash
./mvnw clean package   # Build
./mvnw test            # Run tests
./mvnw spring-boot:run # Run locally
```

**Code Structure**

```
controller/   # REST endpoints
service/      # Business logic
entity/       # JPA entities
repository/   # Data access
dto/          # Transfer objects
config/       # Config classes
exception/    # Custom exceptions
```

## Integration

* **Upstream**: NeoWs Service (publishes asteroid events)
* **Infra**: Kafka, MySQL, SMTP
* **Downstream**: Email clients (users receive alerts)