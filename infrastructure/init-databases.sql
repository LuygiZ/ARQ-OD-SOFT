-- Create databases for each microservice (Database-per-Service pattern)
CREATE DATABASE genre_db;
CREATE DATABASE author_db;
CREATE DATABASE book_db;
CREATE DATABASE reader_db;
CREATE DATABASE lending_db;
CREATE DATABASE user_db;

-- Create dedicated user
CREATE USER lms_user WITH PASSWORD 'lms_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE genre_db TO lms_user;
GRANT ALL PRIVILEGES ON DATABASE author_db TO lms_user;
GRANT ALL PRIVILEGES ON DATABASE book_db TO lms_user;
GRANT ALL PRIVILEGES ON DATABASE reader_db TO lms_user;
GRANT ALL PRIVILEGES ON DATABASE lending_db TO lms_user;
GRANT ALL PRIVILEGES ON DATABASE user_db TO lms_user;