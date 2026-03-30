package com.kale.interview.graphql

import com.kale.interview.data.Submission
import com.kale.interview.services.SubmissionService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument

@DgsComponent
class SubmissionDataFetcher(private val submissionService: SubmissionService) {

    @DgsQuery
    fun submissions(@InputArgument campaignId: String): List<Submission> =
        submissionService.getSubmissionsByCampaign(campaignId.toLong())

    @DgsMutation
    fun submitContent(@InputArgument input: SubmitContentInput): Submission =
        submissionService.submitContent(
            campaignId = input.campaignId.toLong(),
            creatorId = input.creatorId,
            contentUrl = input.contentUrl,
        )

    @DgsMutation
    fun approveSubmission(@InputArgument submissionId: String): Submission =
        submissionService.approveSubmission(submissionId.toLong())

    @DgsMutation
    fun rejectSubmission(@InputArgument submissionId: String): Submission =
        submissionService.rejectSubmission(submissionId.toLong())

    @DgsMutation
    fun processPayment(@InputArgument submissionId: String): Submission =
        submissionService.processPayment(submissionId.toLong())
}

data class SubmitContentInput(
    val campaignId: String,
    val creatorId: String,
    val contentUrl: String,
)
