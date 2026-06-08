import pytest

from src.graphql.schema import schema
from src.services.campaign_service import CampaignService
from src.services.submission_service import SubmissionService


def _context(pool):
    return {
        "campaign_service": CampaignService(pool),
        "submission_service": SubmissionService(pool),
    }


async def test_create_campaign_mutation(pool, brand_id):
    query = """
    mutation($input: CreateCampaignInput!) {
      createCampaign(input: $input) { id title state payoutCents maxSubmissions }
    }
    """
    result = await schema.execute(
        query,
        variable_values={"input": {
            "brandId": str(brand_id), "title": "Summer", "payoutCents": 5000,
        }},
        context_value=_context(pool),
    )
    assert result.errors is None
    data = result.data["createCampaign"]
    assert data["title"] == "Summer"
    assert data["state"] == "DRAFT"
    assert data["maxSubmissions"] == 100  # default applied in resolver


async def test_campaigns_query(pool, brand_id):
    create = """
    mutation($input: CreateCampaignInput!) { createCampaign(input: $input) { id } }
    """
    await schema.execute(
        create,
        variable_values={"input": {"brandId": str(brand_id), "title": "A", "payoutCents": 1}},
        context_value=_context(pool),
    )
    result = await schema.execute("{ campaigns { id title } }", context_value=_context(pool))
    assert result.errors is None
    assert len(result.data["campaigns"]) == 1


async def test_validation_error_surfaces(pool, brand_id):
    query = """
    mutation($id: ID!) { activateCampaign(campaignId: $id) { id } }
    """
    result = await schema.execute(
        query, variable_values={"id": "999999"}, context_value=_context(pool)
    )
    assert result.errors is not None
    assert "not found" in result.errors[0].message
