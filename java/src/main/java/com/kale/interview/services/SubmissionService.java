package com.kale.interview.services;

import com.kale.interview.data.Brand;
import com.kale.interview.data.Campaign;
import com.kale.interview.data.CampaignState;
import com.kale.interview.data.Creator;
import com.kale.interview.data.Submission;
import com.kale.interview.data.SubmissionState;
import com.kale.interview.repositories.BrandRepository;
import com.kale.interview.repositories.CampaignRepository;
import com.kale.interview.repositories.CreatorRepository;
import com.kale.interview.repositories.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final CampaignRepository campaignRepository;
    private final CreatorRepository creatorRepository;
    private final BrandRepository brandRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            CampaignRepository campaignRepository,
            CreatorRepository creatorRepository,
            BrandRepository brandRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.campaignRepository = campaignRepository;
        this.creatorRepository = creatorRepository;
        this.brandRepository = brandRepository;
    }

    public List<Submission> getSubmissionsByCampaign(long campaignId) {
        return submissionRepository.findByCampaignId(campaignId);
    }

    public Submission getSubmission(long id) {
        return submissionRepository.findById(id);
    }

    @Transactional
    public Submission submitContent(long campaignId, String creatorId, String contentUrl) {
        Campaign campaign = campaignRepository.findById(campaignId);
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign " + campaignId + " not found");
        }
        if (campaign.state() != CampaignState.ACTIVE) {
            throw new IllegalStateException("Campaign is not active");
        }
        if (creatorRepository.findById(creatorId) == null) {
            throw new IllegalArgumentException("Creator " + creatorId + " not found");
        }
        if (submissionRepository.countByCampaignId(campaignId) >= campaign.maxSubmissions()) {
            throw new IllegalStateException("Campaign has reached maximum submissions");
        }
        return submissionRepository.create(campaignId, creatorId, contentUrl);
    }

    @Transactional
    public Submission approveSubmission(long id) {
        Submission submission = submissionRepository.findById(id);
        if (submission == null) {
            throw new IllegalArgumentException("Submission " + id + " not found");
        }
        if (submission.state() != SubmissionState.PENDING) {
            throw new IllegalStateException("Submission must be in PENDING state to approve");
        }
        return submissionRepository.updateState(id, SubmissionState.APPROVED);
    }

    @Transactional
    public Submission rejectSubmission(long id) {
        Submission submission = submissionRepository.findById(id);
        if (submission == null) {
            throw new IllegalArgumentException("Submission " + id + " not found");
        }
        if (submission.state() != SubmissionState.PENDING) {
            throw new IllegalStateException("Submission must be in PENDING state to reject");
        }
        return submissionRepository.updateState(id, SubmissionState.REJECTED);
    }

    @Transactional
    public Submission processPayment(long id) {
        Submission submission = submissionRepository.findById(id);
        if (submission == null) {
            throw new IllegalArgumentException("Submission " + id + " not found");
        }
        if (submission.state() != SubmissionState.APPROVED) {
            throw new IllegalStateException("Submission must be in APPROVED state to process payment");
        }

        Campaign campaign = campaignRepository.findById(submission.campaignId());
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign " + submission.campaignId() + " not found");
        }

        Brand brand = brandRepository.findById(campaign.brandId());
        if (brand == null) {
            throw new IllegalArgumentException("Brand " + campaign.brandId() + " not found");
        }
        if (brand.balanceCents() < campaign.payoutCents()) {
            throw new IllegalStateException("Insufficient brand balance for payout");
        }

        CompletableFuture.runAsync(() ->
                brandRepository.updateBalance(brand.id(), brand.balanceCents() - campaign.payoutCents())
        );

        Creator creator = creatorRepository.findById(submission.creatorId());
        if (creator == null) {
            throw new IllegalArgumentException("Creator " + submission.creatorId() + " not found");
        }
        creatorRepository.updateBalance(creator.id(), creator.balanceCents() - campaign.payoutCents());

        return submissionRepository.updateState(id, SubmissionState.PAID);
    }
}
