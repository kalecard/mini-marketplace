from src.repositories.brand_repository import BrandRepository
from src.repositories.creator_repository import CreatorRepository


async def test_brand_find_by_id(pool, brand_id):
    repo = BrandRepository(pool)
    brand = await repo.find_by_id(brand_id)
    assert brand is not None
    assert brand.name == "Test Brand"
    assert brand.balance_cents == 100_000


async def test_brand_find_by_id_missing(pool, brand_id):
    repo = BrandRepository(pool)
    assert await repo.find_by_id(999_999) is None


async def test_brand_update_balance(pool, brand_id):
    repo = BrandRepository(pool)
    await repo.update_balance(brand_id, 250)
    brand = await repo.find_by_id(brand_id)
    assert brand.balance_cents == 250


async def test_creator_find_by_id(pool, brand_id):
    repo = CreatorRepository(pool)
    creator = await repo.find_by_id("creator-1")
    assert creator is not None
    assert creator.display_name == "Test Creator"


async def test_creator_find_by_id_missing(pool, brand_id):
    repo = CreatorRepository(pool)
    assert await repo.find_by_id("nope") is None
