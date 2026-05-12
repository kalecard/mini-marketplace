-- Pair-programming Task 3 seed: campaign budget guard scenario.
--
-- Sets up a brand with exactly one payout's worth of balance, an active campaign,
-- and two pending submissions from two different creators. Once the budget guard
-- is implemented, calling `approveSubmission` from GraphiQL on submission 1
-- succeeds and on submission 2 fails with an insufficient-balance error.
--
-- Usage (from the repo root, with `docker-compose up -d` already running):
--   docker exec -i $(docker-compose ps -q postgres) \
--     psql -U postgres -d interview < seed-task3-budget.sql
--
-- The script is destructive: it truncates brands/creators/campaigns/submissions
-- so it can be re-run from a clean slate. If you have data you want to keep,
-- comment out the TRUNCATE.

BEGIN;

TRUNCATE submissions, campaigns, creators, brands RESTART IDENTITY CASCADE;

-- Brand has exactly one payout's worth of balance: 5000 cents.
INSERT INTO brands (name, balance_cents) VALUES ('Acme', 5000);

-- Two creators -- one per pending submission.
INSERT INTO creators (id, display_name, email) VALUES
  ('creator-1', 'Creator One', 'c1@example.com'),
  ('creator-2', 'Creator Two', 'c2@example.com');

-- Active campaign paying 5000 cents per submission.
INSERT INTO campaigns (brand_id, title, payout_cents, max_submissions, state)
  VALUES (1, 'Budget Test', 5000, 50, 'ACTIVE');

-- Two pending submissions -- the candidate's mutation should approve the first
-- and reject the second once the budget guard is in place.
INSERT INTO submissions (campaign_id, creator_id, content_url, state) VALUES
  (1, 'creator-1', 'https://example.com/v1.mp4', 'PENDING'),
  (1, 'creator-2', 'https://example.com/v2.mp4', 'PENDING');

COMMIT;

-- After running, try the following in GraphiQL (http://localhost:8080/graphiql):
--   mutation { approveSubmission(submissionId: "1") { id state } }   # APPROVED
--   mutation { approveSubmission(submissionId: "2") { id state } }   # fails once guard exists
--   query    { submissions(campaignId: "1") { id state } }           # sub 2 still PENDING
