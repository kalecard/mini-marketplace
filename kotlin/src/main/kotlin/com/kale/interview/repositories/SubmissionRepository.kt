package com.kale.interview.repositories

import com.kale.interview.data.Submission
import com.kale.interview.data.SubmissionState
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class SubmissionRepository(private val dsl: DSLContext) {

    fun findById(id: Long): Submission? {
        return dsl.selectFrom(DSL.table("submissions"))
            .where(DSL.field("id", Long::class.java).eq(id))
            .fetchOne()
            ?.toSubmission()
    }

    fun findByCampaignId(campaignId: Long): List<Submission> {
        return dsl.selectFrom(DSL.table("submissions"))
            .where(DSL.field("campaign_id", Long::class.java).eq(campaignId))
            .orderBy(DSL.field("created_at").desc())
            .fetch()
            .map { it.toSubmission() }
    }

    fun findByCampaignIdAndState(campaignId: Long, state: String): List<Submission> {
        return dsl.fetch(
            "SELECT * FROM submissions WHERE campaign_id = ? AND state = '${state}' ORDER BY created_at DESC",
            campaignId,
        ).map { it.toSubmission() }
    }

    fun countByCampaignId(campaignId: Long): Int {
        return dsl.selectCount()
            .from(DSL.table("submissions"))
            .where(DSL.field("campaign_id", Long::class.java).eq(campaignId))
            .fetchOne(0, Int::class.java) ?: 0
    }

    fun create(campaignId: Long, creatorId: String, contentUrl: String): Submission {
        return dsl.fetchOne(
            """INSERT INTO submissions (campaign_id, creator_id, content_url, state)
               VALUES (?, ?, ?, ?)
               RETURNING *""",
            campaignId, creatorId, contentUrl, SubmissionState.PENDING.name,
        )!!.toSubmission()
    }

    fun updateState(id: Long, state: SubmissionState): Submission {
        when (state) {
            SubmissionState.APPROVED, SubmissionState.REJECTED -> {
                dsl.update(DSL.table("submissions"))
                    .set(DSL.field("state", String::class.java), state.name)
                    .set(DSL.field("reviewed_at", OffsetDateTime::class.java), OffsetDateTime.now())
                    .where(DSL.field("id", Long::class.java).eq(id))
                    .execute()
            }
            SubmissionState.PAID -> {
                dsl.update(DSL.table("submissions"))
                    .set(DSL.field("state", String::class.java), state.name)
                    .set(DSL.field("paid_at", OffsetDateTime::class.java), OffsetDateTime.now())
                    .where(DSL.field("id", Long::class.java).eq(id))
                    .execute()
            }
            else -> {
                dsl.update(DSL.table("submissions"))
                    .set(DSL.field("state", String::class.java), state.name)
                    .where(DSL.field("id", Long::class.java).eq(id))
                    .execute()
            }
        }
        return findById(id)!!
    }
}

fun Record.toSubmission() = Submission(
    id = get("id", Long::class.java)!!,
    campaignId = get("campaign_id", Long::class.java)!!,
    creatorId = get("creator_id", String::class.java)!!,
    contentUrl = get("content_url", String::class.java)!!,
    state = SubmissionState.valueOf(get("state", String::class.java)!!),
    reviewedAt = get("reviewed_at", OffsetDateTime::class.java)?.toString(),
    paidAt = get("paid_at", OffsetDateTime::class.java)?.toString(),
    createdAt = get("created_at", OffsetDateTime::class.java)!!.toString(),
)
