package com.kale.interview.graphql;

import com.kale.interview.data.Submission;
import com.kale.interview.services.SubmissionService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;

import java.util.List;

@DgsComponent
public class SubmissionDataFetcher {

    private final SubmissionService submissionService;

    public SubmissionDataFetcher(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @DgsQuery
    public List<Submission> submissions(@InputArgument String campaignId) {
        return submissionService.getSubmissionsByCampaign(Long.parseLong(campaignId));
    }

    @DgsMutation
    public Submission submitContent(@InputArgument SubmitContentInput input) {
        return submissionService.submitContent(
                Long.parseLong(input.campaignId()),
                input.creatorId(),
                input.contentUrl()
        );
    }

    @DgsMutation
    public Submission approveSubmission(@InputArgument String submissionId) {
        return submissionService.approveSubmission(Long.parseLong(submissionId));
    }

    @DgsMutation
    public Submission rejectSubmission(@InputArgument String submissionId) {
        return submissionService.rejectSubmission(Long.parseLong(submissionId));
    }

    public record SubmitContentInput(
            String campaignId,
            String creatorId,
            String contentUrl
    ) {}
}
