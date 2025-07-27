# Gym CRM Core
**CRM system for gym management.**

# Prerequisites
Before you build or run the project, make sure the following software is installed on your machine:

| Technology | Minimum Version |
| ---------- |-----------------|
| Java       | 21              |
| Maven      | 3.8+            |
| Docker     | 20.10+          |
| MySQL      | 8.0+            |

Docker is required to run integration tests via Testcontainers.
> *Optional:* You can use MySQL Workbench or any other database client to inspect the schema, test queries, or browse data.


# Getting Started (Local Setup)

1. ## Database Setup (MySQL)
**Before the first run, make sure to create a database and user with proper privileges:**

```sql
CREATE DATABASE gym_crm;
CREATE USER 'gcauser'@'localhost' IDENTIFIED BY 'gcauser';
GRANT ALL PRIVILEGES ON gym_crm.* TO 'gcauser'@'localhost';
```
2. ## Environment Variables
To run the application locally, define the following environment variables in your run configuration.

```
DB_URL=jdbc:mysql://localhost:3306/gym_crm;
DB_USERNAME=gcauser;
DB_PASSWORD=gcauser;
```

# API Documentation

Once the application is running, you can access the interactive API documentation through **Swagger UI**:

## 🔗 Swagger UI
**[http://localhost:8080/gym-crm-core/swagger-ui/index.html](http://localhost:8080/gym-crm-core/swagger-ui/index.html)**

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
