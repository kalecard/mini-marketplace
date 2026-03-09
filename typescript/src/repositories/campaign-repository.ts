import type { QueryResultRow } from "pg";
import type { Campaign } from "../data/models.js";
import { CampaignState } from "../data/enums.js";
import type { PoolOrClient } from "../db/pool.js";

function toCampaign(row: QueryResultRow): Campaign {
  return {
    id: Number(row.id),
    brandId: Number(row.brand_id),
    title: row.title,
    description: row.description ?? null,
    payoutCents: Number(row.payout_cents),
    maxSubmissions: Number(row.max_submissions),
    state: row.state as CampaignState,
    startsAt: row.starts_at?.toISOString() ?? null,
    endsAt: row.ends_at?.toISOString() ?? null,
    createdAt: row.created_at.toISOString(),
  };
}

export class CampaignRepository {
  constructor(private db: PoolOrClient) {}

  async findById(id: number): Promise<Campaign | null> {
    const { rows } = await this.db.query(
      "SELECT * FROM campaigns WHERE id = $1",
      [id],
    );
    return rows.length > 0 ? toCampaign(rows[0]) : null;
  }

  async findAll(): Promise<Campaign[]> {
    const { rows } = await this.db.query(
      "SELECT * FROM campaigns ORDER BY created_at DESC",
    );
    return rows.map(toCampaign);
  }

  async create(
    brandId: number,
    title: string,
    description: string | null,
    payoutCents: number,
    maxSubmissions: number,
  ): Promise<Campaign> {
    const { rows } = await this.db.query(
      `INSERT INTO campaigns (brand_id, title, description, payout_cents, max_submissions, state)
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING *`,
      [brandId, title, description, payoutCents, maxSubmissions, CampaignState.DRAFT],
    );
    return toCampaign(rows[0]);
  }

  async updateState(id: number, state: CampaignState): Promise<Campaign> {
    await this.db.query(
      "UPDATE campaigns SET state = $1 WHERE id = $2",
      [state, id],
    );
    return (await this.findById(id))!;
  }
}
