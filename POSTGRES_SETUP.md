# PostgreSQL Setup for Cicloza

This guide will help you set up PostgreSQL for the Cicloza application.

## Prerequisites

1. PostgreSQL installed on your system
2. PostgreSQL server running

## Setup Steps

### 1. Install PostgreSQL (if not already installed)

#### macOS (using Homebrew)
```bash
brew install postgresql@16
brew services start postgresql@16
```

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 2. Create Database and User

You can use the provided `db-setup.sql` script or follow these manual steps:

#### Using the script
```bash
psql -U postgres -f db-setup.sql
```

#### Manual setup
```bash
# Connect to PostgreSQL
psql -U postgres
psql postgres # if the above command will not work 

# Create database
CREATE DATABASE cicloza;

# Connect to the created database
\c cicloza

# The tables will be automatically created by Hibernate when the application starts
```

### 3. Configure Application

The application is already configured with the following database settings in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cicloza
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

You might need to update the username and password according to your PostgreSQL setup.

### 4. Start the Application

Run the application using Maven:

```bash
./mvnw spring-boot:run
```

## Testing the Setup

Once the application is running, you can test the database connection by accessing the API endpoints:

- GET all user: `http://localhost:8080/api/user`
- GET user by ID: `http://localhost:8080/api/user/{id}`
- POST create user: `http://localhost:8080/api/user`
- PUT update user: `http://localhost:8080/api/user/{id}`
- DELETE user: `http://localhost:8080/api/user/{id}`

Example POST request body:
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phone": "+0987654321"
}
```

## Troubleshooting

1. If you encounter connection issues, check that PostgreSQL is running:
   ```bash
   # macOS
   brew services list
   
   # Ubuntu/Debian
   sudo systemctl status postgresql
   ```

2. If you can't connect with the default credentials, you may need to create a PostgreSQL user:
   ```sql
   CREATE USER your_username WITH PASSWORD 'your_password';
   ALTER ROLE your_username WITH CREATEDB;
   ```