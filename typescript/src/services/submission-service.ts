import type pg from "pg";
import type { Submission } from "../data/models.js";
import { CampaignState } from "../data/enums.js";
import { SubmissionState } from "../data/enums.js";
import { CampaignRepository } from "../repositories/campaign-repository.js";
import { CreatorRepository } from "../repositories/creator-repository.js";
import { SubmissionRepository } from "../repositories/submission-repository.js";
import { withTransaction } from "../db/pool.js";

export class SubmissionService {
  constructor(private pool: pg.Pool) {}

  async getSubmissionsByCampaign(campaignId: number): Promise<Submission[]> {
    const repo = new SubmissionRepository(this.pool);
    return repo.findByCampaignId(campaignId);
  }

  async getSubmission(id: number): Promise<Submission | null> {
    const repo = new SubmissionRepository(this.pool);
    return repo.findById(id);
  }

  async submitContent(
    campaignId: number,
    creatorId: string,
    contentUrl: string,
  ): Promise<Submission> {
    return withTransaction(this.pool, async (client) => {
      const campaignRepo = new CampaignRepository(client);
      const campaign = await campaignRepo.findById(campaignId);
      if (!campaign) {
        throw new Error(`Campaign ${campaignId} not found`);
      }
      if (campaign.state !== CampaignState.ACTIVE) {
        throw new Error("Campaign is not active");
      }

      const creatorRepo = new CreatorRepository(client);
      const creator = await creatorRepo.findById(creatorId);
      if (!creator) {
        throw new Error(`Creator ${creatorId} not found`);
      }

      const submissionRepo = new SubmissionRepository(client);
      const count = await submissionRepo.countByCampaignId(campaignId);
      if (count >= campaign.maxSubmissions) {
        throw new Error("Campaign has reached maximum submissions");
      }

      return submissionRepo.create(campaignId, creatorId, contentUrl);
    });
  }

  async approveSubmission(id: number): Promise<Submission> {
    return withTransaction(this.pool, async (client) => {
      const repo = new SubmissionRepository(client);
      const submission = await repo.findById(id);
      if (!submission) {
        throw new Error(`Submission ${id} not found`);
      }
      if (submission.state !== SubmissionState.PENDING) {
        throw new Error("Submission must be in PENDING state to approve");
      }
      return repo.updateState(id, SubmissionState.APPROVED);
    });
  }

  async rejectSubmission(id: number): Promise<Submission> {
    return withTransaction(this.pool, async (client) => {
      const repo = new SubmissionRepository(client);
      const submission = await repo.findById(id);
      if (!submission) {
        throw new Error(`Submission ${id} not found`);
      }
      if (submission.state !== SubmissionState.PENDING) {
        throw new Error("Submission must be in PENDING state to reject");
      }
      return repo.updateState(id, SubmissionState.REJECTED);
    });
  }
}
