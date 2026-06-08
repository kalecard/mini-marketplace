import asyncio

import asyncpg
import pytest
import pytest_asyncio
from testcontainers.postgres import PostgresContainer

from src.db.migrate import run_migration


@pytest.fixture(scope="session")
def postgres_dsn():
    with PostgresContainer("postgres:16") as container:
        dsn = (
            f"postgresql://{container.username}:{container.password}"
            f"@{container.get_container_host_ip()}:{container.get_exposed_port(5432)}"
            f"/{container.dbname}"
        )
        asyncio.run(_migrate(dsn))
        yield dsn


async def _migrate(dsn: str) -> None:
    conn = await asyncpg.connect(dsn)
    try:
        await run_migration(conn)
    finally:
        await conn.close()


@pytest_asyncio.fixture
async def pool(postgres_dsn):
    pool = await asyncpg.create_pool(dsn=postgres_dsn)
    yield pool
    await pool.close()


@pytest_asyncio.fixture
async def brand_id(pool):
    """Clean all tables (FK order) and seed one brand + one creator. Returns the brand id."""
    async with pool.acquire() as conn:
        await conn.execute("DELETE FROM submissions")
        await conn.execute("DELETE FROM campaigns")
        await conn.execute("DELETE FROM creators")
        await conn.execute("DELETE FROM brands")
        bid = await conn.fetchval(
            "INSERT INTO brands (name, balance_cents) VALUES ($1, $2) RETURNING id",
            "Test Brand", 100_000,
        )
        await conn.execute(
            "INSERT INTO creators (id, display_name, email) VALUES ($1, $2, $3)",
            "creator-1", "Test Creator", "creator@test.com",
        )
    return bid
