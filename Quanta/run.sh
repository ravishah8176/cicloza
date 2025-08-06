#!/bin/bash

# =============================================================================
# Cicloza Application Startup Script
# =============================================================================
# This script provides comprehensive setup and startup functionality for the
# Cicloza Spring Boot application with automated prerequisite installation.
# =============================================================================

set -e  # Exit on any error

# =============================================================================
# COLOR DEFINITIONS AND OUTPUT FUNCTIONS
# =============================================================================

# Color codes for formatted output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Formatted output functions
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "\n${BLUE}=====================================${NC}"
    echo -e "${BLUE}  Cicloza Application Startup${NC}"
    echo -e "${BLUE}=====================================${NC}\n"
}

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

# Check if a command exists in the system
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# =============================================================================
# PREREQUISITE VALIDATION FUNCTIONS
# =============================================================================

# Validate Java installation and version
check_java() {
    print_status "Checking Java installation..."
    
    if ! command_exists java; then
        print_error "Java is not installed!"
        print_status "Please install Java 21 or later:"
        print_status "  macOS: brew install openjdk@21"
        print_status "  Linux: sudo apt install openjdk-21-jdk (Ubuntu/Debian)"
        return 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        print_error "Java 21 or later is required. Found Java $JAVA_VERSION"
        return 1
    fi
    
    print_success "Java $JAVA_VERSION found"
    return 0
}

# Validate PostgreSQL installation and service status
check_postgresql() {
    print_status "Checking PostgreSQL..."
    
    if ! command_exists psql; then
        print_error "PostgreSQL is not installed or not in PATH!"
        print_status "Please install PostgreSQL:"
        print_status "  macOS: brew install postgresql@16"
        print_status "         brew services start postgresql@16"
        print_status "See POSTGRES_SETUP.md for detailed instructions"
        return 1
    fi
    
    # Verify PostgreSQL service is running
    if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
        print_warning "PostgreSQL service is not running!"
        print_status "Starting PostgreSQL service..."
        
        if command_exists brew; then
            if brew services list | grep -q "postgresql.*started"; then
                print_success "PostgreSQL service is already managed by Homebrew"
            else
                print_status "Attempting to start PostgreSQL via Homebrew..."
                brew services start postgresql@16 || brew services start postgresql@17 || {
                    print_error "Failed to start PostgreSQL via Homebrew"
                    print_status "Please start PostgreSQL manually or check POSTGRES_SETUP.md"
                    return 1
                }
            fi
        else
            print_error "Please start PostgreSQL service manually"
            print_status "See POSTGRES_SETUP.md for instructions"
            return 1
        fi
    fi
    
    print_success "PostgreSQL is running"
    return 0
}

# Validate database existence and connectivity
check_database() {
    print_status "Checking database connection..."
    
    # Use a single command to check if database exists
    if psql -U postgres -l 2>/dev/null | grep -q "cicloza"; then
        print_success "Database 'cicloza' exists and is accessible"
        return 0
    else
        print_error "Database 'cicloza' not found"
        print_status "Please ensure PostgreSQL is running and database is created"
        print_status "Run: psql -U postgres -c \"CREATE DATABASE cicloza;\""
        print_status "Or see POSTGRES_SETUP.md for detailed setup instructions"
        return 1
    fi
}

