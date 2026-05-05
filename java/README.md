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
