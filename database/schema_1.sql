BEGIN TRANSACTION;

-- Drop tables if they already exist (for reset purposes)
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS user_instruments CASCADE;
DROP TABLE IF EXISTS instruments CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP TABLE IF EXISTS venues CASCADE;
DROP TABLE IF EXISTS user_authorities CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table first since many tables reference it
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type VARCHAR(10) CHECK (user_type IN ('Musician', 'VenueOwner')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create profiles table
CREATE TABLE profiles (
    profile_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    bio TEXT,
    location VARCHAR(100),
    genres TEXT[],  -- Array to store multiple genres
    instruments TEXT[],  -- Array for musician instruments
    venue_name VARCHAR(100),  -- For VenueOwners
    capacity INT,  -- For VenueOwners
    profile_picture_url VARCHAR(255)
);

-- Create projects table
CREATE TABLE projects (
    project_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    genre VARCHAR(50),
    creator_id INT REFERENCES users(user_id),
    start_date DATE,
    end_date DATE
);

-- Create instruments table
CREATE TABLE instruments (
    instrument_id SERIAL PRIMARY KEY,
    instrument_name VARCHAR(50) NOT NULL UNIQUE
);

-- Create user_instruments table (Join table for Users and Instruments)
CREATE TABLE user_instruments (
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    instrument_id INT REFERENCES instruments(instrument_id),
    PRIMARY KEY (user_id, instrument_id)
);

-- Create venues table
CREATE TABLE venues (
    venue_id SERIAL PRIMARY KEY,
    owner_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    capacity INT,
    genre_preferences TEXT[],  -- Array for preferred genres
    contact_info VARCHAR(100)
);

-- Create messages table
CREATE TABLE messages (
    message_id SERIAL PRIMARY KEY,
    sender_id INT REFERENCES users(user_id),
    receiver_id INT REFERENCES users(user_id),
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user_authorities table
CREATE TABLE user_authorities (
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    authority_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, authority_name),
    CONSTRAINT check_valid_roles CHECK (authority_name IN ('ROLE_ADMIN', 'ROLE_USER'))
);

-- Commit the transaction
COMMIT;