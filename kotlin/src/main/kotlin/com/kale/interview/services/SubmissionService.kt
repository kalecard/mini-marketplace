package com.kale.interview.services

import com.kale.interview.data.CampaignState
import com.kale.interview.data.Submission
import com.kale.interview.data.SubmissionState
import com.kale.interview.repositories.CampaignRepository
import com.kale.interview.repositories.CreatorRepository
import com.kale.interview.repositories.SubmissionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val campaignRepository: CampaignRepository,
    private val creatorRepository: CreatorRepository,
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
}