# Validate Maven wrapper files and configuration
check_maven() {
    print_status "Checking Maven wrapper..."
    
    if [ ! -f "./mvnw" ]; then
        print_error "Maven wrapper (mvnw) not found!"
        print_warning "The Maven wrapper files are missing from this project."
        print_status "Please install Maven wrapper first before running this application."
        echo
        print_status "To install Maven wrapper, run these commands in order:"
        echo -e "  ${BLUE}1.${NC} Ensure you have Maven installed globally:"
        echo -e "     ${YELLOW}mvn --version${NC}"
        echo -e "  ${BLUE}2.${NC} Initialize Maven wrapper in project directory:"
        echo -e "     ${YELLOW}mvn wrapper:wrapper${NC}"
        echo -e "  ${BLUE}3.${NC} Verify wrapper files were created:"
        echo -e "     ${YELLOW}ls -la mvnw* .mvn/${NC}"
        echo -e "  ${BLUE}4.${NC} Run the check command again:"
        echo -e "     ${YELLOW}./run.sh --check${NC}"
        echo
        print_status "Alternative: If you don't have Maven installed globally:"
        echo -e "  ${BLUE}•${NC} Download Maven from: https://maven.apache.org/download.cgi"
        echo -e "  ${BLUE}•${NC} Or install via package manager:"
        echo -e "    macOS: ${YELLOW}brew install maven${NC}"
        echo -e "    Windows: ${YELLOW}winget install Apache.Maven${NC}"
        echo
        return 1
    fi
    
    # Verify .mvn directory exists
    if [ ! -d ".mvn" ]; then
        print_error "Maven wrapper configuration (.mvn directory) not found!"
        print_status "Please reinstall Maven wrapper:"
        echo -e "  ${YELLOW}mvn wrapper:wrapper${NC}"
        return 1
    fi
    
    # Make mvnw executable
    chmod +x ./mvnw
    print_success "Maven wrapper is ready"
    return 0
}

# =============================================================================
# USER INTERFACE FUNCTIONS
# =============================================================================

# Display help information and usage instructions
show_help() {
    echo "Cicloza Application Startup Script"
    echo
    echo "Usage: $0 [OPTION]"
    echo
    echo "Options:"
    echo "  -s, --setup-instruction    Show complete setup instructions"
    echo "  -c, --check                Only check prerequisites without starting the app"
    echo "  -m, --setup                Automatically install and setup all requirements"
    echo "  -a, --start-application    Start the application (no prerequisite checks)"
    echo "  -h, --help                 Show this help message"
    echo
    echo "Prerequisites:"
    echo "  - Java 21 or later"
    echo "  - PostgreSQL database server"
    echo "  - 'cicloza' database created"
    echo "  - Maven wrapper files (mvnw, .mvn/)"
    echo
    echo "Quick setup workflow (run in order):"
    echo "  1. ./run.sh --setup-instruction    # Show complete setup instructions"
    echo "  2. ./run.sh --check                # Verify all prerequisites are met"
    echo "  3. ./run.sh --setup                # Automatically install and setup everything"
    echo "  4. ./run.sh --start-application    # Start the application"
    echo "  5. ./run.sh --help                 # Get help if needed"
    echo
    echo "For detailed setup instructions:"
    echo "  - ./run.sh --setup-instruction (comprehensive guide)"
    echo "  - POSTGRES_SETUP.md (database setup)"
    echo "  - README.md (project overview)"
}

