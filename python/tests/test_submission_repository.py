from src.data.enums import CampaignState, SubmissionState
from src.repositories.campaign_repository import CampaignRepository
from src.repositories.submission_repository import SubmissionRepository


async def _make_campaign(pool, brand_id):
    return await CampaignRepository(pool).create(brand_id, "C", None, 5000, 50)


async def test_create_and_find(pool, brand_id):
    campaign = await _make_campaign(pool, brand_id)
    repo = SubmissionRepository(pool)
    created = await repo.create(campaign.id, "creator-1", "https://x/v.mp4")
    assert created.id is not None
    assert created.campaign_id == campaign.id
    assert created.creator_id == "creator-1"
    assert created.content_url == "https://x/v.mp4"
    assert created.state is SubmissionState.PENDING
    assert created.reviewed_at is None
    assert created.paid_at is None

    assert await repo.find_by_id(created.id) == created


async def test_count_and_find_by_campaign(pool, brand_id):
    campaign = await _make_campaign(pool, brand_id)
    repo = SubmissionRepository(pool)
    await repo.create(campaign.id, "creator-1", "u1")
    await repo.create(campaign.id, "creator-1", "u2")
    assert await repo.count_by_campaign_id(campaign.id) == 2
    assert len(await repo.find_by_campaign_id(campaign.id)) == 2


async def test_update_state_approved_sets_reviewed_at(pool, brand_id):
    campaign = await _make_campaign(pool, brand_id)
    repo = SubmissionRepository(pool)
    created = await repo.create(campaign.id, "creator-1", "u")
    updated = await repo.update_state(created.id, SubmissionState.APPROVED)
    assert updated.state is SubmissionState.APPROVED
    assert updated.reviewed_at is not None
    assert updated.paid_at is None


async def test_update_state_paid_sets_paid_at(pool, brand_id):
    campaign = await _make_campaign(pool, brand_id)
    repo = SubmissionRepository(pool)
    created = await repo.create(campaign.id, "creator-1", "u")
    updated = await repo.update_state(created.id, SubmissionState.PAID)
    assert updated.state is SubmissionState.PAID
    assert updated.paid_at is not None
