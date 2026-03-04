# Kale Interview - Mini Marketplace

A simplified creator marketplace backend built with Kotlin, Spring Boot, DGS GraphQL, and JOOQ.

## Prerequisites

- JDK 17+
- Docker (for PostgreSQL)

## Setup

**1. Start PostgreSQL:**

```bash
docker-compose up -d
```

**2. Run the application:**

```bash
./gradlew bootRun
```

The app runs Flyway migrations automatically on startup.

**3. Open GraphiQL:**

Visit [http://localhost:8080/graphiql](http://localhost:8080/graphiql) to explore the API.

## Running Tests

Tests use TestContainers (spins up a temporary PostgreSQL instance automatically -- no manual Docker setup needed):

```bash
./gradlew test
```

## Project Structure

```
src/main/kotlin/com/kale/interview/
├── KaleInterviewApplication.kt          # Spring Boot entry point
├── data/
│   ├── Models.kt                        # Brand, Creator, Campaign, Submission
│   ├── CampaignState.kt                 # DRAFT, ACTIVE, COMPLETED
│   └── SubmissionState.kt               # PENDING, APPROVED, REJECTED, PAID
├── repositories/
│   ├── BrandRepository.kt               # JOOQ queries for brands
│   ├── CreatorRepository.kt             # JOOQ queries for creators
│   ├── CampaignRepository.kt            # JOOQ queries for campaigns
│   └── SubmissionRepository.kt          # JOOQ queries for submissions
├── services/
│   ├── CampaignService.kt               # Campaign CRUD + state transitions
│   └── SubmissionService.kt             # Submit, approve, reject
└── graphql/
    ├── CampaignDataFetcher.kt           # GraphQL queries + mutations
    └── SubmissionDataFetcher.kt         # GraphQL queries + mutations
```

## Key Patterns

- **JOOQ without codegen**: Repositories use `DSLContext` with string-based table/field references
- **DGS GraphQL**: `@DgsComponent` data fetchers with `@DgsQuery` / `@DgsMutation`
- **State machines**: Enums stored as strings in the database, validated in the service layer
- **`@Transactional`**: Service methods that mutate data run inside transactions

## Example Queries

```graphql
# Create a campaign
mutation {
  createCampaign(input: {
    brandId: "1"
    title: "Summer Campaign"
    payoutCents: 5000
    maxSubmissions: 50
  }) {
    id
    title
    state
  }
}

# List all campaigns
query {
  campaigns {
    id
    title
    state
    payoutCents
  }
}

# Activate a campaign
mutation {
  activateCampaign(campaignId: "1") {
    id
    state
  }
}

# Submit content to a campaign
mutation {
  submitContent(input: {
    campaignId: "1"
    creatorId: "creator-1"
    contentUrl: "https://example.com/video.mp4"
  }) {
    id
    state
  }
}

# Approve a submission
mutation {
  approveSubmission(submissionId: "1") {
    id
    state
    reviewedAt
  }
}
```

## Database Schema

Four tables: `brands`, `creators`, `campaigns`, `submissions`. See `src/main/resources/db/migration/V1__initial.sql` for the full schema.