# Display comprehensive setup instructions
show_setup_instruction() {
    print_header
    echo -e "${GREEN}📋 COMPLETE SETUP GUIDE FOR CICLOZA APPLICATION${NC}"
    echo
    echo -e "${BLUE}==== STEP 1: INSTALL PREREQUISITES ====${NC}"
    echo
    echo -e "${YELLOW}Java 21+ Installation:${NC}"
    echo "  macOS:    brew install openjdk@21"
    echo "  Windows:  winget install Microsoft.OpenJDK.21"
    echo "  Linux:    sudo apt install openjdk-21-jdk"
    echo "  Verify:   java --version"
    echo
    echo -e "${YELLOW}PostgreSQL Installation:${NC}"
    echo "  macOS:    brew install postgresql@16 && brew services start postgresql@16"
    echo "  Windows:  winget install PostgreSQL.PostgreSQL.17"
    echo "  Linux:    sudo apt install postgresql postgresql-contrib"
    echo "  Verify:   psql --version"
    echo
    echo -e "${YELLOW}Maven Installation:${NC}"
    echo "  macOS:    brew install maven"
    echo "  Windows:  winget install Apache.Maven"
    echo "  Linux:    sudo apt install maven"
    echo "  Verify:   mvn --version"
    echo
    echo -e "${BLUE}==== STEP 2: DATABASE SETUP ====${NC}"
    echo
    echo "  1. Start PostgreSQL service"
    echo "  2. Create database: psql -U postgres -c \"CREATE DATABASE cicloza;\""
    echo "  3. Test connection: psql -U postgres -d cicloza -c \"SELECT 1;\""
    echo
    echo -e "${BLUE}==== STEP 3: MAVEN WRAPPER SETUP ====${NC}"
    echo
    echo "  1. Navigate to project directory: cd /path/to/cicloza/Quanta"
    echo "  2. Initialize Maven wrapper: mvn wrapper:wrapper"
    echo "  3. Verify files created: ls -la mvnw* .mvn/"
    echo
    echo -e "${BLUE}==== STEP 4: VERIFY SETUP ====${NC}"
    echo
    echo "  1. Check prerequisites: ./run.sh --check           # STEP 2"
    echo "  2. Auto-setup everything: ./run.sh --setup        # STEP 3"
    echo "  3. Start application: ./run.sh --start-application # STEP 4"
    echo "  4. Get help if needed: ./run.sh --help            # STEP 5"
    echo
    echo -e "${GREEN}🎯 After successful setup, your application will be available at:${NC}"
    echo -e "   ${YELLOW}http://localhost:8080${NC}"
    echo
    echo -e "${BLUE}For detailed database setup, see: POSTGRES_SETUP.md${NC}"
}

# =============================================================================
# SOFTWARE INSTALLATION FUNCTIONS
# =============================================================================

# Install Java 21+ based on operating system
install_java() {
    print_status "Installing Java 21..."
    case "$(uname -s)" in
        Darwin*)
            if command_exists brew; then
                brew install openjdk@21
                echo 'export PATH="/usr/local/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
                print_success "Java 21 installed via Homebrew"
            else
                print_error "Homebrew not found. Please install Homebrew first or install Java manually."
                exit 1
            fi
            ;;
        MINGW*|CYGWIN*)
            if command_exists winget; then
                winget install Microsoft.OpenJDK.21 --accept-source-agreements --accept-package-agreements
                print_success "Java 21 installed via winget"
            else
                print_error "winget not found. Please install Java 21 manually from: https://adoptium.net/"
                exit 1
            fi
            ;;
        Linux*)
            if command_exists apt; then
                sudo apt update && sudo apt install -y openjdk-21-jdk
                print_success "Java 21 installed via apt"
            elif command_exists yum; then
                sudo yum install -y java-21-openjdk-devel
                print_success "Java 21 installed via yum"
            else
                print_error "Package manager not found. Please install Java 21 manually."
                exit 1
            fi
            ;;
        *)
            print_error "Unsupported operating system. Please install Java 21 manually."
            exit 1
            ;;
    esac
}

# Install PostgreSQL based on operating system
install_postgresql() {
    print_status "Installing PostgreSQL..."
    case "$(uname -s)" in
        Darwin*)
            if command_exists brew; then
                brew install postgresql@16
                brew services start postgresql@16
                print_success "PostgreSQL installed and started via Homebrew"
            else
                print_error "Homebrew not found. Please install Homebrew first."
                exit 1
            fi
            ;;
        MINGW*|CYGWIN*)
            if command_exists winget; then
                winget install PostgreSQL.PostgreSQL.17 --accept-source-agreements --accept-package-agreements
                print_success "PostgreSQL installed via winget"
                print_warning "Please start PostgreSQL service manually: net start postgresql-x64-17"
            else
                print_error "winget not found. Please install PostgreSQL manually."
                exit 1
            fi
            ;;
        Linux*)
            if command_exists apt; then
                sudo apt update && sudo apt install -y postgresql postgresql-contrib
                sudo systemctl start postgresql
                sudo systemctl enable postgresql
                print_success "PostgreSQL installed and started via apt"
            elif command_exists yum; then
                sudo yum install -y postgresql postgresql-server
                sudo postgresql-setup initdb
                sudo systemctl start postgresql
                sudo systemctl enable postgresql
                print_success "PostgreSQL installed and started via yum"
            else
                print_error "Package manager not found. Please install PostgreSQL manually."
                exit 1
            fi
            ;;
        *)
            print_error "Unsupported operating system. Please install PostgreSQL manually."
            exit 1
            ;;
    esac
}

