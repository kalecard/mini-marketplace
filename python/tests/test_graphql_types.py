from src.data.enums import CampaignState, SubmissionState
from src.data.models import Campaign as CampaignModel, Submission as SubmissionModel
from src.graphql.types import Campaign, Submission


def test_campaign_from_model_maps_fields():
    model = CampaignModel(
        id=7, brand_id=3, title="T", description=None, payout_cents=5000,
        max_submissions=100, state=CampaignState.DRAFT, starts_at=None, ends_at=None,
        created_at="2026-01-01T00:00:00+00:00",
    )
    gql = Campaign.from_model(model)
    assert gql.id == "7"
    assert gql.brand_id == "3"
    assert gql.state is CampaignState.DRAFT
    assert gql.payout_cents == 5000


def test_submission_from_model_maps_fields():
    model = SubmissionModel(
        id=9, campaign_id=7, creator_id="creator-1", content_url="u",
        state=SubmissionState.PENDING, reviewed_at=None, paid_at=None,
        created_at="2026-01-01T00:00:00+00:00",
    )
    gql = Submission.from_model(model)
    assert gql.id == "9"
    assert gql.campaign_id == "7"
    assert gql.creator_id == "creator-1"
    assert gql.state is SubmissionState.PENDING
