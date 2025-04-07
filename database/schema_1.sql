BEGIN TRANSACTION;

-- Drop tables if they already exist (for reset purposes)
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS conversation_participants CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;
DROP TABLE IF EXISTS user_instruments CASCADE;
DROP TABLE IF EXISTS instruments CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS musician_profiles CASCADE;
DROP TABLE IF EXISTS venue_profiles CASCADE;
DROP TABLE IF EXISTS user_authorities CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table first since many tables reference it
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type VARCHAR(10) CHECK (user_type IN ('Musician', 'VenueOwner', 'Admin')) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create musician_profiles table
CREATE TABLE musician_profiles (
    musician_profile_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100), -- Added
    bio TEXT,
    location VARCHAR(100),
    genres TEXT,
    instruments TEXT,
    profile_picture_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create venue_profiles table
CREATE TABLE venue_profiles (
    venue_profile_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(2),
    zip_code VARCHAR(10),
    capacity INT,
    venue_type VARCHAR(50),
    genre_preferences TEXT[],
    phone VARCHAR(20),
    email VARCHAR(100),
    website_url VARCHAR(255),
    operating_hours TEXT,
    amenities TEXT[],
    profile_picture_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

-- Create conversations table to manage separate conversation threads
CREATE TABLE conversations (
    conversation_id SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create conversation_participants table to track participants in each conversation
CREATE TABLE conversation_participants (
    conversation_id INT REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    PRIMARY KEY (conversation_id, user_id)
);

-- Create messages table with threaded conversations
CREATE TABLE messages (
    message_id SERIAL PRIMARY KEY,
    conversation_id INT REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    sender_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    receiver_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    parent_message_id INT REFERENCES messages(message_id) ON DELETE CASCADE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user_authorities table
CREATE TABLE user_authorities (
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    authority_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, authority_name),
    CONSTRAINT check_valid_roles CHECK (authority_name IN ('ROLE_ADMIN', 'ROLE_USER'))
);

-- Commit the transaction
COMMIT;