# Install Maven with multiple fallback methods
install_maven() {
    print_status "Installing Maven..."
    case "$(uname -s)" in
        Darwin*)
            if command_exists brew; then
                brew install maven
                print_success "Maven installed via Homebrew"
            else
                print_error "Homebrew not found. Please install Homebrew first."
                return 1
            fi
            ;;
        MINGW*|CYGWIN*)
            install_maven_windows
            return $?
            ;;
        Linux*)
            if command_exists apt; then
                sudo apt update && sudo apt install -y maven
                print_success "Maven installed via apt"
            elif command_exists yum; then
                sudo yum install -y maven
                print_success "Maven installed via yum"
            else
                print_error "Package manager not found. Please install Maven manually."
                return 1
            fi
            ;;
        *)
            print_error "Unsupported operating system. Please install Maven manually."
            return 1
            ;;
    esac
}

# =============================================================================
# WINDOWS-SPECIFIC MAVEN INSTALLATION
# =============================================================================

# Install Maven on Windows with multiple fallback methods
install_maven_windows() {
    local installed=false
    
    # Method 1: Chocolatey (most reliable)
    if command_exists choco; then
        print_status "Trying to install Maven via Chocolatey..."
        choco install maven -y >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            print_success "Maven installed via Chocolatey"
            source ~/.bashrc 2>/dev/null || true
            export PATH="$PATH:/c/ProgramData/chocolatey/lib/maven/apache-maven-*/bin"
            installed=true
        fi
    fi
    
    # Method 2: winget with multiple package names
    if [ "$installed" = false ] && command_exists winget; then
        print_status "Trying to install Maven via winget..."
        
        # Try primary package name
        winget install Apache.Maven --accept-source-agreements --accept-package-agreements >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            print_success "Maven installed via winget (Apache.Maven)"
            installed=true
        else
            # Try alternative package names
            print_status "Trying alternative winget package names..."
            winget install Maven.Maven --accept-source-agreements --accept-package-agreements >/dev/null 2>&1
            if [ $? -eq 0 ]; then
                print_success "Maven installed via winget (Maven.Maven)"
                installed=true
            fi
        fi
    fi
    
    # Method 3: Manual download and installation
    if [ "$installed" = false ]; then
        print_status "Attempting manual Maven download and installation..."
        install_maven_manually_windows
        if [ $? -eq 0 ]; then
            installed=true
        fi
    fi
    
    # Validate installation success
    if [ "$installed" = true ]; then
        hash -r  # Clear bash command cache
        if command_exists mvn; then
            print_success "Maven is now available in PATH"
            return 0
        else
            print_warning "Maven installed but not yet in PATH. You may need to restart your terminal."
            return 0
        fi
    else
        print_error "All Maven installation methods failed"
        show_manual_maven_instructions
        return 1
    fi
}

