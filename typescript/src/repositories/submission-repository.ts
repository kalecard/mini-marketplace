import type { QueryResultRow } from "pg";
import type { Submission } from "../data/models.js";
import { SubmissionState } from "../data/enums.js";
import type { PoolOrClient } from "../db/pool.js";

function toSubmission(row: QueryResultRow): Submission {
  return {
    id: Number(row.id),
    campaignId: Number(row.campaign_id),
    creatorId: row.creator_id,
    contentUrl: row.content_url,
    state: row.state as SubmissionState,
    reviewedAt: row.reviewed_at?.toISOString() ?? null,
    paidAt: row.paid_at?.toISOString() ?? null,
    createdAt: row.created_at.toISOString(),
  };
}

export class SubmissionRepository {
  constructor(private db: PoolOrClient) {}

  async findById(id: number): Promise<Submission | null> {
    const { rows } = await this.db.query(
      "SELECT * FROM submissions WHERE id = $1",
      [id],
    );
    return rows.length > 0 ? toSubmission(rows[0]) : null;
  }

  async findByCampaignId(campaignId: number): Promise<Submission[]> {
    const { rows } = await this.db.query(
      "SELECT * FROM submissions WHERE campaign_id = $1 ORDER BY created_at DESC",
      [campaignId],
    );
    return rows.map(toSubmission);
  }

  async countByCampaignId(campaignId: number): Promise<number> {
    const { rows } = await this.db.query(
      "SELECT COUNT(*)::int AS count FROM submissions WHERE campaign_id = $1",
      [campaignId],
    );
    return rows[0].count;
  }

  async create(
    campaignId: number,
    creatorId: string,
    contentUrl: string,
  ): Promise<Submission> {
    const { rows } = await this.db.query(
      `INSERT INTO submissions (campaign_id, creator_id, content_url, state)
       VALUES ($1, $2, $3, $4)
       RETURNING *`,
      [campaignId, creatorId, contentUrl, SubmissionState.PENDING],
    );
    return toSubmission(rows[0]);
  }

  async updateState(id: number, state: SubmissionState): Promise<Submission> {
    if (state === SubmissionState.APPROVED || state === SubmissionState.REJECTED) {
      await this.db.query(
        "UPDATE submissions SET state = $1, reviewed_at = now() WHERE id = $2",
        [state, id],
      );
    } else if (state === SubmissionState.PAID) {
      await this.db.query(
        "UPDATE submissions SET state = $1, paid_at = now() WHERE id = $2",
        [state, id],
      );
    } else {
      await this.db.query(
        "UPDATE submissions SET state = $1 WHERE id = $2",
        [state, id],
      );
    }
    return (await this.findById(id))!;
  }
}
