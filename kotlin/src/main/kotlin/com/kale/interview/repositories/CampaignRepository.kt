package com.kale.interview.repositories

import com.kale.interview.data.Campaign
import com.kale.interview.data.CampaignState
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class CampaignRepository(private val dsl: DSLContext) {

    fun findById(id: Long): Campaign? {
        return dsl.selectFrom(DSL.table("campaigns"))
            .where(DSL.field("id", Long::class.java).eq(id))
            .fetchOne()
            ?.toCampaign()
    }

    fun findAll(): List<Campaign> {
        return dsl.selectFrom(DSL.table("campaigns"))
            .orderBy(DSL.field("created_at").desc())
            .fetch()
            .map { it.toCampaign() }
    }

    fun create(
        brandId: Long,
        title: String,
        description: String?,
        payoutCents: Int,
        maxSubmissions: Int,
    ): Campaign {
        return dsl.fetchOne(
            """INSERT INTO campaigns (brand_id, title, description, payout_cents, max_submissions, state)
               VALUES (?, ?, ?, ?, ?, ?)
               RETURNING *""",
            brandId, title, description, payoutCents.toLong(), maxSubmissions, CampaignState.DRAFT.name,
        )!!.toCampaign()
    }

    fun updateState(id: Long, state: CampaignState): Campaign {
        dsl.update(DSL.table("campaigns"))
            .set(DSL.field("state", String::class.java), state.name)
            .where(DSL.field("id", Long::class.java).eq(id))
            .execute()
        return findById(id)!!
    }
}

fun Record.toCampaign() = Campaign(
    id = get("id", Long::class.java)!!,
    brandId = get("brand_id", Long::class.java)!!,
    title = get("title", String::class.java)!!,
    description = get("description", String::class.java),
    payoutCents = get("payout_cents", Long::class.java)!!.toInt(),
    maxSubmissions = get("max_submissions", Int::class.java)!!,
    state = CampaignState.valueOf(get("state", String::class.java)!!),
    startsAt = get("starts_at", OffsetDateTime::class.java)?.toString(),
    endsAt = get("ends_at", OffsetDateTime::class.java)?.toString(),
    createdAt = get("created_at", OffsetDateTime::class.java)!!.toString(),
)
