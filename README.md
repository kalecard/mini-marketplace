# Mini Marketplace

A simplified creator marketplace backend where brands create campaigns and creators submit content. Includes approval and payment workflows with state machine transitions.

Three functionally equivalent implementations share the same PostgreSQL database and GraphQL API contract:

| | Kotlin | TypeScript | Java | Python |
|---|---|---|---|---|
| Framework | Spring Boot + DGS GraphQL | graphql-yoga | Spring Boot + DGS GraphQL | Strawberry + FastAPI |
| DB access | JOOQ | pg (raw SQL) | JOOQ | asyncpg (raw SQL) |
| Testing | TestNG + TestContainers | Vitest + TestContainers | JUnit 5 + Testcontainers | pytest + Testcontainers |

## Prerequisites

- Docker (for PostgreSQL)
- **Kotlin**: JDK 17+
- **TypeScript**: Node.js 20+
- **Java**: JDK 17+
- **Python**: Python 3.11+

## Quick Start

```bash
# Start PostgreSQL (shared by all implementations)
docker-compose up -d

# Run Kotlin version
cd kotlin && ./gradlew bootRun

# — OR —

# Run TypeScript version
cd typescript && npm install && npm run dev

# — OR —

# Run Java version
cd java && ./mvnw spring-boot:run

# — OR —

# Run Python version
cd python && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt && .venv/bin/python -m src.main
```

All serve the GraphQL API at `http://localhost:8080/graphql`.

See each subfolder's README for implementation-specific details:

- [kotlin/README.md](kotlin/README.md)
- [typescript/README.md](typescript/README.md)
- [java/README.md](java/README.md)
- [python/README.md](python/README.md)

## Database

PostgreSQL runs on `localhost:5433` (mapped from container port 5432).

Four tables: `brands`, `creators`, `campaigns`, `submissions`. All implementations use the same `V1__initial.sql` migration.

## GraphQL API

```graphql
# Create a campaign
mutation {
  createCampaign(input: {
    brandId: "1"
    title: "Summer Campaign"
    payoutCents: 5000
    maxSubmissions: 50
  }) { id title state }
}

# List campaigns
query { campaigns { id title state payoutCents } }

# Activate a campaign
mutation { activateCampaign(campaignId: "1") { id state } }

# Submit content
mutation {
  submitContent(input: {
    campaignId: "1"
    creatorId: "creator-1"
    contentUrl: "https://example.com/video.mp4"
  }) { id state }
}

# Approve / reject a submission
mutation { approveSubmission(submissionId: "1") { id state reviewedAt } }
mutation { rejectSubmission(submissionId: "1") { id state reviewedAt } }
```
