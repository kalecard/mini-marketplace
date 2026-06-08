import strawberry
from strawberry.types.unset import UNSET

from src.data import enums, models

# Register the domain enums with Strawberry (code-first → these become GraphQL enums).
CampaignState = strawberry.enum(enums.CampaignState)
SubmissionState = strawberry.enum(enums.SubmissionState)


@strawberry.type
class Campaign:
    id: strawberry.ID
    brand_id: strawberry.ID
    title: str
    description: str | None
    payout_cents: int
    max_submissions: int
    state: enums.CampaignState
    starts_at: str | None
    ends_at: str | None
    created_at: str

    @classmethod
    def from_model(cls, m: models.Campaign) -> "Campaign":
        return cls(
            id=strawberry.ID(str(m.id)),
            brand_id=strawberry.ID(str(m.brand_id)),
            title=m.title,
            description=m.description,
            payout_cents=m.payout_cents,
            max_submissions=m.max_submissions,
            state=m.state,
            starts_at=m.starts_at,
            ends_at=m.ends_at,
            created_at=m.created_at,
        )


@strawberry.type
class Submission:
    id: strawberry.ID
    campaign_id: strawberry.ID
    creator_id: strawberry.ID
    content_url: str
    state: enums.SubmissionState
    reviewed_at: str | None
    paid_at: str | None
    created_at: str

    @classmethod
    def from_model(cls, m: models.Submission) -> "Submission":
        return cls(
            id=strawberry.ID(str(m.id)),
            campaign_id=strawberry.ID(str(m.campaign_id)),
            creator_id=strawberry.ID(m.creator_id),
            content_url=m.content_url,
            state=m.state,
            reviewed_at=m.reviewed_at,
            paid_at=m.paid_at,
            created_at=m.created_at,
        )


@strawberry.input
class CreateCampaignInput:
    brand_id: strawberry.ID
    title: str
    payout_cents: int
    description: str | None = UNSET  # type: ignore[assignment]
    max_submissions: int | None = UNSET  # type: ignore[assignment]


@strawberry.input
class SubmitContentInput:
    campaign_id: strawberry.ID
    creator_id: strawberry.ID
    content_url: str
