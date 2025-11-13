# FIDO2 Backend

Spring Boot service for FIDO2 flows. 

Startup notes:
- Binds to port 3001 by default. Override with env: SERVER_PORT.
- Database via environment variables:
  - SPRING_DATASOURCE_URL (e.g., jdbc:postgresql://host:5432/db)
  - SPRING_DATASOURCE_USERNAME
  - SPRING_DATASOURCE_PASSWORD
  - Optional: SPRING_DATASOURCE_DRIVER_CLASS_NAME (defaults to org.postgresql.Driver)
- Flyway can be disabled in preview environments by setting FLYWAY_ENABLED=false or by not providing SPRING_DATASOURCE_URL (ConditionalFlywayConfig turns Flyway off if no DS URL).
- Health endpoints:
  - GET /health -> "OK"
  - GET /health/db -> connectivity check (will report ok=false if DB not reachable)
- Swagger UI:
  - GET /docs -> redirects to /swagger-ui.html
