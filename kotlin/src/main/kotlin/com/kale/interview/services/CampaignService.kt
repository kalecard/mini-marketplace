package com.kale.interview.services

import com.kale.interview.data.Campaign
import com.kale.interview.data.CampaignState
import com.kale.interview.repositories.BrandRepository
import com.kale.interview.repositories.CampaignRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CampaignService(
    private val campaignRepository: CampaignRepository,
    private val brandRepository: BrandRepository,
) {

    fun getCampaigns(): List<Campaign> = campaignRepository.findAll()

    fun getCampaign(id: Long): Campaign? = campaignRepository.findById(id)

    @Transactional
    fun createCampaign(
        brandId: Long,
        title: String,
        description: String?,
        payoutCents: Int,
        maxSubmissions: Int,
    ): Campaign {
        brandRepository.findById(brandId)
            ?: throw IllegalArgumentException("Brand $brandId not found")
        return campaignRepository.create(brandId, title, description, payoutCents, maxSubmissions)
    }

    @Transactional
    fun activateCampaign(id: Long): Campaign {
        val campaign = campaignRepository.findById(id)
            ?: throw IllegalArgumentException("Campaign $id not found")
        if (campaign.state != CampaignState.DRAFT) {
            throw IllegalStateException("Campaign must be in DRAFT state to activate")
        }
        return campaignRepository.updateState(id, CampaignState.ACTIVE)
    }

    @Transactional
    fun completeCampaign(id: Long): Campaign {
        val campaign = campaignRepository.findById(id)
            ?: throw IllegalArgumentException("Campaign $id not found")
        if (campaign.state != CampaignState.DRAFT) {
            throw IllegalStateException("Campaign must be in ACTIVE state to complete")
        }
        return campaignRepository.updateState(id, CampaignState.COMPLETED)
    }
}
