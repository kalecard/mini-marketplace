package com.kale.interview.repositories

import com.kale.interview.data.Brand
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class BrandRepository(private val dsl: DSLContext) {

    fun findById(id: Long): Brand? {
        return dsl.selectFrom(DSL.table("brands"))
            .where(DSL.field("id", Long::class.java).eq(id))
            .fetchOne()
            ?.toBrand()
    }

    fun updateBalance(id: Long, balanceCents: Long) {
        dsl.update(DSL.table("brands"))
            .set(DSL.field("balance_cents", Long::class.java), balanceCents)
            .where(DSL.field("id", Long::class.java).eq(id))
            .execute()
    }
}

fun Record.toBrand() = Brand(
    id = get("id", Long::class.java)!!,
    name = get("name", String::class.java)!!,
    balanceCents = get("balance_cents", Long::class.java)!!,
    createdAt = get("created_at", OffsetDateTime::class.java)!!.toString(),
)
