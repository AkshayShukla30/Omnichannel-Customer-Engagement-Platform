# Resume bullets

Use the variant that matches the company, keep 3 or 4 bullets, and drop any claim you cannot explain in an interview. Add numbers only from your own measurements (for example the JaCoCo percentage after you run it).

Project line: **Omnichannel Customer Engagement Platform** | Java 17, Spring Boot 3, MySQL, Docker, GitHub Actions

## Contact center company

- Built a mini contact center platform with an agent desktop (login, screen pop, notes, dispositions), skill-based routing and a supervisor dashboard, backed by Spring Boot and MySQL.
- Implemented a priority queue with interchangeable routing strategies (longest-idle, least-busy) using the Strategy pattern, selectable by configuration.
- Designed a TelephonyProvider abstraction with a call/chat simulator for demos and an optional Twilio integration (IVR menu, call bridging to the agent, signature-verified webhooks).
- Delivered handle-time, per-agent, queue-length and disposition reports through MySQL stored procedures, visualised in Chart.js.

## Bank / fintech

- Built a customer-engagement platform with a pluggable CRM integration layer (Adapter + Factory) that reads customer profiles from a core-banking database, a REST CRM or flat files, switchable through configuration only.
- Parsed and transformed XML and JSON CRM payloads, hardening the XML parser against XXE (DOCTYPE disabled), and degraded gracefully when the CRM is unavailable.
- Moved reporting logic into MySQL stored procedures called via JdbcTemplate, with Flyway-versioned schema and migrations (Oracle PL/SQL equivalent documented).
- Secured the API with BCrypt password hashing, session-based auth (HttpOnly, SameSite cookies), input validation and RFC 7807 error responses; no secrets in source.

## Java service company

- Developed a four-module Spring Boot 3 / Java 17 application using package-by-feature structure, Spring Data JPA, JdbcTemplate, Flyway and springdoc OpenAPI.
- Applied Adapter, Factory and Strategy patterns to keep integrations and routing rules swappable without code changes.
- Wrote JUnit 5 and Mockito unit tests plus Testcontainers (MySQL) integration tests for the full agent flow and the stored procedures, with a JaCoCo coverage report.
- Containerised the app with a multi-stage Dockerfile and docker-compose (app + MySQL), and set up a GitHub Actions pipeline that builds, tests and builds the Docker image.
