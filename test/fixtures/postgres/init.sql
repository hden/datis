CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE IF NOT EXISTS inventory.customers (
  id         INTEGER PRIMARY KEY,
  first_name TEXT NOT NULL,
  last_name  TEXT NOT NULL,
  email      TEXT NOT NULL
);

INSERT INTO inventory.customers (id, first_name, last_name, email)
VALUES (1001, 'Sally', 'Thomas', 'sally.thomas@example.com')
ON CONFLICT (id) DO NOTHING;
