# Mini Marketplace — Java

Spring Boot + DGS GraphQL + JOOQ implementation.

## Prerequisites

- JDK 17+
- Docker (for PostgreSQL)

## Running

```bash
# Start PostgreSQL (from repo root)
docker-compose up -d

# Run the application (Flyway migrations run automatically)
./mvnw spring-boot:run
```

GraphiQL: [http://localhost:8080/graphiql](http://localhost:8080/graphiql)

## Trying it out

The schema has no public way to create brands or creators (deliberate — they're upstream concerns). Seed one of each before running the campaign/submission flow:

```bash
docker exec -i $(docker-compose ps -q postgres) psql -U postgres -d interview -c \
  "INSERT INTO brands (name) VALUES ('Acme'); \
   INSERT INTO creators (id, display_name, email) VALUES ('creator-1', 'Test', 'c@e.com');"
```

Then in GraphiQL run these in order:

```graphql
mutation { createCampaign(input: { brandId: "1", title: "Summer", payoutCents: 5000, maxSubmissions: 50 }) { id title state } }
mutation { activateCampaign(campaignId: "1") { id state } }
mutation { submitContent(input: { campaignId: "1", creatorId: "creator-1", contentUrl: "https://example.com/v.mp4" }) { id state } }
mutation { approveSubmission(submissionId: "1") { id state reviewedAt } }
query   { campaigns { id title state } }
query   { submissions(campaignId: "1") { id state reviewedAt } }
```

Expected progression: `DRAFT` → `ACTIVE` → `PENDING` → `APPROVED` (with `reviewedAt` populated).

## Troubleshooting

**Port 5433 already in use / `password authentication failed for user "postgres"`** — something else on your machine is bound to `127.0.0.1:5433` (commonly `cloud-sql-proxy`). Diagnose with `lsof -nP -iTCP:5433 -sTCP:LISTEN`. Either stop the conflicting process or override the JDBC URL to use the IPv6 loopback (which Docker Desktop binds on):

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://[::1]:5433/interview"
```

## Testing

Tests use Testcontainers — no manual database setup needed:

```bash
./mvnw test
```

## Project Structure

```
src/main/java/com/kale/interview/
├── KaleInterviewApplication.java     # Spring Boot entry point
├── data/
│   ├── Brand.java, Creator.java,
│   ├── Campaign.java, Submission.java   # Records (immutable)
│   ├── CampaignState.java               # DRAFT, ACTIVE, COMPLETED
│   └── SubmissionState.java             # PENDING, APPROVED, REJECTED, PAID
├── repositories/
│   ├── BrandRepository.java             # JOOQ queries for brands
│   ├── CreatorRepository.java           # JOOQ queries for creators
│   ├── CampaignRepository.java          # JOOQ queries for campaigns
│   └── SubmissionRepository.java        # JOOQ queries for submissions
├── services/
│   ├── CampaignService.java             # Campaign CRUD + state transitions
│   └── SubmissionService.java           # Submit, approve, reject
└── graphql/
    ├── CampaignDataFetcher.java         # GraphQL queries + mutations
    └── SubmissionDataFetcher.java       # GraphQL queries + mutations
```

## Key Patterns

- **Java records** — immutable models, replace Kotlin `data class`.
- **JOOQ without codegen** — repositories use `DSLContext` with `DSL.table("name")` / `DSL.field("name", Type.class)` string identifiers.
- **DGS GraphQL** — `@DgsComponent` data fetchers with `@DgsQuery` / `@DgsMutation`.
- **State machines** — enums stored as strings in the DB, validated in the service layer.
- **`@Transactional`** — service methods that mutate data run inside Spring-managed transactions.
