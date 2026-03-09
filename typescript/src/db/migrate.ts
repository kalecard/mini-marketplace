import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import type pg from "pg";
import type { PoolOrClient } from "./pool.js";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export async function migrate(db: PoolOrClient): Promise<void> {
  const sql = fs.readFileSync(
    path.join(__dirname, "migrations", "V1__initial.sql"),
    "utf-8",
  );
  await db.query(sql);
}
