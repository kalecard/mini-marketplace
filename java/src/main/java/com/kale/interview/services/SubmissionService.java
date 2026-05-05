package com.kale.interview.services;

import com.kale.interview.data.Campaign;
import com.kale.interview.data.CampaignState;
import com.kale.interview.data.Submission;
import com.kale.interview.data.SubmissionState;
import com.kale.interview.repositories.CampaignRepository;
import com.kale.interview.repositories.CreatorRepository;
import com.kale.interview.repositories.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final CampaignRepository campaignRepository;
    private final CreatorRepository creatorRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            CampaignRepository campaignRepository,
            CreatorRepository creatorRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.campaignRepository = campaignRepository;
        this.creatorRepository = creatorRepository;
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
}
