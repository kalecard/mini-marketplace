import pytest

from src.data.enums import CampaignState, SubmissionState
from src.services.campaign_service import CampaignService
from src.services.submission_service import SubmissionService


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


async def test_submit_content_creates_submission_for_active_campaign(pool, brand_id):
    campaign_service = CampaignService(pool)
    submission_service = SubmissionService(pool)
    campaign = await campaign_service.create_campaign(brand_id, "Test Campaign", None, 5000, 50)
    await campaign_service.activate_campaign(campaign.id)

    submission = await submission_service.submit_content(
        campaign.id, "creator-1", "https://example.com/video.mp4"
    )
    assert submission.id is not None
    assert submission.campaign_id == campaign.id
    assert submission.creator_id == "creator-1"
    assert submission.state is SubmissionState.PENDING


async def test_submit_content_fails_for_draft_campaign(pool, brand_id):
    campaign_service = CampaignService(pool)
    submission_service = SubmissionService(pool)
    campaign = await campaign_service.create_campaign(brand_id, "Test Campaign", None, 5000, 50)
    with pytest.raises(ValueError, match="Campaign is not active"):
        await submission_service.submit_content(
            campaign.id, "creator-1", "https://example.com/video.mp4"
        )


async def test_approve_submission_pending_to_approved(pool, brand_id):
    campaign_service = CampaignService(pool)
    submission_service = SubmissionService(pool)
    campaign = await campaign_service.create_campaign(brand_id, "Test Campaign", None, 5000, 50)
    await campaign_service.activate_campaign(campaign.id)
    submission = await submission_service.submit_content(
        campaign.id, "creator-1", "https://example.com/video.mp4"
    )
    approved = await submission_service.approve_submission(submission.id)
    assert approved.state is SubmissionState.APPROVED
    assert approved.reviewed_at is not None


async def test_reject_submission_pending_to_rejected(pool, brand_id):
    campaign_service = CampaignService(pool)
    submission_service = SubmissionService(pool)
    campaign = await campaign_service.create_campaign(brand_id, "Test Campaign", None, 5000, 50)
    await campaign_service.activate_campaign(campaign.id)
    submission = await submission_service.submit_content(
        campaign.id, "creator-1", "https://example.com/video.mp4"
    )
    rejected = await submission_service.reject_submission(submission.id)
    assert rejected.state is SubmissionState.REJECTED
    assert rejected.reviewed_at is not None
