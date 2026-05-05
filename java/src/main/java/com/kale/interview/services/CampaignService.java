package com.kale.interview.services;

import com.kale.interview.data.Campaign;
import com.kale.interview.data.CampaignState;
import com.kale.interview.repositories.BrandRepository;
import com.kale.interview.repositories.CampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final BrandRepository brandRepository;

    public CampaignService(CampaignRepository campaignRepository, BrandRepository brandRepository) {
        this.campaignRepository = campaignRepository;
        this.brandRepository = brandRepository;
    }

    public List<Campaign> getCampaigns() {
        return campaignRepository.findAll();
    }

    public Campaign getCampaign(long id) {
        return campaignRepository.findById(id);
    }

    @Transactional
    public Campaign createCampaign(long brandId, String title, String description, int payoutCents, int maxSubmissions) {
        if (brandRepository.findById(brandId) == null) {
            throw new IllegalArgumentException("Brand " + brandId + " not found");
        }
        return campaignRepository.create(brandId, title, description, payoutCents, maxSubmissions);
    }

    @Transactional
    public Campaign activateCampaign(long id) {
        Campaign campaign = campaignRepository.findById(id);
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign " + id + " not found");
        }
        if (campaign.state() != CampaignState.DRAFT) {
            throw new IllegalStateException("Campaign must be in DRAFT state to activate");
        }
        return campaignRepository.updateState(id, CampaignState.ACTIVE);
    }
}
