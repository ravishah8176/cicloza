# Cicloza Application

The main repository of the Cicloza Spring Boot application.

## 🚀 Quick Start

### Prerequisites
- **Java 21** or later
- **PostgreSQL** database server
- **Git** (for cloning the repository)

### Running the Application

#### Option 1: Use the Startup Scripts (Recommended)

**For macOS/Linux:**
```bash
chmod +x run.sh
./run.sh
```

**For Windows:**
```cmd
run.bat
```

#### Option 2: Manual Commands

**macOS/Linux:**
```bash
./mvnw clean spring-boot:run
```

**Windows:**
```cmd
mvnw.cmd clean spring-boot:run
```

## 📋 Setup Instructions

### 1. Database Setup
This application requires PostgreSQL. See [`POSTGRES_SETUP.md`](POSTGRES_SETUP.md) for detailed installation and setup instructions for both macOS and Windows.

**Quick Database Setup:**
```sql
-- Create database
CREATE DATABASE cicloza;
```

### 2. Application Configuration
The application is configured to connect to:
- **Database:** `cicloza`
- **Host:** `localhost:5432`
- **Username:** `postgres`
- **Password:** `postgres`

You can modify these settings in `src/main/resources/application.yml`.

### 3. First Time Setup
1. Clone the repository
2. Install PostgreSQL (see POSTGRES_SETUP.md)
3. Create the `cicloza` database
4. Run the application using the startup scripts

## 🌐 Application URLs

Once started, the application will be available at:
- **Main Application:** http://localhost:8080
- **API Base URL:** http://localhost:8080/api

## 🛠️ Development

### Available Maven Commands
```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw test

# Package the application
./mvnw package

# Run with hot reload (development)
./mvnw spring-boot:run
```

### Project Structure
```
src/
├── main/
│   ├── java/com/cicloza/
│   │   ├── controller/     # REST Controllers
│   │   ├── service/        # Business Logic
│   │   ├── entity/         # JPA Entities
│   │   ├── repository/     # Data Access Layer
│   │   ├── dto/            # Data Transfer Objects
│   │   └── util/           # Utility Classes
│   └── resources/
│       ├── application.yml # Application Configuration
│       └── static/         # Static Web Resources
└── test/                   # Test Classes
```

## 📚 API Documentation

### Main Controllers
- **UserController** - User management operations
- **SerialPortController** - Serial port communication

## 🔧 Troubleshooting

### Common Issues

1. **Application won't start**
   - Ensure Java 21+ is installed
   - Check PostgreSQL is running
   - Verify database `cicloza` exists

2. **Database connection errors**
   - Check PostgreSQL service status
   - Verify credentials in `application.yml`
   - See POSTGRES_SETUP.md for detailed troubleshooting

3. **Port already in use**
   - Default port is 8080
   - Kill existing processes or change port in `application.yml`

### Checking Prerequisites
Use the check mode in startup scripts:
```bash
# macOS/Linux
./run.sh --check

# Windows
run.bat --check
```

## 📖 Additional Documentation
- [`POSTGRES_SETUP.md`](POSTGRES_SETUP.md) - Comprehensive PostgreSQL setup guide
- [`HELP.md`](HELP.md) - Spring Boot reference documentation