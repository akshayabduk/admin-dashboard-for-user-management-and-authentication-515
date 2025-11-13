# FIDO2 Backend

Spring Boot service for FIDO2 flows. 

Startup notes:
- Binds to port 3001 by default. Override with env: SERVER_PORT.
- Database via environment variables (required for DB connectivity):
  - SPRING_DATASOURCE_URL (e.g., jdbc:postgresql://host:5432/dbname)
  - SPRING_DATASOURCE_USERNAME
  - SPRING_DATASOURCE_PASSWORD
  - Optional: SPRING_DATASOURCE_DRIVER_CLASS_NAME (defaults to org.postgresql.Driver)
- Flyway migrations:
  - Enabled by default. To disable (e.g., in preview/no-DB environments) set FLYWAY_ENABLED=false.
  - Ensure the database is reachable and credentials are valid; Flyway runs at startup and applies migrations from classpath:db/migration.
- Health endpoints:
  - GET /health -> "OK"
  - GET /health/db -> connectivity check (returns ok=true if DataSource can obtain a connection)
- Swagger UI:
  - GET /docs -> redirects to /swagger-ui.html

Run locally (example):
```
SERVER_PORT=3001 \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
./gradlew bootRun --args='--server.port=3001'
```

Disable Flyway if DB is not available:
```
FLYWAY_ENABLED=false ./gradlew bootRun
```
