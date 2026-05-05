package com.kale.interview.data;

public record Campaign(
        Long id,
        Long brandId,
        String title,
        String description,
        Integer payoutCents,
        Integer maxSubmissions,
        CampaignState state,
        String startsAt,
        String endsAt,
        String createdAt
) {}
