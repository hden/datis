# Datis - Change Data Capture Service [![CircleCI](https://dl.circleci.com/status-badge/img/gh/hden/datis/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/hden/datis/tree/main)

Datis is a service that captures database changes from PostgreSQL and streams them to Google Cloud Pub/Sub using Debezium. This enables real-time data synchronization and event-driven architectures.

## What is Change Data Capture (CDC)?

Change Data Capture (CDC) is a software design pattern that tracks and captures changes made to a database. When data is inserted, updated, or deleted in your database, CDC systems automatically detect these changes and make them available for other systems to consume.

## What is Debezium?

Debezium is an open-source distributed platform for change data capture. It:
- Connects to your database and captures all row-level changes
- Sends these changes as events to message brokers
- Provides reliable and scalable CDC capabilities
- Supports various databases including PostgreSQL

## Architecture

```
PostgreSQL → Debezium → Google Cloud Pub/Sub → Downstream Services
```

1. PostgreSQL: Your source database where changes occur
2. Debezium: Captures changes and converts them to events
3. Google Cloud Pub/Sub: Distributes events to interested services
4. Downstream Services: Consume and process the change events

### Enhanced Message Filtering

One of the key features of Datis is its enhanced message filtering capability using PubSub's message attributes. Each message includes metadata such as:
- Operation type (insert, update, delete)
- Database name
- Schema name
- Table name
- Record ID (before/after)

This metadata enables powerful filtering at the PubSub subscription level, allowing you to:
- Subscribe only to specific tables
- Filter by operation type (e.g., only inserts)
- Target specific records by ID
- Combine multiple conditions using PubSub's filter expressions

Example PubSub filter expressions:
```
attributes.table = "users" AND attributes.op = "c"  # Only new users
attributes.schema = "public" AND attributes.op = "u"  # Only updates in public schema
attributes.id = "123"  # Changes to specific record
```

This feature provides more flexibility than standard Debezium Server implementations, as it allows downstream services to selectively process only the events they need, reducing unnecessary processing and network traffic.

## Prerequisites

- PostgreSQL 10.0 or later
- Google Cloud Platform account with Pub/Sub enabled
- Java 11 or later
- Leiningen (Clojure build tool)

## Configuration

1. PostgreSQL Configuration:
   - Enable logical replication
   - Create a replication user
   - Configure appropriate permissions

2. Debezium Configuration:
   - Set up connector configuration
   - Configure database connection details
   - Define which tables to monitor

3. Google Cloud Pub/Sub Configuration:
   - Create a topic for change events
   - Set up appropriate IAM permissions

## Development

### Setup

When you first clone this repository, run:

```sh
lein duct setup
```

### Environment

Start a REPL:

```sh
lein repl
```

Load the development environment:

```clojure
user=> (dev)
:loaded
```

Start the system:

```clojure
dev=> (go)
:duct.server.http.jetty/starting-server {:port 3000}
:initiated
```

The service will be available at http://localhost:3000.

To reload changes:

```clojure
dev=> (reset)
:reloading (...)
:resumed
```

### Testing

Run tests through the REPL:

```clojure
dev=> (test)
...
```

Or using Leiningen:

```sh
lein test
```

## Monitoring

The service provides the following monitoring capabilities:
- Health check endpoints
- Metrics for change events
- Error tracking and logging
- Performance monitoring

## Troubleshooting

Common issues and solutions:
1. Connection issues with PostgreSQL
   - Verify database credentials
   - Check network connectivity
   - Ensure replication is properly configured

2. Pub/Sub delivery failures
   - Check IAM permissions
   - Verify topic configuration
   - Monitor quota limits

3. Performance issues
   - Monitor system resources
   - Check database load
   - Review Debezium configuration

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

Copyright © 2025 Haokang Den
