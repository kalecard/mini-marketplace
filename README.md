# Mini Marketplace

A simplified creator marketplace backend where brands create campaigns and creators submit content. Includes approval and payment workflows with state machine transitions.

Two functionally equivalent implementations share the same PostgreSQL database and GraphQL API contract:

| | Kotlin | TypeScript |
|---|---|---|
| Framework | Spring Boot + DGS GraphQL | graphql-yoga |
| DB access | JOOQ | pg (raw SQL) |
| Testing | TestNG + TestContainers | Vitest + TestContainers |

## Prerequisites

- Docker (for PostgreSQL)
- **Kotlin**: JDK 17+
- **TypeScript**: Node.js 20+

## Quick Start

```bash
# Start PostgreSQL (shared by both implementations)
docker-compose up -d

# Run Kotlin version
cd kotlin && ./gradlew bootRun

# — OR —

# Run TypeScript version
cd typescript && npm install && npm run dev
```

Both serve the GraphQL API at `http://localhost:8080/graphql`.

See each subfolder's README for implementation-specific details:

- [kotlin/README.md](kotlin/README.md)
- [typescript/README.md](typescript/README.md)

## Database

PostgreSQL runs on `localhost:5433` (mapped from container port 5432).

Four tables: `brands`, `creators`, `campaigns`, `submissions`. Both implementations use the same `V1__initial.sql` migration.

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
