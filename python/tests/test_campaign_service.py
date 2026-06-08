import pytest

from src.data.enums import CampaignState
from src.services.campaign_service import CampaignService


async def test_create_campaign_creates_draft(pool, brand_id):
    service = CampaignService(pool)
    campaign = await service.create_campaign(brand_id, "Test Campaign", "A description", 5000, 50)
    assert campaign.id is not None
    assert campaign.title == "Test Campaign"
    assert campaign.description == "A description"
    assert campaign.payout_cents == 5000
    assert campaign.max_submissions == 50
    assert campaign.state is CampaignState.DRAFT


async def test_create_campaign_fails_for_nonexistent_brand(pool, brand_id):
    service = CampaignService(pool)
    with pytest.raises(ValueError, match="Brand 99999 not found"):
        await service.create_campaign(99999, "Bad Campaign", None, 5000, 50)


async def test_activate_campaign_draft_to_active(pool, brand_id):
    service = CampaignService(pool)
    campaign = await service.create_campaign(brand_id, "Test Campaign", None, 5000, 50)
    activated = await service.activate_campaign(campaign.id)
    assert activated.state is CampaignState.ACTIVE


async def test_activate_campaign_fails_when_already_active(pool, brand_id):
    service = CampaignService(pool)
    campaign = await service.create_campaign(brand_id, "Test Campaign", None, 5000, 50)
    await service.activate_campaign(campaign.id)
    with pytest.raises(ValueError, match="Campaign must be in DRAFT state to activate"):
        await service.activate_campaign(campaign.id)


async def test_get_campaigns_returns_all(pool, brand_id):
    service = CampaignService(pool)
    await service.create_campaign(brand_id, "Campaign 1", None, 5000, 50)
    await service.create_campaign(brand_id, "Campaign 2", None, 10000, 25)
    campaigns = await service.get_campaigns()
    assert len(campaigns) == 2
