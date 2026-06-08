import asyncpg

from src.data.enums import SubmissionState
from src.data.models import Submission


def _to_submission(row: asyncpg.Record) -> Submission:
    return Submission(
        id=row["id"],
        campaign_id=row["campaign_id"],
        creator_id=row["creator_id"],
        content_url=row["content_url"],
        state=SubmissionState(row["state"]),
        reviewed_at=row["reviewed_at"].isoformat() if row["reviewed_at"] else None,
        paid_at=row["paid_at"].isoformat() if row["paid_at"] else None,
        created_at=row["created_at"].isoformat(),
    )


class SubmissionRepository:
    def __init__(self, db: asyncpg.Pool | asyncpg.Connection):
        self.db = db

    async def find_by_id(self, id: int) -> Submission | None:
        row = await self.db.fetchrow("SELECT * FROM submissions WHERE id = $1", id)
        return _to_submission(row) if row else None

    async def find_by_campaign_id(self, campaign_id: int) -> list[Submission]:
        rows = await self.db.fetch(
            "SELECT * FROM submissions WHERE campaign_id = $1 ORDER BY created_at DESC",
            campaign_id,
        )
        return [_to_submission(r) for r in rows]

    async def count_by_campaign_id(self, campaign_id: int) -> int:
        return await self.db.fetchval(
            "SELECT COUNT(*) FROM submissions WHERE campaign_id = $1", campaign_id
        )

    async def create(
        self, campaign_id: int, creator_id: str, content_url: str
    ) -> Submission:
        row = await self.db.fetchrow(
            """
            INSERT INTO submissions (campaign_id, creator_id, content_url, state)
            VALUES ($1, $2, $3, $4)
            RETURNING *
            """,
            campaign_id, creator_id, content_url, SubmissionState.PENDING.value,
        )
        return _to_submission(row)

    async def update_state(self, id: int, state: SubmissionState) -> Submission:
        if state in (SubmissionState.APPROVED, SubmissionState.REJECTED):
            await self.db.execute(
                "UPDATE submissions SET state = $1, reviewed_at = now() WHERE id = $2",
                state.value, id,
            )
        elif state is SubmissionState.PAID:
            await self.db.execute(
                "UPDATE submissions SET state = $1, paid_at = now() WHERE id = $2",
                state.value, id,
            )
        else:
            await self.db.execute(
                "UPDATE submissions SET state = $1 WHERE id = $2", state.value, id
            )
        return await self.find_by_id(id)
