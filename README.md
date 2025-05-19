# Bank Microservices Application

## Overview
This project is a microservices-based banking application that manages customers, accounts, and cards. It is built using Java, Spring Boot, and Docker, with service discovery, database integration, and inter-service communication.

## Architecture
The application consists of the following services:
1. **Eureka Server**: Service discovery for microservices.
2. **Customer Service**: Manages customer data.
3. **Account Service**: Manages account data and links to customers.
4. **Card Service**: Manages card data and links to accounts.
5. **Gateway Server**: API gateway for routing and load balancing.

### Databases
Each service has its own PostgreSQL database:
- `customer-service`: `customer_db`
- `account-service`: `account_db`
- `card-service`: `card_db`

## Technologies
- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Cloud (Eureka, OpenFeign, Resilience4j)**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Lombok**

## Prerequisites
- Docker and Docker Compose installed.
- Java 21 installed.
- Maven installed.

## Setup and Run

### 1. Clone the Repository
```bash
git clone https://github.com/GavinNyamboga/bank_app.git
cd bank_app
```

### 2. Build the Application
```bash
mvn clean install
```

### 3. Start the Application
Use Docker Compose to start all services:
```bash
docker compose up --build
```

### 4. Access the Services
All services are accessed through the **Gateway Server** at [http://localhost:8080](http://localhost:8080). Below are the routes for each service:

- **Eureka Server**: [http://localhost:8761](http://localhost:8761)
- **Customer Service**: [http://localhost:8080/api/customers](http://localhost:8080/api/customers)
- **Account Service**: [http://localhost:8080/api/accounts](http://localhost:8080/api/accounts)
- **Card Service**: [http://localhost:8080/api/cards](http://localhost:8080/api/cards)

## Configuration
### Environment Variables
Set the following environment variables in `compose.yaml`:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`

### Application Properties
Each service has its own `application.yml` file for local development.

## Testing
Run unit tests using Maven:
```bash
mvn test
```

## Key Features
- **Service Discovery**: Eureka for dynamic service registration and discovery.
- **API Gateway**: Centralized routing and load balancing.
- **Database Integration**: PostgreSQL for persistent storage.
- **Inter-Service Communication**: RestTemplate for RESTful communication.

## Project Structure
- `common`: Shared DTOs and exceptions.
- `eureka-server`: Service discovery.
- `customer-service`: Customer management.
- `account-service`: Account management.
- `card-service`: Card management.
- `gateway-server`: API gateway.