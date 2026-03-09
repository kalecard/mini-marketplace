package com.kale.interview.repositories

import com.kale.interview.data.Creator
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class CreatorRepository(private val dsl: DSLContext) {

    fun findById(id: String): Creator? {
        return dsl.selectFrom(DSL.table("creators"))
            .where(DSL.field("id", String::class.java).eq(id))
            .fetchOne()
            ?.toCreator()
    }

    fun updateBalance(id: String, balanceCents: Long) {
        dsl.update(DSL.table("creators"))
            .set(DSL.field("balance_cents", Long::class.java), balanceCents)
            .where(DSL.field("id", String::class.java).eq(id))
            .execute()
    }
}

fun Record.toCreator() = Creator(
    id = get("id", String::class.java)!!,
    displayName = get("display_name", String::class.java)!!,
    email = get("email", String::class.java)!!,
    balanceCents = get("balance_cents", Long::class.java)!!,
    createdAt = get("created_at", OffsetDateTime::class.java)!!.toString(),
)
