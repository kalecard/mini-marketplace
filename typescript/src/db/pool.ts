import pg from "pg";

export function createPool(connectionString?: string): pg.Pool {
  return new pg.Pool({
    connectionString:
      connectionString ?? "postgresql://postgres:postgres@localhost:5433/interview",
  });
}

export type PoolOrClient = pg.Pool | pg.PoolClient;

export async function withTransaction<T>(
  pool: pg.Pool,
  fn: (client: pg.PoolClient) => Promise<T>,
): Promise<T> {
  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    const result = await fn(client);
    await client.query("COMMIT");
    return result;
  } catch (err) {
    await client.query("ROLLBACK");
    throw err;
  } finally {
    client.release();
  }
}
