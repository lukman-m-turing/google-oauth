CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    weak_token VARCHAR(255) NOT NULL,
    oauth_access_token TEXT,
    oauth_refresh_token TEXT,
    profile_picture_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