# Manual Maven download and installation for Windows
install_maven_manually_windows() {
    local maven_version="3.9.8"
    local maven_url="https://archive.apache.org/dist/maven/maven-3/${maven_version}/binaries/apache-maven-${maven_version}-bin.zip"
    local install_dir="/c/Program Files/Maven"
    local temp_dir="/tmp/maven-install"
    
    print_status "Downloading Maven ${maven_version}..."
    
    # Create temporary directory
    mkdir -p "$temp_dir"
    
    # Download Maven (try curl first, then wget)
    if command_exists curl; then
        curl -L "$maven_url" -o "$temp_dir/maven.zip" 2>/dev/null
    elif command_exists wget; then
        wget "$maven_url" -O "$temp_dir/maven.zip" 2>/dev/null
    else
        print_error "Neither curl nor wget found. Cannot download Maven."
        return 1
    fi
    
    if [ ! -f "$temp_dir/maven.zip" ]; then
        print_error "Failed to download Maven"
        return 1
    fi
    
    print_status "Extracting Maven..."
    
    # Extract Maven (try unzip first, then 7z)
    if command_exists unzip; then
        unzip -q "$temp_dir/maven.zip" -d "$temp_dir" 2>/dev/null
    elif command_exists 7z; then
        7z x "$temp_dir/maven.zip" -o"$temp_dir" >/dev/null 2>&1
    else
        print_error "No extraction tool found (unzip or 7z required)"
        return 1
    fi
    
    # Create installation directory
    mkdir -p "$install_dir" 2>/dev/null || {
        print_warning "Cannot create $install_dir. Trying user directory..."
        install_dir="$HOME/maven"
        mkdir -p "$install_dir"
    }
    
    # Move Maven to installation directory
    if [ -d "$temp_dir/apache-maven-${maven_version}" ]; then
        cp -r "$temp_dir/apache-maven-${maven_version}"/* "$install_dir/" 2>/dev/null
        if [ $? -eq 0 ]; then
            # Add to PATH in .bashrc
            echo "export PATH=\"\$PATH:$install_dir/bin\"" >> ~/.bashrc
            export PATH="$PATH:$install_dir/bin"
            
            print_success "Maven installed manually to $install_dir"
            
            # Cleanup
            rm -rf "$temp_dir"
            return 0
        fi
    fi
    
    print_error "Failed to install Maven manually"
    rm -rf "$temp_dir"
    return 1
}

# Display manual Maven installation instructions
show_manual_maven_instructions() {
    echo
    print_warning "Automatic Maven installation failed. Please install manually:"
    echo
    echo -e "${YELLOW}Option 1: Install Chocolatey first, then Maven${NC}"
    echo "  1. Open PowerShell as Administrator"
    echo "  2. Install Chocolatey:"
    echo "     Set-ExecutionPolicy Bypass -Scope Process -Force"
    echo "     [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072"
    echo "     iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))"
    echo "  3. Install Maven: choco install maven"
    echo
    echo -e "${YELLOW}Option 2: Manual Download${NC}"
    echo "  1. Download Maven from: https://maven.apache.org/download.cgi"
    echo "  2. Extract to C:\\Program Files\\Maven"
    echo "  3. Add C:\\Program Files\\Maven\\bin to your PATH"
    echo
    echo -e "${YELLOW}Option 3: Use Scoop package manager${NC}"
    echo "  1. Install Scoop: iwr -useb get.scoop.sh | iex"
    echo "  2. Install Maven: scoop install maven"
    echo
}

# =============================================================================
# SERVICE MANAGEMENT FUNCTIONS
# =============================================================================

# Start PostgreSQL service based on operating system
start_postgresql() {
    print_status "Starting PostgreSQL service..."
    case "$(uname -s)" in
        Darwin*)
            if command_exists brew; then
                brew services start postgresql@16 >/dev/null 2>&1 || brew services start postgresql@17 >/dev/null 2>&1
                if [ $? -eq 0 ]; then
                    print_success "PostgreSQL service started"
                else
                    print_warning "PostgreSQL service may already be running"
                fi
            fi
            ;;
        MINGW*|CYGWIN*)
            # Check if service is already running
            sc query postgresql-x64-17 | findstr "RUNNING" >/dev/null 2>&1
            if [ $? -eq 0 ]; then
                print_success "PostgreSQL service already running"
                return 0
            fi
            
            sc query postgresql-x64-16 | findstr "RUNNING" >/dev/null 2>&1
            if [ $? -eq 0 ]; then
                print_success "PostgreSQL service already running"
                return 0
            fi
            
            # Attempt to start the service
            print_status "Attempting to start PostgreSQL service..."
            net start postgresql-x64-17 >/dev/null 2>&1
            if [ $? -eq 0 ]; then
                print_success "PostgreSQL service started (version 17)"
                return 0
            fi
            
            net start postgresql-x64-16 >/dev/null 2>&1
            if [ $? -eq 0 ]; then
                print_success "PostgreSQL service started (version 16)"
                return 0
            fi
            
            print_warning "Could not start PostgreSQL service automatically"
            print_status "You may need to start it manually as Administrator:"
            print_status "  net start postgresql-x64-17"
            print_status "Or check Windows Services for PostgreSQL"
            ;;
        Linux*)
            sudo systemctl start postgresql >/dev/null 2>&1
            if [ $? -eq 0 ]; then
                print_success "PostgreSQL service started"
            else
                print_warning "Could not start PostgreSQL service automatically"
            fi
            ;;
    esac
}

# =============================================================================
# PROJECT SETUP FUNCTIONS
# =============================================================================

# Setup the cicloza database
setup_database() {
    print_status "Setting up cicloza database..."
    
    # Check if database already exists (single password prompt)
    if psql -U postgres -l 2>/dev/null | grep -q "cicloza"; then
        print_success "Database 'cicloza' already exists"
        return 0
    fi
    
    # Create database if it doesn't exist
    print_status "Creating database 'cicloza'..."
    if psql -U postgres -c "CREATE DATABASE cicloza;" >/dev/null 2>&1; then
        print_success "Database 'cicloza' created successfully"
    else
        print_warning "Could not create database. You may need to set up PostgreSQL authentication first."
        print_status "See POSTGRES_SETUP.md for manual database setup instructions."
    fi
}

# Setup Maven wrapper files and configuration
setup_maven_wrapper() {
    print_status "Setting up Maven wrapper..."
    
    if [ ! -f "./mvnw" ] || [ ! -d ".mvn" ]; then
        # Refresh command cache and check for Maven
        hash -r
        
        if command_exists mvn; then
            print_status "Creating Maven wrapper..."
            mvn wrapper:wrapper
            if [ $? -eq 0 ] && [ -f "./mvnw" ] && [ -d ".mvn" ]; then
                chmod +x ./mvnw
                print_success "Maven wrapper created successfully"
            else
                print_error "Failed to create Maven wrapper"
                return 1
            fi
        else
            # Search for Maven in common installation directories
            local maven_paths=(
                "/c/Program Files/Maven/bin/mvn"
                "/c/ProgramData/chocolatey/lib/maven/apache-maven-*/bin/mvn"
                "$HOME/maven/bin/mvn"
                "/c/Users/$USER/scoop/apps/maven/current/bin/mvn"
            )
            
            local maven_found=false
            for maven_path in "${maven_paths[@]}"; do
                if [ -f "$maven_path" ] || ls $maven_path >/dev/null 2>&1; then
                    print_status "Found Maven at: $maven_path"
                    export PATH="$PATH:$(dirname $maven_path)"
                    hash -r
                    if command_exists mvn; then
                        print_status "Creating Maven wrapper with found Maven..."
                        mvn wrapper:wrapper
                        if [ $? -eq 0 ] && [ -f "./mvnw" ] && [ -d ".mvn" ]; then
                            chmod +x ./mvnw
                            print_success "Maven wrapper created successfully"
                            maven_found=true
                            break
                        fi
                    fi
                fi
            done
            
            if [ "$maven_found" = false ]; then
                print_error "Maven not found. Cannot create Maven wrapper."
                print_status "Please ensure Maven is installed and in your PATH"
                print_status "Or restart your terminal and try again"
                return 1
            fi
        fi
    else
        print_success "Maven wrapper already exists and is complete"
        chmod +x ./mvnw
    fi
}

