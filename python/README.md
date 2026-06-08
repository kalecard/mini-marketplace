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
