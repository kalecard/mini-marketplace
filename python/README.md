# Mini Marketplace — Python

The Python sibling of the mini-marketplace backend. Functionally equivalent to the
Kotlin, TypeScript, and Java versions: same Postgres database, same GraphQL contract,
same port 8080.

## Stack

- **GraphQL:** Strawberry (code-first, async resolvers)
- **HTTP:** FastAPI + uvicorn (GraphiQL at `/graphql`)
- **DB access:** asyncpg (raw parameterized SQL)
- **Tests:** pytest + pytest-asyncio + Testcontainers

## Prerequisites

- Python 3.11+
- Docker (for PostgreSQL, and for the Testcontainers-based tests)

## Setup

```bash
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

## Running

Start the shared PostgreSQL (from the repo root):

```bash
docker-compose up -d
```

Then run the server (migration runs automatically on startup):

```bash
.venv/bin/python -m src.main
```

GraphQL is served at `http://localhost:8080/graphql` (GraphiQL UI in the browser).

To run the migration on its own:

```bash
.venv/bin/python -m src.db.migrate
```

## Trying it out

The schema has no way to create brands or creators (deliberate — they're upstream
concerns). Seed one of each before running the campaign/submission flow:

```bash
docker-compose exec -T postgres psql -U postgres -d interview -c \
  "INSERT INTO brands (name) VALUES ('Acme'); \
   INSERT INTO creators (id, display_name, email) VALUES ('creator-1', 'Test', 'c@e.com');"
```

Then open GraphiQL at `http://localhost:8080/graphql` and run these in order:

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

**`asyncpg.exceptions.DuplicateTableError: relation "brands" already exists` on
startup** — the migration (`V1__initial.sql`) uses plain `CREATE TABLE` and is **not**
idempotent (matching the TypeScript sibling; the Java/Kotlin siblings use Flyway, which
tracks applied migrations). It fails if the tables already exist — e.g. from a previous
run, or because another sibling created them in the shared database. Reset the schema
before starting:

```bash
docker-compose exec -T postgres psql -U postgres -d interview -c \
  "DROP TABLE IF EXISTS submissions, campaigns, creators, brands CASCADE;"
```

**`[Errno 48] address already in use` on port 8080** — only one implementation can
serve port 8080 at a time. Find the process holding it with
`lsof -nP -iTCP:8080 -sTCP:LISTEN`, then `kill <pid>` (commonly a stale server from a
previous run).

## Testing

Tests spin up a throwaway PostgreSQL via Testcontainers (Docker required):

```bash
.venv/bin/pytest
```

## Project structure

```
src/
├── main.py            FastAPI app + uvicorn entrypoint
├── data/              dataclass models + enums
├── db/                asyncpg pool, transaction helper, migration runner
├── repositories/      one per table, raw SQL
├── services/          transactions + state-machine validation
└── graphql/           Strawberry types + Query/Mutation resolvers
tests/                 Testcontainers-backed integration tests
schema.graphqls        shared GraphQL contract (reference + drift test)
```

## Key patterns

- **Code-first GraphQL:** the schema is generated from Python types in `src/graphql/`.
  `tests/test_schema_contract.py` asserts the generated SDL matches the shared
  `schema.graphqls`, so the API cannot silently drift from the other siblings.
- **GraphQL `ID` handling:** received as strings, parsed to `int` at the resolver
  boundary before reaching services.
- **State-machine validation** lives in the service layer and raises `ValueError`,
  which Strawberry surfaces as GraphQL errors.