# =============================================================================
# APPLICATION STARTUP FUNCTIONS
# =============================================================================

# Start the application without prerequisite checks
start_application() {
    print_header
    echo -e "${GREEN}🚀 STARTING CICLOZA APPLICATION${NC}"
    echo
    print_status "Starting Cicloza application..."
    print_status "This may take a few moments for first-time startup..."
    print_status "Application will be available at: http://localhost:8080"
    print_status "Press Ctrl+C to stop the application"
    echo
    
    # Run the application directly
    ./mvnw clean spring-boot:run
}

# Run the application with prerequisite checks
run_application() {
    print_status "Starting Cicloza application..."
    print_status "This may take a few moments for first-time startup..."
    
    # Run the application
    ./mvnw clean spring-boot:run
}

# =============================================================================
# AUTOMATED SETUP ORCHESTRATION
# =============================================================================

# Comprehensive automated setup function
auto_setup() {
    echo
    echo -e "${BLUE}=====================================${NC}"
    echo -e "${BLUE}  Cicloza Prerequisites Setup${NC}"
    echo -e "${BLUE}=====================================${NC}"
    echo
    echo -e "${GREEN}🔧 AUTOMATIC SETUP: Installing and configuring prerequisites${NC}"
    echo
    
    # Step 1: Java installation check and setup
    print_status "Step 1: Checking Java installation..."
    if command_exists java; then
        JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 21 ]; then
            print_success "Java $JAVA_VERSION already installed"
        else
            print_warning "Java $JAVA_VERSION found, but Java 21+ required"
            install_java
        fi
    else
        print_warning "Java not found, installing..."
        install_java
    fi
    
    # Step 2: PostgreSQL installation check and setup
    print_status "Step 2: Checking PostgreSQL installation..."
    if command_exists psql; then
        print_success "PostgreSQL already installed"
        start_postgresql
    else
        print_warning "PostgreSQL not found, installing..."
        install_postgresql
    fi
    
    # Step 3: Maven installation check and setup
    print_status "Step 3: Checking Maven installation..."
    if command_exists mvn; then
        print_success "Maven already installed"
    else
        print_warning "Maven not found, installing..."
        if install_maven; then
            print_success "Maven installation completed"
        else
            print_warning "Maven installation failed - you may need to install it manually"
            print_status "The setup will continue, but you'll need Maven for the wrapper step"
        fi
    fi
    
    # Step 4: Database setup
    print_status "Step 4: Setting up database..."
    setup_database
    
    # Step 5: Maven wrapper setup
    print_status "Step 5: Setting up Maven wrapper..."
    setup_maven_wrapper
    
    # Step 6: Final validation
    print_status "Step 6: Running final verification..."
    echo
    if check_java && check_postgresql && check_maven; then
        print_success "🎉 Complete setup successful!"
        echo
        print_status "Setup completed! To start the application, run:"
        echo -e "  ${YELLOW}./run.sh --start-application${NC}"
        echo
        print_status "Or use the default command:"
        echo -e "  ${YELLOW}./run.sh${NC}"
        echo
        print_status "Application will be available at:"
        echo -e "  ${YELLOW}http://localhost:8080${NC}"
        echo
        print_success "Setup process complete. Application is ready to run!"
    else
        print_error "Setup completed with some issues. Run './run.sh --check' for details."
        exit 1
    fi
}

