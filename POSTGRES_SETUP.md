# 🐘 PostgreSQL Setup Guide for Cicloza Application

> **Complete installation and configuration guide for PostgreSQL on macOS and Windows**

## 📋 Quick Start Checklist

- [ ] Install PostgreSQL
- [ ] Start PostgreSQL service  
- [ ] Create `cicloza` database
- [ ] Configure application credentials
- [ ] Test connection
- [ ] Run Spring Boot application

---

## 📖 Table of Contents

| Section | Description |
|---------|-------------|
| [🍎 macOS Setup](#-macos-setup) | Complete setup for Mac users |
| [🪟 Windows Setup](#-windows-setup) | Complete setup for Windows users |
| [⚙️ Application Configuration](#️-application-configuration) | Spring Boot configuration |
| [✅ Testing & Verification](#-testing--verification) | How to test your setup |
| [🛠️ Troubleshooting](#️-troubleshooting) | Common issues and solutions |

---

## 🍎 macOS Setup

### 📦 Installation

#### **Prerequisites**
- macOS 10.14 or later
- Homebrew installed ([Install Homebrew](https://brew.sh/))
- Terminal access

#### **Install PostgreSQL**
```bash
# Install PostgreSQL 16 (Recommended - Stable)
brew install postgresql@16
brew services start postgresql@16

# Alternative: PostgreSQL 17 (Latest)
brew install postgresql@17
brew services start postgresql@17

# Verify installation
brew services list | grep postgresql
psql --version
```

### 🗄️ Database Setup

#### **Option A: Quick Setup (Default User)**
```bash
# Connect to PostgreSQL
psql postgres

# Create database
CREATE DATABASE cicloza;

# Verify and exit
\l
\q
```

#### **Option B: Dedicated User (Recommended)**
```bash
# Connect to PostgreSQL
psql postgres

# Create user and database
CREATE USER cicloza_user WITH PASSWORD 'cicloza123';
ALTER ROLE cicloza_user WITH CREATEDB;
CREATE DATABASE cicloza OWNER cicloza_user;
GRANT ALL PRIVILEGES ON DATABASE cicloza TO cicloza_user;

# Exit and test
\q
psql -U cicloza_user -d cicloza -c "SELECT current_user;"
```

### ✅ **macOS Verification**
```bash
# Check service status
brew services list | grep postgresql

# Test connection
psql -U postgres -c "SELECT version();"
```

---

## 🪟 Windows Setup

### 📦 Installation

#### **Method 1: Windows Package Manager (Recommended)**
```cmd
# Search available packages
winget search postgresql

# Install latest version
winget install PostgreSQL.PostgreSQL.17

# Alternative: Stable version
winget install PostgreSQL.PostgreSQL.16
```

#### **Method 2: Official Installer**
1. 🌐 Visit: https://www.postgresql.org/download/windows/
2. 📥 Download installer for your Windows version
3. 🚀 Run installer and follow setup wizard
4. 🔑 **Important**: Remember the password for `postgres` user
5. 🔧 pgAdmin 4 will be installed automatically

### 🚀 Service Management

#### **Start PostgreSQL Service**
```cmd
# Open Command Prompt as Administrator (Windows + X → Terminal Admin)

# Start service
net start postgresql-x64-17

# Check status
sc query postgresql-x64-17

# Expected output:
# SERVICE_NAME: postgresql-x64-17
#         STATE              : 4  RUNNING
```

### 🛤️ Add to PATH (Recommended)

#### **Method 1: System Environment Variables (Windows-wide)**
1. Press `Windows + R` → type `sysdm.cpl` → Enter
2. Click "Environment Variables"
3. Under "System Variables" → find "Path" → "Edit"
4. Click "New" → add `C:\Program Files\PostgreSQL\17\bin`
5. **Restart terminal**

#### **Method 2: Git Bash Only (Permanent)**

**Option A: Using echo command**
```bash
# Create ~/.bashrc if it doesn't exist
touch ~/.bashrc

# Add PostgreSQL to PATH permanently for all Git Bash sessions
echo 'export PATH="$PATH:/c/Program Files/PostgreSQL/17/bin"' >> ~/.bashrc

# Apply changes to current session
source ~/.bashrc

# Verify PATH is set
echo $PATH | grep PostgreSQL
psql --version
```

**Option B: Using vim editor**
```bash
# Create or edit ~/.bashrc with vim
vim ~/.bashrc

# In vim, press 'i' to enter insert mode, then add:
# export PATH="$PATH:/c/Program Files/PostgreSQL/17/bin"
# 
# Save and exit:
# Press ESC, then type :wq and press Enter

# Apply changes to current session
source ~/.bashrc

# Verify PATH is set
psql --version
```

#### **Method 3: Temporary (Current Session Only)**
```bash
# In Git Bash (resets when terminal closes)
export PATH="$PATH:/c/Program Files/PostgreSQL/17/bin"
```

### 🗄️ Database Setup

#### **Option A: Quick Setup**
```cmd
# One-line database creation
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -c "CREATE DATABASE cicloza;"
```

#### **Option B: Dedicated User (Recommended)**
```cmd
# Connect to PostgreSQL
psql -U postgres
# OR: "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres

# Create user and database
CREATE USER cicloza_user WITH PASSWORD 'cicloza123';
ALTER ROLE cicloza_user WITH CREATEDB;
CREATE DATABASE cicloza OWNER cicloza_user;
GRANT ALL PRIVILEGES ON DATABASE cicloza TO cicloza_user;

# Exit and test
\q
psql -U cicloza_user -d cicloza -c "SELECT current_user;"
```

---

## ⚙️ Application Configuration

### 📝 Update `application.properties`

#### **Configuration Options**

| Setup Type | Username | Password | Use Case |
|------------|----------|----------|----------|
| Default User | `postgres` | `postgres` | Quick setup, development |
| Dedicated User | `cicloza_user` | `cicloza123` | Recommended, production-ready |

#### **Option 1: Default PostgreSQL User**
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/cicloza
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

#### **Option 2: Dedicated User (Recommended)**
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/cicloza
spring.datasource.username=cicloza_user
spring.datasource.password=cicloza123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 🚀 **Start Application**

| Platform | Command |
|----------|---------|
| **macOS/Linux** | `./mvnw spring-boot:run` |
| **Windows** | `mvnw.cmd spring-boot:run` |

---

## ✅ Testing & Verification

### 🔗 **Database Connection Test**

```bash
# Test with postgres user
psql -U postgres -d cicloza -c "SELECT 'Connection successful!' as status;"

# Test with dedicated user
psql -U cicloza_user -d cicloza -c "SELECT 'Connection successful!' as status;"

# List all databases
psql -U postgres -c "\l"
```

### 🌐 **Application Health Check**

Once Spring Boot starts, verify these work:
- ✅ Application starts without errors
- ✅ Database tables are created automatically
- ✅ No connection errors in logs

---

## 🛠️ Troubleshooting

### 🔐 **Issue 1: Password Authentication Failed**

<details>
<summary><strong>🍎 macOS Solution</strong></summary>

```bash
# Check if PostgreSQL is running
brew services list | grep postgresql

# Start if not running
brew services start postgresql@16

# Reset password if needed
psql postgres -c "ALTER USER postgres PASSWORD 'postgres';"
```
</details>

<details>
<summary><strong>🪟 Windows Solution</strong></summary>

```cmd
# Run as Administrator

# 1. Stop service
net stop postgresql-x64-17

# 2. Edit authentication config
notepad "C:\Program Files\PostgreSQL\17\data\pg_hba.conf"
# Find: host    all    all    127.0.0.1/32    scram-sha-256
# Change to: host    all    all    127.0.0.1/32    trust

# 3. Start service and reset password
net start postgresql-x64-17
psql -U postgres
ALTER USER postgres PASSWORD 'postgres';
\q

# 4. Restore security (change 'trust' back to 'scram-sha-256')
# 5. Restart service
net stop postgresql-x64-17
net start postgresql-x64-17
```
</details>

### 🚫 **Issue 2: Service Not Running**

| Platform | Check Status | Start Service | Restart Service |
|----------|--------------|---------------|-----------------|
| **macOS** | `brew services list \| grep postgresql` | `brew services start postgresql@16` | `brew services restart postgresql@16` |
| **Windows** | `sc query postgresql-x64-17` | `net start postgresql-x64-17` | `net stop postgresql-x64-17 && net start postgresql-x64-17` |

### 📝 **Issue 3: Command Not Found (psql)**

<details>
<summary><strong>🍎 macOS Solution</strong></summary>

```bash
# Add to PATH permanently
echo 'export PATH="/usr/local/opt/postgresql@16/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Or use full path
/usr/local/opt/postgresql@16/bin/psql --version
```
</details>

<details>
<summary><strong>🪟 Windows Solution</strong></summary>

**Option 1: Create/Update .bashrc (Git Bash permanent solution)**

*Method A: Using echo command*
```bash
# Create ~/.bashrc if it doesn't exist
touch ~/.bashrc

# Add PostgreSQL to PATH permanently
echo 'export PATH="$PATH:/c/Program Files/PostgreSQL/17/bin"' >> ~/.bashrc

# Apply changes immediately
source ~/.bashrc

# Test
psql --version
```

*Method B: Using vim editor*
```bash
# Create or edit ~/.bashrc with vim
vim ~/.bashrc

# In vim:
# 1. Press 'i' to enter insert mode
# 2. Add this line: export PATH="$PATH:/c/Program Files/PostgreSQL/17/bin"
# 3. Press ESC to exit insert mode
# 4. Type :wq and press Enter to save and quit

# Apply changes immediately
source ~/.bashrc

# Test
psql --version
```

**Option 2: Use full path**
```cmd
# Direct command with full path
"C:\Program Files\PostgreSQL\17\bin\psql.exe" --version

# Or create alias in .bashrc
echo 'alias psql="\"C:\Program Files\PostgreSQL\17\bin\psql.exe\""' >> ~/.bashrc
source ~/.bashrc
```

**Option 3: Add to System PATH**
```
System Properties → Environment Variables → Path → Add:
C:\Program Files\PostgreSQL\17\bin
```
</details>

### 🌐 **Issue 4: Connection Refused**

```bash
# Check if PostgreSQL is listening on port 5432
# macOS/Linux:
netstat -an | grep 5432
lsof -i :5432

# Windows:
netstat -an | findstr 5432
```

### 🗄️ **Issue 5: Database Does Not Exist**

```sql
-- Connect and list databases
psql -U postgres
\l

-- Create database if missing
CREATE DATABASE cicloza;
\c cicloza
\q
```

---

## 📚 **Quick Reference**

### **Common Commands**

| Task | macOS | Windows |
|------|-------|---------|
| **Start Service** | `brew services start postgresql@16` | `net start postgresql-x64-17` |
| **Stop Service** | `brew services stop postgresql@16` | `net stop postgresql-x64-17` |
| **Check Status** | `brew services list \| grep postgresql` | `sc query postgresql-x64-17` |
| **Connect to DB** | `psql -U postgres -d cicloza` | `psql -U postgres -d cicloza` |
| **Full Path psql** | `/usr/local/opt/postgresql@16/bin/psql` | `"C:\Program Files\PostgreSQL\17\bin\psql.exe"` |

### **PATH Setup Methods (Windows)**

| Method | Scope | Persistence | Commands |
|--------|-------|-------------|----------|
| **System Variables** | All programs | Permanent | System Properties → Environment Variables |
| **Git Bash (.bashrc)** | Git Bash only | Permanent | `echo 'export PATH="$PATH:/c/Program Files/PostgreSQL/17/bin"' >> ~/.bashrc` |
| **Current Session** | Current terminal | Temporary | `export PATH="$PATH:/c/Program Files/PostgreSQL/17/bin"` |

### **Vim Commands for .bashrc Editing**

| Action | Command | Description |
|--------|---------|-------------|
| **Open/Create** | `vim ~/.bashrc` | Open .bashrc in vim |
| **Insert Mode** | `i` | Start editing/inserting text |
| **Exit Insert** | `ESC` | Exit insert mode |
| **Save & Quit** | `:wq` | Write (save) and quit |
| **Quit without Save** | `:q!` | Quit without saving changes |
| **Navigate** | Arrow keys | Move cursor around |
| **Delete Line** | `dd` | Delete entire line |
| **Undo** | `u` | Undo last change |

### **Default Credentials**

| Setup | Username | Password | Database |
|-------|----------|----------|----------|
| **Quick Setup** | `postgres` | `postgres` | `cicloza` |
| **Dedicated User** | `cicloza_user` | `cicloza123` | `cicloza` |

---

## 🔗 **Additional Resources**

| Resource | Link |
|----------|------|
| **PostgreSQL Documentation** | https://www.postgresql.org/docs/ |
| **pgAdmin 4 Guide** | https://www.pgadmin.org/docs/ |
| **Spring Boot JPA Guide** | https://spring.io/guides/gs/accessing-data-jpa/ |
| **Homebrew** | https://brew.sh/ |

---

## 🔒 **Security Best Practices**

> ⚠️ **Production Recommendations**

1. 🔑 **Never use default passwords in production**
2. 💪 **Use strong, unique passwords for database users**  
3. 🚪 **Restrict database access to necessary users only**
4. 🔄 **Keep PostgreSQL updated to latest stable version**
5. 🔐 **Enable SSL connections for production environments**
6. 🛡️ **Configure proper firewall rules**
7. 📝 **Regular database backups**

---

> **✅ Setup Complete!** Your PostgreSQL database is now ready for the Cicloza application.