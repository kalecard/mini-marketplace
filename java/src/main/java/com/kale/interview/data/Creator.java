package com.kale.interview.data;

public record Creator(
        String id,
        String displayName,
        String email,
        Long balanceCents,
        String createdAt
) {}
