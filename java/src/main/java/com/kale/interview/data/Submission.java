package com.kale.interview.data;

public record Submission(
        Long id,
        Long campaignId,
        String creatorId,
        String contentUrl,
        SubmissionState state,
        String reviewedAt,
        String paidAt,
        String createdAt
) {}
