@echo off
REM =============================================================================
REM Cicloza Application Startup Script for Windows
REM =============================================================================

setlocal EnableDelayedExpansion

REM Color codes for Windows (using echo with special characters)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

goto :main

:print_status
    echo %BLUE%[INFO]%NC% %~1
    goto :eof

:print_success
    echo %GREEN%[SUCCESS]%NC% %~1
    goto :eof

:print_warning
    echo %YELLOW%[WARNING]%NC% %~1
    goto :eof

:print_error
    echo %RED%[ERROR]%NC% %~1
    goto :eof

:print_header
    echo.
    echo %BLUE%=====================================%NC%
    echo %BLUE%  Cicloza Application Startup%NC%
    echo %BLUE%=====================================%NC%
    echo.
    goto :eof

:command_exists
    where %1 >nul 2>&1
    if %errorlevel% == 0 (
        exit /b 0
    ) else (
        exit /b 1
    )

:check_java
    call :print_status "Checking Java installation..."
    
    call :command_exists java
    if !errorlevel! neq 0 (
        call :print_error "Java is not installed!"
        call :print_status "Please install Java 21 or later:"
        call :print_status "  Download from: https://adoptium.net/"
        call :print_status "  Or use: winget install Microsoft.OpenJDK.21"
        exit /b 1
    )
    
    REM Get Java version
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr version') do (
        set JAVA_VERSION_STR=%%i
    )
    set JAVA_VERSION_STR=!JAVA_VERSION_STR:"=!
    for /f "tokens=1 delims=." %%i in ("!JAVA_VERSION_STR!") do set JAVA_VERSION=%%i
    
    if !JAVA_VERSION! lss 21 (
        call :print_error "Java 21 or later is required. Found Java !JAVA_VERSION!"
        exit /b 1
    )
    
    call :print_success "Java !JAVA_VERSION! found"
    exit /b 0

:check_postgresql
    call :print_status "Checking PostgreSQL..."
    
    call :command_exists psql
    if !errorlevel! neq 0 (
        call :print_error "PostgreSQL is not installed or not in PATH!"
        call :print_status "Please install PostgreSQL:"
        call :print_status "  Method 1: winget install PostgreSQL.PostgreSQL.17"
        call :print_status "  Method 2: Download from https://www.postgresql.org/download/windows/"
        call :print_status "See POSTGRES_SETUP.md for detailed instructions"
        exit /b 1
    )
    
    REM Check if PostgreSQL service is running
    sc query postgresql-x64-17 | findstr "RUNNING" >nul 2>&1
    if !errorlevel! neq 0 (
        sc query postgresql-x64-16 | findstr "RUNNING" >nul 2>&1
        if !errorlevel! neq 0 (
            call :print_warning "PostgreSQL service is not running!"
            call :print_status "Attempting to start PostgreSQL service..."
            
            net start postgresql-x64-17 >nul 2>&1
            if !errorlevel! neq 0 (
                net start postgresql-x64-16 >nul 2>&1
                if !errorlevel! neq 0 (
                    call :print_error "Failed to start PostgreSQL service"
                    call :print_status "Please start PostgreSQL service manually:"
                    call :print_status "  Run as Administrator: net start postgresql-x64-17"
                    call :print_status "  Or see POSTGRES_SETUP.md for instructions"
                    exit /b 1
                )
            )
            call :print_success "PostgreSQL service started"
        )
    )
    
    call :print_success "PostgreSQL is running"
    exit /b 0

:check_database
    call :print_status "Checking database connection..."
    
    REM Test database connection
    psql -U postgres -d cicloza -c "SELECT 1;" >nul 2>&1
    if !errorlevel! == 0 (
        call :print_success "Database 'cicloza' exists and is accessible"
        exit /b 0
    )
    
    call :print_warning "Database 'cicloza' not found or not accessible"
    call :print_status "Attempting to create database..."
    
    REM Try to create database
    psql -U postgres -c "CREATE DATABASE cicloza;" >nul 2>&1
    if !errorlevel! == 0 (
        call :print_success "Database 'cicloza' created successfully"
        exit /b 0
    ) else (
        call :print_error "Failed to create database"
        call :print_status "Please ensure PostgreSQL is running and you have proper permissions"
        call :print_status "Run: psql -U postgres -c \"CREATE DATABASE cicloza;\""
        call :print_status "Or see POSTGRES_SETUP.md for detailed setup instructions"
        exit /b 1
    )

:check_maven
    call :print_status "Checking Maven wrapper..."
    
    if not exist "mvnw.cmd" (
        call :print_error "Maven wrapper (mvnw.cmd) not found!"
        call :print_status "Please ensure you're running this script from the project root directory"
        exit /b 1
    )
    
    call :print_success "Maven wrapper is ready"
    exit /b 0

:run_application
    call :print_status "Starting Cicloza application..."
    call :print_status "This may take a few moments for first-time startup..."
    
    REM Run the application
    mvnw.cmd clean spring-boot:run
    goto :eof

:check_prerequisites
    call :check_java
    if !errorlevel! neq 0 exit /b 1
    
    call :check_postgresql
    if !errorlevel! neq 0 exit /b 1
    
    call :check_database
    if !errorlevel! neq 0 exit /b 1
    
    call :check_maven
    if !errorlevel! neq 0 exit /b 1
    
    exit /b 0

:show_help
    echo Cicloza Application Startup Script
    echo.
    echo Usage: %~nx0 [OPTION]
    echo.
    echo Options:
    echo   -h, --help     Show this help message
    echo   -c, --check    Only check prerequisites without starting the app
    echo.
    echo Prerequisites:
    echo   - Java 21 or later
    echo   - PostgreSQL database server
    echo   - 'cicloza' database created
    echo.
    echo For detailed setup instructions, see:
    echo   - POSTGRES_SETUP.md
    echo   - README.md
    goto :eof

:main
    if "%~1"=="-h" goto :help
    if "%~1"=="--help" goto :help
    if "%~1"=="-c" goto :check_only
    if "%~1"=="--check" goto :check_only
    if "%~1"=="" goto :run_main
    
    call :print_error "Unknown option: %~1"
    goto :show_help
    
:help
    call :show_help
    exit /b 0

:check_only
    call :print_header
    call :check_prerequisites
    if !errorlevel! == 0 (
        call :print_success "All prerequisites are met!"
    ) else (
        call :print_error "Some prerequisites are missing"
        exit /b 1
    )
    exit /b 0

:run_main
    call :print_header
    
    REM Check all prerequisites
    call :check_prerequisites
    if !errorlevel! neq 0 exit /b 1
    
    call :print_success "All prerequisites met!"
    call :print_status "Application will be available at: http://localhost:8080"
    call :print_status "Press Ctrl+C to stop the application"
    echo.
    
    REM Run the application
    call :run_application
    goto :eof