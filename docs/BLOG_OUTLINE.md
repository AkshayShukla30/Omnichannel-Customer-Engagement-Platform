# Blog outline: "Building a CRM adapter layer in Java"

Audience: Java developers who have been asked to integrate "the customer system" and know it will change. Target length: 1,200 to 1,500 words, one diagram, three code snippets.

## 1. The problem (150 words)
- Every contact center and bank has a different customer system: a core banking database, a SaaS CRM, a legacy XML service, sometimes just a spreadsheet.
- Hard-coding the first one makes the second a rewrite. Show the "before" snippet where the desk code calls a vendor client directly.

## 2. The shape of the solution (200 words)
- One interface: `CustomerDataAdapter` with `findById` and `findByPhone`, and one internal model, `CustomerProfile`.
- Diagram: desk -> factory -> adapter -> source. Reuse `docs/architecture.svg`.
- Rule of thumb: the interface speaks the platform's language, adapters translate.

## 3. Three adapters, three kinds of pain (400 words)
- **Database**: plain JdbcTemplate and a row mapper. Phone matching on the last 10 digits.
- **REST + JSON**: RestClient, timeouts, 404 means "not found" and not an error. Flattening nested JSON.
- **REST + XML**: why XML is still around. DOM + XPath, mapping attributes and elements, and the XXE section: disable DOCTYPE, test it with a hostile payload.
- **Files**: lazy loading, CSV quoting edge cases, fail on first use rather than at startup.

## 4. Picking the adapter by configuration (150 words)
- Factory collects all adapter beans, indexes them by `type()`, picks one from `crm.adapter`.
- Fail fast with the list of valid names. Show the error message.
- Why not `@ConditionalOnProperty` per adapter: trade-offs (you lose the "list available" feature, you gain not constructing unused beans).

## 5. When the CRM is down (150 words)
- The agent desktop must not break. Catch at the boundary, return a warning in the view model, log the cause.
- Timeouts matter more than retries here.

## 6. Testing adapters (200 words)
- `MockRestServiceServer` for HTTP adapters.
- A round-trip test: JSON -> legacy XML -> profile equals JSON -> profile. Catches mapping drift.
- Temp files for the file adapter.

## 7. What I would add next (100 words)
- Caching with short TTL, circuit breaker, adapter health in the actuator, write-back (notes to the CRM).

## 8. Wrap-up (50 words)
- Link to the repo, the swap-by-environment-variable demo command, call to action.

## Assets to prepare
- Architecture diagram (from `docs/`), screenshot of the screen pop showing the adapter label, a terminal capture of the one-line swap.
