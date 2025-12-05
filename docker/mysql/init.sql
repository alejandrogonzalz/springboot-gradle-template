-- Initial database setup for MySQL
-- Create database if not exists (already created by environment variables)
-- USE backend_db;

-- Grant all privileges to the backend_user
GRANT ALL PRIVILEGES ON backend_db.* TO 'backend_user'@'%';
FLUSH PRIVILEGES;
