import { createServer } from "node:http";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { createSchema, createYoga } from "graphql-yoga";
import { createPool } from "./db/pool.js";
import { migrate } from "./db/migrate.js";
import { CampaignService } from "./services/campaign-service.js";
import { SubmissionService } from "./services/submission-service.js";
import { createResolvers } from "./graphql/resolvers.js";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

async function main() {
  const pool = createPool();
  await migrate(pool);

  const campaignService = new CampaignService(pool);
  const submissionService = new SubmissionService(pool);

  const typeDefs = fs.readFileSync(
    path.join(__dirname, "schema.graphqls"),
    "utf-8",
  );

  const yoga = createYoga({
    schema: createSchema({
      typeDefs,
      resolvers: createResolvers(campaignService, submissionService),
    }),
    graphqlEndpoint: "/graphql",
    landingPage: true,
  });

  const server = createServer(yoga);
  const port = process.env.PORT ?? 8080;
  server.listen(port, () => {
    console.log(`Server running at http://localhost:${port}/graphql`);
  });
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