# =============================================================================
# MAIN EXECUTION FUNCTIONS
# =============================================================================

# Main application startup with full prerequisite validation
main() {
    print_header
    
    # Validate all prerequisites
    check_java || exit 1
    check_postgresql || exit 1
    check_database || exit 1
    check_maven || exit 1
    
    print_success "All prerequisites met!"
    print_status "Application will be available at: http://localhost:8080"
    print_status "Press Ctrl+C to stop the application"
    echo
    
    # Start the application
    run_application
}

# =============================================================================
# COMMAND LINE ARGUMENT PARSING
# =============================================================================

# Parse and execute based on command line arguments
case "${1:-}" in
    -s|--setup-instruction)
        show_setup_instruction
        exit 0
        ;;
    -c|--check)
        print_header
        check_java && check_postgresql && check_database && check_maven
        if [ $? -eq 0 ]; then
            print_success "All prerequisites are met!"
        else
            print_error "Some prerequisites are missing"
            exit 1
        fi
        exit 0
        ;;
    -m|--setup)
        auto_setup
        ;;
    -a|--start-application)
        start_application
        ;;
    "")
        main
        ;;
    -h|--help)
        show_help
        exit 0
        ;;
    *)
        print_error "Unknown option: $1"
        show_help
        exit 1
        ;;
esac