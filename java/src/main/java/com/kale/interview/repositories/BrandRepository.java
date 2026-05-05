package com.kale.interview.repositories;

import com.kale.interview.data.Brand;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class BrandRepository {

    private final DSLContext dsl;

    public BrandRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Brand findById(long id) {
        Record row = dsl.selectFrom(DSL.table("brands"))
                .where(DSL.field("id", Long.class).eq(id))
                .fetchOne();
        return row == null ? null : toBrand(row);
    }

    public void updateBalance(long id, long balanceCents) {
        dsl.update(DSL.table("brands"))
                .set(DSL.field("balance_cents", Long.class), balanceCents)
                .where(DSL.field("id", Long.class).eq(id))
                .execute();
    }

    static Brand toBrand(Record row) {
        return new Brand(
                row.get("id", Long.class),
                row.get("name", String.class),
                row.get("balance_cents", Long.class),
                row.get("created_at", OffsetDateTime.class).toString()
        );
    }
}
