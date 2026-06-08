import asyncpg

from src.data.models import Brand


def _to_brand(row: asyncpg.Record) -> Brand:
    return Brand(
        id=row["id"],
        name=row["name"],
        balance_cents=row["balance_cents"],
        created_at=row["created_at"].isoformat(),
    )


class BrandRepository:
    def __init__(self, db: asyncpg.Pool | asyncpg.Connection):
        self.db = db

    async def find_by_id(self, id: int) -> Brand | None:
        row = await self.db.fetchrow("SELECT * FROM brands WHERE id = $1", id)
        return _to_brand(row) if row else None

    async def update_balance(self, id: int, balance_cents: int) -> None:
        await self.db.execute(
            "UPDATE brands SET balance_cents = $1 WHERE id = $2", balance_cents, id
        )
