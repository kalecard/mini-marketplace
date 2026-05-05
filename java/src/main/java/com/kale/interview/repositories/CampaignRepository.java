package com.kale.interview.repositories;

import com.kale.interview.data.Campaign;
import com.kale.interview.data.CampaignState;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class CampaignRepository {

    private final DSLContext dsl;

    public CampaignRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Campaign findById(long id) {
        Record row = dsl.selectFrom(DSL.table("campaigns"))
                .where(DSL.field("id", Long.class).eq(id))
                .fetchOne();
        return row == null ? null : toCampaign(row);
    }

    public List<Campaign> findAll() {
        return dsl.selectFrom(DSL.table("campaigns"))
                .orderBy(DSL.field("created_at").desc())
                .fetch()
                .map(CampaignRepository::toCampaign);
    }

    public Campaign create(long brandId, String title, String description, int payoutCents, int maxSubmissions) {
        Record row = dsl.fetchOne(
                """
                INSERT INTO campaigns (brand_id, title, description, payout_cents, max_submissions, state)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING *
                """,
                brandId, title, description, (long) payoutCents, maxSubmissions, CampaignState.DRAFT.name()
        );
        return toCampaign(row);
    }

    public Campaign updateState(long id, CampaignState state) {
        dsl.update(DSL.table("campaigns"))
                .set(DSL.field("state", String.class), state.name())
                .where(DSL.field("id", Long.class).eq(id))
                .execute();
        return findById(id);
    }

    static Campaign toCampaign(Record row) {
        OffsetDateTime startsAt = row.get("starts_at", OffsetDateTime.class);
        OffsetDateTime endsAt = row.get("ends_at", OffsetDateTime.class);
        return new Campaign(
                row.get("id", Long.class),
                row.get("brand_id", Long.class),
                row.get("title", String.class),
                row.get("description", String.class),
                row.get("payout_cents", Long.class).intValue(),
                row.get("max_submissions", Integer.class),
                CampaignState.valueOf(row.get("state", String.class)),
                startsAt == null ? null : startsAt.toString(),
                endsAt == null ? null : endsAt.toString(),
                row.get("created_at", OffsetDateTime.class).toString()
        );
    }
}
