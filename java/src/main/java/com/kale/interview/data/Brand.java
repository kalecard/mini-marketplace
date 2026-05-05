package com.kale.interview.data;

public record Brand(
        Long id,
        String name,
        Long balanceCents,
        String createdAt
) {}
