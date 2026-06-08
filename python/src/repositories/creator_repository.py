import asyncpg

from src.data.models import Creator


def _to_creator(row: asyncpg.Record) -> Creator:
    return Creator(
        id=row["id"],
        display_name=row["display_name"],
        email=row["email"],
        balance_cents=row["balance_cents"],
        created_at=row["created_at"].isoformat(),
    )


class CreatorRepository:
    def __init__(self, db: asyncpg.Pool | asyncpg.Connection):
        self.db = db

    async def find_by_id(self, id: str) -> Creator | None:
        row = await self.db.fetchrow("SELECT * FROM creators WHERE id = $1", id)
        return _to_creator(row) if row else None

    async def update_balance(self, id: str, balance_cents: int) -> None:
        await self.db.execute(
            "UPDATE creators SET balance_cents = $1 WHERE id = $2", balance_cents, id
        )
