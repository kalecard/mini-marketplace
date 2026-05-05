package com.kale.interview.repositories;

import com.kale.interview.data.Submission;
import com.kale.interview.data.SubmissionState;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class SubmissionRepository {

    private final DSLContext dsl;

    public SubmissionRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Submission findById(long id) {
        Record row = dsl.selectFrom(DSL.table("submissions"))
                .where(DSL.field("id", Long.class).eq(id))
                .fetchOne();
        return row == null ? null : toSubmission(row);
    }

    public List<Submission> findByCampaignId(long campaignId) {
        return dsl.selectFrom(DSL.table("submissions"))
                .where(DSL.field("campaign_id", Long.class).eq(campaignId))
                .orderBy(DSL.field("created_at").desc())
                .fetch()
                .map(SubmissionRepository::toSubmission);
    }

    public int countByCampaignId(long campaignId) {
        Integer count = dsl.selectCount()
                .from(DSL.table("submissions"))
                .where(DSL.field("campaign_id", Long.class).eq(campaignId))
                .fetchOne(0, Integer.class);
        return count == null ? 0 : count;
    }

    public Submission create(long campaignId, String creatorId, String contentUrl) {
        Record row = dsl.fetchOne(
                """
                INSERT INTO submissions (campaign_id, creator_id, content_url, state)
                VALUES (?, ?, ?, ?)
                RETURNING *
                """,
                campaignId, creatorId, contentUrl, SubmissionState.PENDING.name()
        );
        return toSubmission(row);
    }

    public Submission updateState(long id, SubmissionState state) {
        switch (state) {
            case APPROVED, REJECTED -> dsl.update(DSL.table("submissions"))
                    .set(DSL.field("state", String.class), state.name())
                    .set(DSL.field("reviewed_at", OffsetDateTime.class), OffsetDateTime.now())
                    .where(DSL.field("id", Long.class).eq(id))
                    .execute();
            case PAID -> dsl.update(DSL.table("submissions"))
                    .set(DSL.field("state", String.class), state.name())
                    .set(DSL.field("paid_at", OffsetDateTime.class), OffsetDateTime.now())
                    .where(DSL.field("id", Long.class).eq(id))
                    .execute();
            default -> dsl.update(DSL.table("submissions"))
                    .set(DSL.field("state", String.class), state.name())
                    .where(DSL.field("id", Long.class).eq(id))
                    .execute();
        }
        return findById(id);
    }

    static Submission toSubmission(Record row) {
        OffsetDateTime reviewedAt = row.get("reviewed_at", OffsetDateTime.class);
        OffsetDateTime paidAt = row.get("paid_at", OffsetDateTime.class);
        return new Submission(
                row.get("id", Long.class),
                row.get("campaign_id", Long.class),
                row.get("creator_id", String.class),
                row.get("content_url", String.class),
                SubmissionState.valueOf(row.get("state", String.class)),
                reviewedAt == null ? null : reviewedAt.toString(),
                paidAt == null ? null : paidAt.toString(),
                row.get("created_at", OffsetDateTime.class).toString()
        );
    }
}
