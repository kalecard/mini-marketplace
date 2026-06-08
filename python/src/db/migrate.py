from pathlib import Path

import asyncpg

MIGRATION_PATH = Path(__file__).parent / "migrations" / "V1__initial.sql"


def read_migration_sql() -> str:
    return MIGRATION_PATH.read_text()


async def run_migration(db: asyncpg.Pool | asyncpg.Connection) -> None:
    await db.execute(read_migration_sql())


async def _main() -> None:
    from src.db.pool import create_pool

    pool = await create_pool()
    await run_migration(pool)
    await pool.close()


if __name__ == "__main__":
    import asyncio

    asyncio.run(_main())
