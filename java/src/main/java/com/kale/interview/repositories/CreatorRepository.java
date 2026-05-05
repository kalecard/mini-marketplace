package com.kale.interview.repositories;

import com.kale.interview.data.Creator;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class CreatorRepository {

    private final DSLContext dsl;

    public CreatorRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Creator findById(String id) {
        Record row = dsl.selectFrom(DSL.table("creators"))
                .where(DSL.field("id", String.class).eq(id))
                .fetchOne();
        return row == null ? null : toCreator(row);
    }

    public void updateBalance(String id, long balanceCents) {
        dsl.update(DSL.table("creators"))
                .set(DSL.field("balance_cents", Long.class), balanceCents)
                .where(DSL.field("id", String.class).eq(id))
                .execute();
    }

    static Creator toCreator(Record row) {
        return new Creator(
                row.get("id", String.class),
                row.get("display_name", String.class),
                row.get("email", String.class),
                row.get("balance_cents", Long.class),
                row.get("created_at", OffsetDateTime.class).toString()
        );
    }
}
