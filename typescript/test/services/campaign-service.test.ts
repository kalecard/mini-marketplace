import { describe, it, expect, beforeAll, beforeEach, afterAll } from "vitest";
import type pg from "pg";
import { CampaignService } from "../../src/services/campaign-service.js";
import { SubmissionService } from "../../src/services/submission-service.js";
import { CampaignState } from "../../src/data/enums.js";
import { SubmissionState } from "../../src/data/enums.js";
import { getTestPool, cleanDatabase, teardown } from "../setup.js";

describe("CampaignService", () => {
  let pool: pg.Pool;
  let campaignService: CampaignService;
  let submissionService: SubmissionService;
  let brandId: number;

  beforeAll(async () => {
    pool = await getTestPool();
    campaignService = new CampaignService(pool);
    submissionService = new SubmissionService(pool);
  });

  afterAll(async () => {
    await teardown();
  });

  beforeEach(async () => {
    await cleanDatabase(pool);

    const brandResult = await pool.query(
      "INSERT INTO brands (name, balance_cents) VALUES ($1, $2) RETURNING id",
      ["Test Brand", 100_000],
    );
    brandId = Number(brandResult.rows[0].id);

    await pool.query(
      "INSERT INTO creators (id, display_name, email) VALUES ($1, $2, $3)",
      ["creator-1", "Test Creator", "creator@test.com"],
    );
  });

  it("createCampaign creates a campaign in DRAFT state", async () => {
    const campaign = await campaignService.createCampaign(
      brandId, "Test Campaign", "A description", 50_00, 50,
    );

    expect(campaign.id).toBeDefined();
    expect(campaign.title).toBe("Test Campaign");
    expect(campaign.description).toBe("A description");
    expect(campaign.payoutCents).toBe(50_00);
    expect(campaign.maxSubmissions).toBe(50);
    expect(campaign.state).toBe(CampaignState.DRAFT);
  });

  it("createCampaign fails for nonexistent brand", async () => {
    await expect(
      campaignService.createCampaign(99999, "Bad Campaign", null, 50_00, 50),
    ).rejects.toThrow("Brand 99999 not found");
  });

  it("activateCampaign transitions from DRAFT to ACTIVE", async () => {
    const campaign = await campaignService.createCampaign(
      brandId, "Test Campaign", null, 50_00, 50,
    );
    const activated = await campaignService.activateCampaign(campaign.id);

    expect(activated.state).toBe(CampaignState.ACTIVE);
  });

  it("activateCampaign throws when campaign is already ACTIVE", async () => {
    const campaign = await campaignService.createCampaign(
      brandId, "Test Campaign", null, 50_00, 50,
    );
    await campaignService.activateCampaign(campaign.id);

    await expect(
      campaignService.activateCampaign(campaign.id),
    ).rejects.toThrow("Campaign must be in DRAFT state to activate");
  });

  it("getCampaigns returns all campaigns", async () => {
    await campaignService.createCampaign(brandId, "Campaign 1", null, 50_00, 50);
    await campaignService.createCampaign(brandId, "Campaign 2", null, 100_00, 25);

    const campaigns = await campaignService.getCampaigns();
    expect(campaigns).toHaveLength(2);
  });

  it("submitContent creates a submission for an active campaign", async () => {
    const campaign = await campaignService.createCampaign(
      brandId, "Test Campaign", null, 50_00, 50,
    );
    await campaignService.activateCampaign(campaign.id);

    const submission = await submissionService.submitContent(
      campaign.id, "creator-1", "https://example.com/video.mp4",
    );

    expect(submission.id).toBeDefined();
    expect(submission.campaignId).toBe(campaign.id);
    expect(submission.creatorId).toBe("creator-1");
    expect(submission.state).toBe(SubmissionState.PENDING);
  });

  it("submitContent fails for a DRAFT campaign", async () => {
    const campaign = await campaignService.createCampaign(
      brandId, "Test Campaign", null, 50_00, 50,
    );

    await expect(
      submissionService.submitContent(
        campaign.id, "creator-1", "https://example.com/video.mp4",
      ),
    ).rejects.toThrow("Campaign is not active");
  });

  it("approveSubmission transitions from PENDING to APPROVED", async () => {
    const campaign = await campaignService.createCampaign(
      brandId, "Test Campaign", null, 50_00, 50,
    );
    await campaignService.activateCampaign(campaign.id);
    const submission = await submissionService.submitContent(
      campaign.id, "creator-1", "https://example.com/video.mp4",
    );

    const approved = await submissionService.approveSubmission(submission.id);

    expect(approved.state).toBe(SubmissionState.APPROVED);
    expect(approved.reviewedAt).not.toBeNull();
  });

  it("rejectSubmission transitions from PENDING to REJECTED", async () => {
    const campaign = await campaignService.createCampaign(
      brandId, "Test Campaign", null, 50_00, 50,
    );
    await campaignService.activateCampaign(campaign.id);
    const submission = await submissionService.submitContent(
      campaign.id, "creator-1", "https://example.com/video.mp4",
    );

    const rejected = await submissionService.rejectSubmission(submission.id);

    expect(rejected.state).toBe(SubmissionState.REJECTED);
    expect(rejected.reviewedAt).not.toBeNull();
  });
});
