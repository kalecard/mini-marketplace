import { CampaignState, SubmissionState } from "./enums.js";

export interface Brand {
  id: number;
  name: string;
  balanceCents: number;
  createdAt: string;
}

export interface Creator {
  id: string;
  displayName: string;
  email: string;
  balanceCents: number;
  createdAt: string;
}

export interface Campaign {
  id: number;
  brandId: number;
  title: string;
  description: string | null;
  payoutCents: number;
  maxSubmissions: number;
  state: CampaignState;
  startsAt: string | null;
  endsAt: string | null;
  createdAt: string;
}

export interface Submission {
  id: number;
  campaignId: number;
  creatorId: string;
  contentUrl: string;
  state: SubmissionState;
  reviewedAt: string | null;
  paidAt: string | null;
  createdAt: string;
}
