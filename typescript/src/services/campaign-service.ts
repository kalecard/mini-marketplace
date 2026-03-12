import type pg from "pg";
import type { Campaign } from "../data/models.js";
import { CampaignState } from "../data/enums.js";
import { BrandRepository } from "../repositories/brand-repository.js";
import { CampaignRepository } from "../repositories/campaign-repository.js";
import { withTransaction } from "../db/pool.js";

export class CampaignService {
  constructor(private pool: pg.Pool) {}

  async getCampaigns(): Promise<Campaign[]> {
    const repo = new CampaignRepository(this.pool);
    return repo.findAll();
  }

  async getCampaign(id: number): Promise<Campaign | null> {
    const repo = new CampaignRepository(this.pool);
    return repo.findById(id);
  }

  async createCampaign(
    brandId: number,
    title: string,
    description: string | null,
    payoutCents: number,
    maxSubmissions: number,
  ): Promise<Campaign> {
    return withTransaction(this.pool, async (client) => {
      const brandRepo = new BrandRepository(client);
      const brand = await brandRepo.findById(brandId);
      if (!brand) {
        throw new Error(`Brand ${brandId} not found`);
      }
      const campaignRepo = new CampaignRepository(client);
      return campaignRepo.create(brandId, title, description, payoutCents, maxSubmissions);
    });
  }

  async activateCampaign(id: number): Promise<Campaign> {
    return withTransaction(this.pool, async (client) => {
      const repo = new CampaignRepository(client);
      const campaign = await repo.findById(id);
      if (!campaign) {
        throw new Error(`Campaign ${id} not found`);
      }
      if (campaign.state !== CampaignState.DRAFT) {
        throw new Error("Campaign must be in DRAFT state to activate");
      }
      return repo.updateState(id, CampaignState.ACTIVE);
    });
  }

  async completeCampaign(id: number): Promise<Campaign> {
    return withTransaction(this.pool, async (client) => {
      const repo = new CampaignRepository(client);
      const campaign = await repo.findById(id);
      if (!campaign) {
        throw new Error(`Campaign ${id} not found`);
      }
      if (campaign.state !== CampaignState.DRAFT) {
        throw new Error("Campaign must be in ACTIVE state to complete");
      }
      return repo.updateState(id, CampaignState.COMPLETED);
    });
  }
}
