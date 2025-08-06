# Cicloza Project

## 🏗️ Project Structure

```
cicloza/
├── Quanta/          # Backend (Spring Boot Application)
│   ├── src/         # Java source code
│   ├── pom.xml      # Maven configuration
│   └── run.sh       # Setup and startup scripts
├── Pulse/           # Frontend (Future development)
└── README.md        # This file
```

## 🚀 Quick Start

### Backend Development
```bash
cd Quanta/
./run.sh --setup    # First time setup
./run.sh            # Start application
```

### Important Notes
- **Always run Maven commands from `Quanta/` directory**
- **Never run `mvn` commands from project root**
- **The `target/` directory should only exist in `Quanta/`**

## 📂 Directory Guidelines

### ✅ Correct Maven Usage:
```bash
cd Quanta/
./mvnw clean install
./mvnw spring-boot:run
```

### ❌ Incorrect Maven Usage:
```bash
# DON'T do this from project root:
mvn clean install     # Creates target/ at wrong location
```

## 🌳 Branch Strategy

- **`cicloza_dev`** - Default development branch
- **`main`** - Staging branch  
- **`master`** - Production branch

## 📚 Documentation

- **Backend Setup:** `Quanta/README.md`
- **Database Setup:** `Quanta/POSTGRES_SETUP.md`
- **API Documentation:** Available at `http://localhost:8080` when running

## 🛠️ Development Workflow

1. **Clone and setup:**
   ```bash
   git clone <repository>
   cd cicloza/Quanta/
   ./run.sh --setup
   ```

2. **Start development:**
   ```bash
   ./run.sh --start-application
   ```

3. **Access application:**
   - **Backend API:** http://localhost:8080
   - **Frontend:** (Coming soon in Pulse/)

---

⚠️ **Remember:** Always work in the appropriate subdirectory (`Quanta/` for backend, `Pulse/` for frontend)