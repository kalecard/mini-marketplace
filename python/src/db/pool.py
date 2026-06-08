import os
from typing import Awaitable, Callable, TypeVar

import asyncpg

T = TypeVar("T")

DEFAULT_DSN = "postgresql://postgres:postgres@localhost:5433/interview"


async def create_pool(dsn: str | None = None) -> asyncpg.Pool:
    return await asyncpg.create_pool(dsn=dsn or os.environ.get("DATABASE_URL", DEFAULT_DSN))


async def with_transaction(
    pool: asyncpg.Pool,
    fn: Callable[[asyncpg.Connection], Awaitable[T]],
) -> T:
    async with pool.acquire() as conn:
        async with conn.transaction():
            return await fn(conn)
