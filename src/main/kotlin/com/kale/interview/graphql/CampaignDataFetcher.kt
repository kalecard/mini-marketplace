package com.kale.interview.graphql

import com.kale.interview.data.Campaign
import com.kale.interview.services.CampaignService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument

@DgsComponent
class CampaignDataFetcher(private val campaignService: CampaignService) {

    @DgsQuery
    fun campaigns(): List<Campaign> = campaignService.getCampaigns()

    @DgsQuery
    fun campaign(@InputArgument id: String): Campaign? =
        campaignService.getCampaign(id.toLong())

    @DgsMutation
    fun createCampaign(@InputArgument input: CreateCampaignInput): Campaign =
        campaignService.createCampaign(
            brandId = input.brandId.toLong(),
            title = input.title,
            description = input.description,
            payoutCents = input.payoutCents,
            maxSubmissions = input.maxSubmissions ?: 100,
        )

    @DgsMutation
    fun activateCampaign(@InputArgument campaignId: String): Campaign =
        campaignService.activateCampaign(campaignId.toLong())
}

data class CreateCampaignInput(
    val brandId: String,
    val title: String,
    val description: String? = null,
    val payoutCents: Int,
    val maxSubmissions: Int? = null,
)
