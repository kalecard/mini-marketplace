import asyncpg

from src.data.enums import CampaignState, SubmissionState
from src.data.models import Submission
from src.db.pool import with_transaction
from src.repositories.campaign_repository import CampaignRepository
from src.repositories.creator_repository import CreatorRepository
from src.repositories.submission_repository import SubmissionRepository


class SubmissionService:
    def __init__(self, pool: asyncpg.Pool):
        self.pool = pool

    async def get_submissions_by_campaign(self, campaign_id: int) -> list[Submission]:
        return await SubmissionRepository(self.pool).find_by_campaign_id(campaign_id)

    async def get_submission(self, id: int) -> Submission | None:
        return await SubmissionRepository(self.pool).find_by_id(id)

    async def submit_content(
        self, campaign_id: int, creator_id: str, content_url: str
    ) -> Submission:
        async def txn(conn: asyncpg.Connection) -> Submission:
            campaign = await CampaignRepository(conn).find_by_id(campaign_id)
            if campaign is None:
                raise ValueError(f"Campaign {campaign_id} not found")
            if campaign.state is not CampaignState.ACTIVE:
                raise ValueError("Campaign is not active")

            creator = await CreatorRepository(conn).find_by_id(creator_id)
            if creator is None:
                raise ValueError(f"Creator {creator_id} not found")

            submission_repo = SubmissionRepository(conn)
            count = await submission_repo.count_by_campaign_id(campaign_id)
            if count >= campaign.max_submissions:
                raise ValueError("Campaign has reached maximum submissions")

            return await submission_repo.create(campaign_id, creator_id, content_url)

        return await with_transaction(self.pool, txn)

    async def approve_submission(self, id: int) -> Submission:
        return await self._review(id, SubmissionState.APPROVED, "approve")

    async def reject_submission(self, id: int) -> Submission:
        return await self._review(id, SubmissionState.REJECTED, "reject")

    async def _review(self, id: int, target: SubmissionState, verb: str) -> Submission:
        async def txn(conn: asyncpg.Connection) -> Submission:
            repo = SubmissionRepository(conn)
            submission = await repo.find_by_id(id)
            if submission is None:
                raise ValueError(f"Submission {id} not found")
            if submission.state is not SubmissionState.PENDING:
                raise ValueError(f"Submission must be in PENDING state to {verb}")
            return await repo.update_state(id, target)

        return await with_transaction(self.pool, txn)
