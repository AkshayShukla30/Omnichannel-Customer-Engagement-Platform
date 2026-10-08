# 3-minute demo script

Goal: show the four modules working together in one flow. Speak slowly, it is easier to cut than to re-record.

## Before you hit record (2 minutes)

1. `docker compose up --build`, wait until the app logs `Started OmnichannelApplication`.
2. Open three browser windows or tabs side by side:
   - Tab A: http://localhost:8080 (agent desktop)
   - Tab B: http://localhost:8080/dashboard.html (log in first in Tab A, the dashboard needs the session)
   - Tab C: http://localhost:8080/swagger-ui.html
3. Log out in Tab A so you start from the login screen. Zoom the browser to 110 percent.
4. Have a terminal ready with the project folder open (for the CRM swap at the end).

## Timeline

| Time | On screen | Say |
|---|---|---|
| 0:00 - 0:15 | Architecture diagram from the README | "This is a mini contact center platform in Java 17 and Spring Boot. Four modules: agent desktop, CRM integration layer, skill-based routing and a reporting dashboard, on MySQL." |
| 0:15 - 0:35 | Tab A, log in as `priya` | "An agent logs in and becomes available. Priya handles loan and general queries." |
| 0:35 - 1:05 | Demo tools, generate 3 interactions | "There is no real telephony, so a simulator creates calls and chats. The routing engine matches the skill, and among the matching agents picks the one idle the longest. Strategy pattern, switchable by config." Point at the interaction that popped up. |
| 1:05 - 1:35 | Screen pop | "The screen pop comes from the CRM integration layer. This one says the data came from the core-banking adapter, a SQL lookup. The platform only knows an interface." |
| 1:35 - 2:00 | Add a note, choose a disposition, close | "The agent adds a note, sets a disposition and closes. She becomes available again and the next waiting interaction is assigned right away." |
| 2:00 - 2:30 | Tab B, dashboard | "Every chart comes from a MySQL stored procedure: average handle time, interactions per agent, queue length and dispositions. Closing that interaction just changed these numbers." |
| 2:30 - 2:50 | Terminal: `CRM_ADAPTER=rest-crm CRM_REST_FORMAT=xml docker compose up -d app`, then log in again and get a new interaction | "To swap the CRM I change one config value. Now the same screen pop is fed by a REST CRM that answers in legacy XML. The adapter parses and transforms it. No code change." Point at the "rest-crm adapter" label. |
| 2:50 - 3:00 | Tab C, Swagger | "Everything is documented in Swagger, there is CI with tests and a Docker build. Code is on GitHub, link in the description." |

## Tips

- If the queue empties too fast, generate 10 instead of 3.
- If a screen pop shows a warning, the CRM adapter could not reach its source. Check `GET /api/integration/adapter`.
- To make the strategy visible, log in as two agents (second browser profile) and show that new interactions alternate by idle time. Run with `ROUTING_STRATEGY=least-busy` if you want to talk about the other strategy.
- Do not demo Twilio unless you have actually tested it with your own account.
