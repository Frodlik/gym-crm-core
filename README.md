# Gym CRM Core
**CRM system for gym management.**

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

