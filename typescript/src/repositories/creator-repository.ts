import type { QueryResultRow } from "pg";
import type { Creator } from "../data/models.js";
import type { PoolOrClient } from "../db/pool.js";

function toCreator(row: QueryResultRow): Creator {
  return {
    id: row.id,
    displayName: row.display_name,
    email: row.email,
    balanceCents: Number(row.balance_cents),
    createdAt: row.created_at.toISOString(),
  };
}

export class CreatorRepository {
  constructor(private db: PoolOrClient) {}

  async findById(id: string): Promise<Creator | null> {
    const { rows } = await this.db.query(
      "SELECT * FROM creators WHERE id = $1",
      [id],
    );
    return rows.length > 0 ? toCreator(rows[0]) : null;
  }

  async updateBalance(id: string, balanceCents: number): Promise<void> {
    await this.db.query(
      "UPDATE creators SET balance_cents = $1 WHERE id = $2",
      [balanceCents, id],
    );
  }
}
