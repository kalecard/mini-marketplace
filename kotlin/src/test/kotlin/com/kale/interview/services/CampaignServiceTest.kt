package com.kale.interview.services

import com.kale.interview.BaseIntegrationTest
import com.kale.interview.data.CampaignState
import com.kale.interview.data.SubmissionState
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CampaignServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var campaignService: CampaignService

    @Autowired
    lateinit var submissionService: SubmissionService

    @Autowired
    lateinit var dsl: DSLContext

    private var brandId: Long = 0

    @BeforeMethod
    fun setUp() {
        dsl.deleteFrom(DSL.table("submissions")).execute()
        dsl.deleteFrom(DSL.table("campaigns")).execute()
        dsl.deleteFrom(DSL.table("creators")).execute()
        dsl.deleteFrom(DSL.table("brands")).execute()

        brandId = dsl.fetchOne(
            "INSERT INTO brands (name, balance_cents) VALUES (?, ?) RETURNING id",
            "Test Brand", 1_000_00L,
        )!!.get("id", Long::class.java)!!

        dsl.execute(
            "INSERT INTO creators (id, display_name, email) VALUES (?, ?, ?)",
            "creator-1", "Test Creator", "creator@test.com",
        )
    }

    @Test
    fun `createCampaign creates a campaign in DRAFT state`() {
        val campaign = campaignService.createCampaign(brandId, "Test Campaign", "A description", 50_00, 50)

        assertNotNull(campaign.id)
        assertEquals("Test Campaign", campaign.title)
        assertEquals("A description", campaign.description)
        assertEquals(50_00, campaign.payoutCents)
        assertEquals(50, campaign.maxSubmissions)
        assertEquals(CampaignState.DRAFT, campaign.state)
    }

    @Test
    fun `createCampaign fails for nonexistent brand`() {
        assertFailsWith<IllegalArgumentException> {
            campaignService.createCampaign(99999, "Bad Campaign", null, 50_00, 50)
        }
    }

    @Test
    fun `activateCampaign transitions from DRAFT to ACTIVE`() {
        val campaign = campaignService.createCampaign(brandId, "Test Campaign", null, 50_00, 50)
        val activated = campaignService.activateCampaign(campaign.id)

        assertEquals(CampaignState.ACTIVE, activated.state)
    }

    @Test
    fun `activateCampaign throws when campaign is already ACTIVE`() {
        val campaign = campaignService.createCampaign(brandId, "Test Campaign", null, 50_00, 50)
        campaignService.activateCampaign(campaign.id)

        assertFailsWith<IllegalStateException> {
            campaignService.activateCampaign(campaign.id)
        }
    }

    @Test
    fun `getCampaigns returns all campaigns`() {
        campaignService.createCampaign(brandId, "Campaign 1", null, 50_00, 50)
        campaignService.createCampaign(brandId, "Campaign 2", null, 100_00, 25)

        val campaigns = campaignService.getCampaigns()
        assertEquals(2, campaigns.size)
    }

    @Test
    fun `submitContent creates a submission for an active campaign`() {
        val campaign = campaignService.createCampaign(brandId, "Test Campaign", null, 50_00, 50)
        campaignService.activateCampaign(campaign.id)

        val submission = submissionService.submitContent(campaign.id, "creator-1", "https://example.com/video.mp4")

        assertNotNull(submission.id)
        assertEquals(campaign.id, submission.campaignId)
        assertEquals("creator-1", submission.creatorId)
        assertEquals(SubmissionState.PENDING, submission.state)
    }

    @Test
    fun `submitContent fails for a DRAFT campaign`() {
        val campaign = campaignService.createCampaign(brandId, "Test Campaign", null, 50_00, 50)

        assertFailsWith<IllegalStateException> {
            submissionService.submitContent(campaign.id, "creator-1", "https://example.com/video.mp4")
        }
    }

    @Test
    fun `approveSubmission transitions from PENDING to APPROVED`() {
        val campaign = campaignService.createCampaign(brandId, "Test Campaign", null, 50_00, 50)
        campaignService.activateCampaign(campaign.id)
        val submission = submissionService.submitContent(campaign.id, "creator-1", "https://example.com/video.mp4")

        val approved = submissionService.approveSubmission(submission.id)

        assertEquals(SubmissionState.APPROVED, approved.state)
        assertNotNull(approved.reviewedAt)
    }

    @Test
    fun `rejectSubmission transitions from PENDING to REJECTED`() {
        val campaign = campaignService.createCampaign(brandId, "Test Campaign", null, 50_00, 50)
        campaignService.activateCampaign(campaign.id)
        val submission = submissionService.submitContent(campaign.id, "creator-1", "https://example.com/video.mp4")

        val rejected = submissionService.rejectSubmission(submission.id)

        assertEquals(SubmissionState.REJECTED, rejected.state)
        assertNotNull(rejected.reviewedAt)
    }
}
