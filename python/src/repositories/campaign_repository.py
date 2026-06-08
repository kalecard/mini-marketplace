import asyncpg

from src.data.enums import CampaignState
from src.data.models import Campaign


def _to_campaign(row: asyncpg.Record) -> Campaign:
    return Campaign(
        id=row["id"],
        brand_id=row["brand_id"],
        title=row["title"],
        description=row["description"],
        payout_cents=row["payout_cents"],
        max_submissions=row["max_submissions"],
        state=CampaignState(row["state"]),
        starts_at=row["starts_at"].isoformat() if row["starts_at"] else None,
        ends_at=row["ends_at"].isoformat() if row["ends_at"] else None,
        created_at=row["created_at"].isoformat(),
    )


class CampaignRepository:
    def __init__(self, db: asyncpg.Pool | asyncpg.Connection):
        self.db = db

    async def find_by_id(self, id: int) -> Campaign | None:
        row = await self.db.fetchrow("SELECT * FROM campaigns WHERE id = $1", id)
        return _to_campaign(row) if row else None

    async def find_all(self) -> list[Campaign]:
        rows = await self.db.fetch("SELECT * FROM campaigns ORDER BY created_at DESC")
        return [_to_campaign(r) for r in rows]

    async def create(
        self,
        brand_id: int,
        title: str,
        description: str | None,
        payout_cents: int,
        max_submissions: int,
    ) -> Campaign:
        row = await self.db.fetchrow(
            """
            INSERT INTO campaigns (brand_id, title, description, payout_cents, max_submissions, state)
            VALUES ($1, $2, $3, $4, $5, $6)
            RETURNING *
            """,
            brand_id, title, description, payout_cents, max_submissions, CampaignState.DRAFT.value,
        )
        return _to_campaign(row)

    async def update_state(self, id: int, state: CampaignState) -> Campaign:
        await self.db.execute(
            "UPDATE campaigns SET state = $1 WHERE id = $2", state.value, id
        )
        return await self.find_by_id(id)
