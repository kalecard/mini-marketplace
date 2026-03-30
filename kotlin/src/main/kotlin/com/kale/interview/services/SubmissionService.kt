package com.kale.interview.services

import com.kale.interview.data.CampaignState
import com.kale.interview.data.Submission
import com.kale.interview.data.SubmissionState
import com.kale.interview.repositories.BrandRepository
import com.kale.interview.repositories.CampaignRepository
import com.kale.interview.repositories.CreatorRepository
import com.kale.interview.repositories.SubmissionRepository
import com.kale.interview.repositories.toBrand
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture
import javax.sql.DataSource

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val campaignRepository: CampaignRepository,
    private val creatorRepository: CreatorRepository,
    private val brandRepository: BrandRepository,
    private val dataSource: DataSource,
) {

    fun getSubmissionsByCampaign(campaignId: Long): List<Submission> =
        submissionRepository.findByCampaignId(campaignId)

    fun getSubmission(id: Long): Submission? = submissionRepository.findById(id)

    @Transactional
    fun submitContent(campaignId: Long, creatorId: String, contentUrl: String): Submission {
        val campaign = campaignRepository.findById(campaignId)
            ?: throw IllegalArgumentException("Campaign $campaignId not found")
        if (campaign.state != CampaignState.ACTIVE) {
            throw IllegalStateException("Campaign is not active")
        }
        creatorRepository.findById(creatorId)
            ?: throw IllegalArgumentException("Creator $creatorId not found")

        val submissionCount = submissionRepository.countByCampaignId(campaignId)
        if (submissionCount >= campaign.maxSubmissions) {
            throw IllegalStateException("Campaign has reached maximum submissions")
        }

        return submissionRepository.create(campaignId, creatorId, contentUrl)
    }

    @Transactional
    fun approveSubmission(id: Long): Submission {
        val submission = submissionRepository.findById(id)
            ?: throw IllegalArgumentException("Submission $id not found")
        if (submission.state != SubmissionState.PENDING) {
            throw IllegalStateException("Submission must be in PENDING state to approve")
        }
        return submissionRepository.updateState(id, SubmissionState.APPROVED)
    }

    @Transactional
    fun rejectSubmission(id: Long): Submission {
        val submission = submissionRepository.findById(id)
            ?: throw IllegalArgumentException("Submission $id not found")
        if (submission.state != SubmissionState.PENDING) {
            throw IllegalStateException("Submission must be in PENDING state to reject")
        }
        return submissionRepository.updateState(id, SubmissionState.REJECTED)
    }

    @Transactional
    fun processPayment(id: Long): Submission {
        val submission = submissionRepository.findById(id)
            ?: throw IllegalArgumentException("Submission $id not found")
        if (submission.state != SubmissionState.APPROVED) {
            throw IllegalStateException("Submission must be in APPROVED state to process payment")
        }

        val campaign = campaignRepository.findById(submission.campaignId)
            ?: throw IllegalArgumentException("Campaign ${submission.campaignId} not found")

        val brandDsl = DSL.using(dataSource, SQLDialect.POSTGRES)
        val brand = brandDsl.selectFrom(DSL.table("brands"))
            .where(DSL.field("id", Long::class.java).eq(campaign.brandId))
            .fetchOne()?.toBrand()
            ?: throw IllegalArgumentException("Brand ${campaign.brandId} not found")

        if (brand.balanceCents < campaign.payoutCents) {
            throw IllegalStateException("Insufficient brand balance for payout")
        }

        CompletableFuture.runAsync {
            brandDsl.update(DSL.table("brands"))
                .set(DSL.field("balance_cents", Long::class.java), brand.balanceCents - campaign.payoutCents)
                .where(DSL.field("id", Long::class.java).eq(brand.id))
                .execute()
        }

        val creator = creatorRepository.findById(submission.creatorId)
            ?: throw IllegalArgumentException("Creator ${submission.creatorId} not found")
        creatorRepository.updateBalance(creator.id, creator.balanceCents - campaign.payoutCents)

        return submissionRepository.updateState(id, SubmissionState.PAID)
    }
}
