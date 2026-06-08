import asyncpg

from src.data.enums import CampaignState
from src.data.models import Campaign
from src.db.pool import with_transaction
from src.repositories.brand_repository import BrandRepository
from src.repositories.campaign_repository import CampaignRepository


class CampaignService:
    def __init__(self, pool: asyncpg.Pool):
        self.pool = pool

    async def get_campaigns(self) -> list[Campaign]:
        return await CampaignRepository(self.pool).find_all()

    async def get_campaign(self, id: int) -> Campaign | None:
        return await CampaignRepository(self.pool).find_by_id(id)

    async def create_campaign(
        self,
        brand_id: int,
        title: str,
        description: str | None,
        payout_cents: int,
        max_submissions: int,
    ) -> Campaign:
        async def txn(conn: asyncpg.Connection) -> Campaign:
            brand = await BrandRepository(conn).find_by_id(brand_id)
            if brand is None:
                raise ValueError(f"Brand {brand_id} not found")
            return await CampaignRepository(conn).create(
                brand_id, title, description, payout_cents, max_submissions
            )

        return await with_transaction(self.pool, txn)

    async def activate_campaign(self, id: int) -> Campaign:
        async def txn(conn: asyncpg.Connection) -> Campaign:
            repo = CampaignRepository(conn)
            campaign = await repo.find_by_id(id)
            if campaign is None:
                raise ValueError(f"Campaign {id} not found")
            if campaign.state is not CampaignState.DRAFT:
                raise ValueError("Campaign must be in DRAFT state to activate")
            return await repo.update_state(id, CampaignState.ACTIVE)

        return await with_transaction(self.pool, txn)
