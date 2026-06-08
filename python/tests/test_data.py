from src.data.enums import CampaignState, SubmissionState
from src.data.models import Brand, Creator, Campaign, Submission


def test_enum_values():
    assert CampaignState.DRAFT.value == "DRAFT"
    assert CampaignState.ACTIVE.value == "ACTIVE"
    assert CampaignState.COMPLETED.value == "COMPLETED"
    assert SubmissionState.PENDING.value == "PENDING"
    assert SubmissionState.APPROVED.value == "APPROVED"
    assert SubmissionState.REJECTED.value == "REJECTED"
    assert SubmissionState.PAID.value == "PAID"


def test_models_construct():
    brand = Brand(id=1, name="Acme", balance_cents=0, created_at="2026-01-01T00:00:00+00:00")
    assert brand.name == "Acme"
    campaign = Campaign(
        id=1, brand_id=1, title="T", description=None, payout_cents=5000,
        max_submissions=100, state=CampaignState.DRAFT, starts_at=None, ends_at=None,
        created_at="2026-01-01T00:00:00+00:00",
    )
    assert campaign.state is CampaignState.DRAFT
    sub = Submission(
        id=1, campaign_id=1, creator_id="creator-1", content_url="u",
        state=SubmissionState.PENDING, reviewed_at=None, paid_at=None,
        created_at="2026-01-01T00:00:00+00:00",
    )
    assert sub.creator_id == "creator-1"
    creator = Creator(id="creator-1", display_name="C", email="c@x.com",
                      balance_cents=0, created_at="2026-01-01T00:00:00+00:00")
    assert creator.email == "c@x.com"
