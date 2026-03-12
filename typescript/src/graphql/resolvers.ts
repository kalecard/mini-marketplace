import type { CampaignService } from "../services/campaign-service.js";
import type { SubmissionService } from "../services/submission-service.js";

export function createResolvers(
  campaignService: CampaignService,
  submissionService: SubmissionService,
) {
  return {
    Query: {
      campaigns: () => campaignService.getCampaigns(),
      campaign: (_: unknown, { id }: { id: string }) =>
        campaignService.getCampaign(Number(id)),
      submissions: (_: unknown, { campaignId }: { campaignId: string }) =>
        submissionService.getSubmissionsByCampaign(Number(campaignId)),
    },
    Mutation: {
      createCampaign: (
        _: unknown,
        {
          input,
        }: {
          input: {
            brandId: string;
            title: string;
            description?: string;
            payoutCents: number;
            maxSubmissions?: number;
          };
        },
      ) =>
        campaignService.createCampaign(
          Number(input.brandId),
          input.title,
          input.description ?? null,
          input.payoutCents,
          input.maxSubmissions ?? 100,
        ),
      activateCampaign: (_: unknown, { campaignId }: { campaignId: string }) =>
        campaignService.activateCampaign(Number(campaignId)),
      completeCampaign: (_: unknown, { campaignId }: { campaignId: string }) =>
        campaignService.completeCampaign(Number(campaignId)),
      submitContent: (
        _: unknown,
        {
          input,
        }: {
          input: { campaignId: string; creatorId: string; contentUrl: string };
        },
      ) =>
        submissionService.submitContent(
          Number(input.campaignId),
          input.creatorId,
          input.contentUrl,
        ),
      approveSubmission: (_: unknown, { submissionId }: { submissionId: string }) =>
        submissionService.approveSubmission(Number(submissionId)),
      rejectSubmission: (_: unknown, { submissionId }: { submissionId: string }) =>
        submissionService.rejectSubmission(Number(submissionId)),
      processPayment: (_: unknown, { submissionId }: { submissionId: string }) =>
        submissionService.processPayment(Number(submissionId)),
    },
  };
}
