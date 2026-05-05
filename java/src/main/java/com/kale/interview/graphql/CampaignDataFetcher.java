package com.kale.interview.graphql;

import com.kale.interview.data.Campaign;
import com.kale.interview.services.CampaignService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;

import java.util.List;

@DgsComponent
public class CampaignDataFetcher {

    private final CampaignService campaignService;

    public CampaignDataFetcher(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @DgsQuery
    public List<Campaign> campaigns() {
        return campaignService.getCampaigns();
    }

    @DgsQuery
    public Campaign campaign(@InputArgument String id) {
        return campaignService.getCampaign(Long.parseLong(id));
    }

    @DgsMutation
    public Campaign createCampaign(@InputArgument CreateCampaignInput input) {
        int maxSubmissions = input.maxSubmissions() == null ? 100 : input.maxSubmissions();
        return campaignService.createCampaign(
                Long.parseLong(input.brandId()),
                input.title(),
                input.description(),
                input.payoutCents(),
                maxSubmissions
        );
    }

    @DgsMutation
    public Campaign activateCampaign(@InputArgument String campaignId) {
        return campaignService.activateCampaign(Long.parseLong(campaignId));
    }

    public record CreateCampaignInput(
            String brandId,
            String title,
            String description,
            Integer payoutCents,
            Integer maxSubmissions
    ) {}
}
