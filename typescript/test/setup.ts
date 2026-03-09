import { PostgreSqlContainer, type StartedPostgreSqlContainer } from "@testcontainers/postgresql";
import pg from "pg";
import { migrate } from "../src/db/migrate.js";

let container: StartedPostgreSqlContainer;
let pool: pg.Pool;

export async function getTestPool(): Promise<pg.Pool> {
  if (pool) return pool;

  container = await new PostgreSqlContainer("postgres:16-alpine")
    .withDatabase("interview_test")
    .withUsername("test")
    .withPassword("test")
    .start();

  pool = new pg.Pool({
    connectionString: container.getConnectionUri(),
  });

  await migrate(pool);
  return pool;
}

export async function cleanDatabase(testPool: pg.Pool): Promise<void> {
  await testPool.query("DELETE FROM submissions");
  await testPool.query("DELETE FROM campaigns");
  await testPool.query("DELETE FROM creators");
  await testPool.query("DELETE FROM brands");
}

export async function teardown(): Promise<void> {
  if (pool) await pool.end();
  if (container) await container.stop();
}
