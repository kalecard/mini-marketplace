# Mini Marketplace — TypeScript

Node.js + graphql-yoga + pg implementation.

## Prerequisites

- Node.js 20+
- Docker (for PostgreSQL)

## Running

```bash
# Start PostgreSQL (from repo root)
docker-compose up -d

# Install dependencies
npm install

# Run in development mode (auto-restart on changes)
npm run dev
```

GraphQL playground: [http://localhost:8080/graphql](http://localhost:8080/graphql)

## Testing

Tests use TestContainers — no manual database setup needed:

```bash
npm test
```

## Scripts

| Script | Description |
|---|---|
| `npm run dev` | Start dev server with auto-reload (tsx watch) |
| `npm run build` | Compile TypeScript to `dist/` |
| `npm start` | Run compiled output |
| `npm test` | Run tests with Vitest |
| `npm run test:watch` | Run tests in watch mode |
| `npm run migrate` | Run database migrations |

## Project Structure

```
src/
├── index.ts                          # HTTP server + graphql-yoga setup
├── schema.graphqls                   # GraphQL schema (same as Kotlin)
├── data/
│   ├── enums.ts                     # CampaignState, SubmissionState
│   └── models.ts                    # Brand, Creator, Campaign, Submission
├── db/
│   ├── pool.ts                      # pg Pool + withTransaction helper
│   ├── migrate.ts                   # Migration runner
│   └── migrations/V1__initial.sql   # Database schema
├── repositories/
│   ├── brand-repository.ts          # Brand queries
│   ├── creator-repository.ts        # Creator queries
│   ├── campaign-repository.ts       # Campaign queries
│   └── submission-repository.ts     # Submission queries
├── services/
│   ├── campaign-service.ts          # Campaign CRUD + state transitions
│   └── submission-service.ts        # Submit, approve, reject
└── graphql/
    └── resolvers.ts                 # Query + Mutation resolvers
```

## Key Patterns

- **Raw SQL with `pg`** — parameterized queries (`$1`, `$2`), direct translation of JOOQ approach
- **`withTransaction`** — wraps BEGIN/COMMIT/ROLLBACK for service methods
- **Row mapping** — `snake_case` DB columns mapped to `camelCase` TypeScript interfaces
- **State machines** — enums stored as strings in the DB, validated in the service layer
