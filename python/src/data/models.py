from dataclasses import dataclass

from src.data.enums import CampaignState, SubmissionState


@dataclass(frozen=True)
class Brand:
    id: int
    name: str
    balance_cents: int
    created_at: str


@dataclass(frozen=True)
class Creator:
    id: str
    display_name: str
    email: str
    balance_cents: int
    created_at: str


@dataclass(frozen=True)
class Campaign:
    id: int
    brand_id: int
    title: str
    description: str | None
    payout_cents: int
    max_submissions: int
    state: CampaignState
    starts_at: str | None
    ends_at: str | None
    created_at: str


@dataclass(frozen=True)
class Submission:
    id: int
    campaign_id: int
    creator_id: str
    content_url: str
    state: SubmissionState
    reviewed_at: str | None
    paid_at: str | None
    created_at: str
