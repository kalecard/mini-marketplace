# Mini Marketplace — Kotlin

Spring Boot + DGS GraphQL + JOOQ implementation.

## Prerequisites

- JDK 17+
- Docker (for PostgreSQL)

## Running

```bash
# Start PostgreSQL (from repo root)
docker-compose up -d

# Run the application (Flyway migrations run automatically)
./gradlew bootRun
```

GraphiQL: [http://localhost:8080/graphiql](http://localhost:8080/graphiql)

## Testing

Tests use TestContainers — no manual database setup needed:

```bash
./gradlew test
```

## Project Structure

```
src/main/kotlin/com/kale/interview/
├── KaleInterviewApplication.kt       # Spring Boot entry point
├── data/
│   ├── Models.kt                     # Brand, Creator, Campaign, Submission
│   ├── CampaignState.kt             # DRAFT, ACTIVE, COMPLETED
│   └── SubmissionState.kt           # PENDING, APPROVED, REJECTED, PAID
├── repositories/
│   ├── BrandRepository.kt           # JOOQ queries for brands
│   ├── CreatorRepository.kt         # JOOQ queries for creators
│   ├── CampaignRepository.kt        # JOOQ queries for campaigns
│   └── SubmissionRepository.kt      # JOOQ queries for submissions
├── services/
│   ├── CampaignService.kt           # Campaign CRUD + state transitions
│   └── SubmissionService.kt         # Submit, approve, reject
└── graphql/
    ├── CampaignDataFetcher.kt       # GraphQL queries + mutations
    └── SubmissionDataFetcher.kt     # GraphQL queries + mutations
```

## Key Patterns

- **JOOQ without codegen** — repositories use `DSLContext` with string-based table/field references
- **DGS GraphQL** — `@DgsComponent` data fetchers with `@DgsQuery` / `@DgsMutation`
- **State machines** — enums stored as strings in the DB, validated in the service layer
- **`@Transactional`** — service methods that mutate data run inside Spring-managed transactions
