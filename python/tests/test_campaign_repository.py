from src.data.enums import CampaignState
from src.repositories.campaign_repository import CampaignRepository


async def test_create_and_find(pool, brand_id):
    repo = CampaignRepository(pool)
    created = await repo.create(brand_id, "Summer", "desc", 5000, 50)
    assert created.id is not None
    assert created.brand_id == brand_id
    assert created.title == "Summer"
    assert created.description == "desc"
    assert created.payout_cents == 5000
    assert created.max_submissions == 50
    assert created.state is CampaignState.DRAFT

    found = await repo.find_by_id(created.id)
    assert found == created


async def test_find_by_id_missing(pool, brand_id):
    repo = CampaignRepository(pool)
    assert await repo.find_by_id(999_999) is None


async def test_find_all_orders_newest_first(pool, brand_id):
    repo = CampaignRepository(pool)
    first = await repo.create(brand_id, "First", None, 5000, 50)
    second = await repo.create(brand_id, "Second", None, 5000, 50)
    all_campaigns = await repo.find_all()
    assert [c.id for c in all_campaigns] == [second.id, first.id]


async def test_update_state(pool, brand_id):
    repo = CampaignRepository(pool)
    created = await repo.create(brand_id, "Summer", None, 5000, 50)
    updated = await repo.update_state(created.id, CampaignState.ACTIVE)
    assert updated.state is CampaignState.ACTIVE
