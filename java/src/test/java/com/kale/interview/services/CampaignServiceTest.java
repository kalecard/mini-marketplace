package com.kale.interview.services;

import com.kale.interview.BaseIntegrationTest;
import com.kale.interview.data.Campaign;
import com.kale.interview.data.CampaignState;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CampaignServiceTest extends BaseIntegrationTest {

    @Autowired CampaignService campaignService;
    @Autowired DSLContext dsl;

    long brandId;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(DSL.table("submissions")).execute();
        dsl.deleteFrom(DSL.table("campaigns")).execute();
        dsl.deleteFrom(DSL.table("creators")).execute();
        dsl.deleteFrom(DSL.table("brands")).execute();

        brandId = dsl.fetchOne(
                "INSERT INTO brands (name, balance_cents) VALUES (?, ?) RETURNING id",
                "Test Brand", 100_000L
        ).get("id", Long.class);

        dsl.execute(
                "INSERT INTO creators (id, display_name, email) VALUES (?, ?, ?)",
                "creator-1", "Test Creator", "creator@test.com"
        );
    }

    @Test
    void createCampaign_createsCampaignInDraftState() {
        Campaign campaign = campaignService.createCampaign(
                brandId, "Test Campaign", "A description", 5_000, 50);

        assertNotNull(campaign.id());
        assertEquals("Test Campaign", campaign.title());
        assertEquals("A description", campaign.description());
        assertEquals(5_000, campaign.payoutCents());
        assertEquals(50, campaign.maxSubmissions());
        assertEquals(CampaignState.DRAFT, campaign.state());
    }

    @Test
    void createCampaign_failsForNonexistentBrand() {
        assertThrows(IllegalArgumentException.class, () ->
                campaignService.createCampaign(99_999L, "Bad Campaign", null, 5_000, 50));
    }

    @Test
    void activateCampaign_transitionsFromDraftToActive() {
        Campaign campaign = campaignService.createCampaign(brandId, "Test Campaign", null, 5_000, 50);
        Campaign activated = campaignService.activateCampaign(campaign.id());

        assertEquals(CampaignState.ACTIVE, activated.state());
    }

    @Test
    void activateCampaign_throwsWhenAlreadyActive() {
        Campaign campaign = campaignService.createCampaign(brandId, "Test Campaign", null, 5_000, 50);
        campaignService.activateCampaign(campaign.id());

        assertThrows(IllegalStateException.class, () ->
                campaignService.activateCampaign(campaign.id()));
    }

    @Test
    void getCampaigns_returnsAllCampaigns() {
        campaignService.createCampaign(brandId, "Campaign 1", null, 5_000, 50);
        campaignService.createCampaign(brandId, "Campaign 2", null, 10_000, 25);

        List<Campaign> campaigns = campaignService.getCampaigns();
        assertEquals(2, campaigns.size());
    }
}
