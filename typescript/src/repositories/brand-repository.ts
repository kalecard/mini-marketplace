import type { QueryResultRow } from "pg";
import type { Brand } from "../data/models.js";
import type { PoolOrClient } from "../db/pool.js";

function toBrand(row: QueryResultRow): Brand {
  return {
    id: Number(row.id),
    name: row.name,
    balanceCents: Number(row.balance_cents),
    createdAt: row.created_at.toISOString(),
  };
}

export class BrandRepository {
  constructor(private db: PoolOrClient) {}

  async findById(id: number): Promise<Brand | null> {
    const { rows } = await this.db.query(
      "SELECT * FROM brands WHERE id = $1",
      [id],
    );
    return rows.length > 0 ? toBrand(rows[0]) : null;
  }

  async updateBalance(id: number, balanceCents: number): Promise<void> {
    await this.db.query(
      "UPDATE brands SET balance_cents = $1 WHERE id = $2",
      [balanceCents, id],
    );
  }
}
