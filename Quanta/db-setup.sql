-- Create database
CREATE DATABASE cicloza;

-- Connect to the database
\c cicloza

-- Create user table (will be automatically created by Hibernate, but shown here for reference)

CREATE TABLE user (
    user_id UUID PRIMARY KEY NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    phone VARCHAR(50) NOT NULL UNIQUE,
    email_id VARCHAR(255) UNIQUE,
    role VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE user_serialPort (
    id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES user(userid) ON DELETE CASCADE,
    serialport TEXT[] -- Array of strings to store serial port identifiers
);
