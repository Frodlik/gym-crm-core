# Gym CRM Core
**CRM system for gym management.**

[![Build](https://github.com/Frodlik/gym-crm-core/actions/workflows/sonarcloud.yml/badge.svg?branch=dev)](https://github.com/Frodlik/gym-crm-core/actions/workflows/sonarcloud.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Frodlik_gym-crm-core&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Frodlik_gym-crm-core)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Frodlik_gym-crm-core&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Frodlik_gym-crm-core)

# Prerequisites
Before you build or run the project, make sure the following software is installed on your machine:

| Technology | Minimum Version |
| ---------- |-----------------|
| Java       | 21              |
| Maven      | 3.8+            |
| Docker     | 20.10+          |
| MySQL      | 8.0+            |

> ⚠️ **Important:** Docker must be running for tests to execute successfully as project uses Testcontainers for integration testing.

# Getting Started (Local Setup)

1. ## Database Setup (MySQL)
**Before the first run, make sure to create a database and user with proper privileges:**

```sql
CREATE DATABASE gym_crm;
CREATE USER 'gcauser'@'localhost' IDENTIFIED BY 'gcauser';
GRANT ALL PRIVILEGES ON gym_crm.* TO 'gcauser'@'localhost';
```
2. ## Environment Variables
Create a .env file in the root directory of the project with the following configuration:

```
# Database Configuration
DB_USERNAME=gcauser
DB_PASSWORD=gcauser
DB_URL=jdbc:mysql://localhost:3306/gym_crm

# Liquibase Configuration
LIQUIBASE_CONTEXTS=dev
DB_SCHEMA=gym_crm
```

3. ## Run the Application
```bash
# Build the project
mvn clean compile

# Run tests (requires Docker to be running)
mvn test

# Start the application
mvn spring-boot:run
```
The application will be available at http://localhost:8080/

4. ## Testing Requirements
**Docker must be running** before executing tests. The integration tests use Testcontainers to:

* Automatically provision MySQL test containers
* Run tests against real database instances
* Ensure test isolation and consistency

If Docker is not running, tests will fail with connection errors.

## Environment Profiles

The application supports multiple environments with different configurations:

| Profile | Description | Logging Level |
|---------|-------------|---------------|
| `local` | Development with detailed SQL logging | DEBUG |
| `dev` | Development environment | DEBUG |
| `stg` | Staging environment | INFO |
| `prod` | Production environment | WARN |

To run with a specific profile:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 📊 Monitoring & Metrics
- **Health Check:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **Metrics:** [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)
- **Prometheus metric:** `http://localhost:8080/actuator/metrics/{requiredMetricName}`

# API Documentation

Once the application is running, you can access the interactive API documentation through **Swagger UI**:

## 🔗 Swagger UI
**[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/gym-crm-core/swagger-ui/index.html)**

## 📮 Postman Collection

For comprehensive API testing, we provide a complete Postman collection with all endpoints, examples, and environment configurations.

### 📁 Collection Files:
- **Collection:** [`postman/Gym-CRM-API.postman_collection.json`](/src/main/resources/postman/Gym-CRM-API.postman_collection.json)

### 📊 Collection Structure:
```
📮 Gym CRM API
├── 🔐 Authentication
│   ├── Login
│   └── Change Password
├── 👥 Trainee
│   ├── Register Trainee
│   ├── Get Trainee Profile
│   ├── Update Trainee Profile
│   ├── Delete Trainee Profile
│   ├── Get Trainee Trainings
│   ├── Get Available Trainers
│   ├── Update Trainee Trainers
│   └── Activate/Deactivate Trainee
├── 🏋️ Trainer
│   ├── Register Trainer
│   ├── Get Trainer Profile
│   ├── Update Trainer Profile
│   ├── Get Trainer Trainings
│   └── Activate/Deactivate Trainer
└── 📚 Training
    ├── Add Training
    └── Get Training Types
```

### 🧪 Testing Workflow:

1. **Start with Registration:**
   ```
   Register Trainee/Trainer → Note generated credentials
   ```

2. **Authentication:**
   ```
   Login with generated credentials
   ```

3. **Profile Management:**
   ```
   Get Profile → Update Profile → Manage Status
   ```

4. **Training Management:**
   ```
   Assign Trainers → Create Training → View Training History
   ```
