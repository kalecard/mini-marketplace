package com.kale.interview.data

data class Brand(
    val id: Long,
    val name: String,
    val balanceCents: Long,
    val createdAt: String,
)

data class Creator(
    val id: String,
    val displayName: String,
    val email: String,
    val balanceCents: Long,
    val createdAt: String,
)

data class Campaign(
    val id: Long,
    val brandId: Long,
    val title: String,
    val description: String?,
    val payoutCents: Int,
    val maxSubmissions: Int,
    val state: CampaignState,
    val startsAt: String?,
    val endsAt: String?,
    val createdAt: String,
)

data class Submission(
    val id: Long,
    val campaignId: Long,
    val creatorId: String,
    val contentUrl: String,
    val state: SubmissionState,
    val reviewedAt: String?,
    val paidAt: String?,
    val createdAt: String,
)
