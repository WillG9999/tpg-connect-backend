package com.tpg.connect.safety.service;

import com.tpg.connect.conversation.repository.ConversationRepository;
import com.tpg.connect.safety.model.entity.Block;
import com.tpg.connect.safety.model.entity.Report;
import com.tpg.connect.safety.model.request.ReportUserRequest;
import com.tpg.connect.safety.model.response.SafetyActionResponse;
import com.tpg.connect.safety.repository.BlockRepository;
import com.tpg.connect.safety.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyService {

    private final ReportRepository reportRepository;
    private final BlockRepository blockRepository;
    private final ConversationRepository conversationRepository;

    public SafetyActionResponse reportUser(long reporterConnectId, long reportedConnectId, ReportUserRequest request) {
        log.info("User {} reporting user {}", reporterConnectId, reportedConnectId);

        Report report = Report.builder()
                .reporterConnectId(reporterConnectId)
                .reportedConnectId(reportedConnectId)
                .conversationId(request.conversationId())
                .reason(request.reason())
                .details(request.details())
                .status(Report.ReportStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        Report saved = reportRepository.save(report);
        return new SafetyActionResponse(true, saved.reportId(), "User reported successfully");
    }

    public SafetyActionResponse blockUser(long blockerConnectId, long blockedConnectId) {
        log.info("User {} blocking user {}", blockerConnectId, blockedConnectId);

        if (blockRepository.isBlocked(blockerConnectId, blockedConnectId)) {
            return new SafetyActionResponse(true, null, "User already blocked");
        }

        Block block = Block.builder()
                .blockerConnectId(blockerConnectId)
                .blockedConnectId(blockedConnectId)
                .createdAt(Instant.now())
                .build();

        Block saved = blockRepository.save(block);
        return new SafetyActionResponse(true, saved.blockId(), "User blocked successfully");
    }

    public SafetyActionResponse unblockUser(long blockerConnectId, long blockedConnectId) {
        log.info("User {} unblocking user {}", blockerConnectId, blockedConnectId);

        boolean deleted = blockRepository.delete(blockerConnectId, blockedConnectId);
        if (deleted) {
            return new SafetyActionResponse(true, null, "User unblocked successfully");
        }
        return new SafetyActionResponse(false, null, "Failed to unblock user");
    }

    public SafetyActionResponse unmatch(long connectId, String conversationId) {
        log.info("User {} unmatching conversation {}", connectId, conversationId);

        boolean deleted = conversationRepository.delete(conversationId);
        if (deleted) {
            return new SafetyActionResponse(true, conversationId, "Unmatched successfully");
        }
        return new SafetyActionResponse(false, null, "Failed to unmatch");
    }

    public List<Long> getBlockedUserIds(long connectId) {
        return blockRepository.findBlockedUserIds(connectId);
    }

    public Set<Long> getAllBlockRelatedUserIds(long connectId) {
        Set<Long> blockedIds = new HashSet<>(blockRepository.findBlockedUserIds(connectId));
        blockedIds.addAll(blockRepository.findBlockedByUserIds(connectId));
        return blockedIds;
    }

    public boolean isUserBlocked(long blockerConnectId, long blockedConnectId) {
        return blockRepository.isBlocked(blockerConnectId, blockedConnectId);
    }

    public boolean isEitherUserBlocked(long userId1, long userId2) {
        return blockRepository.isBlocked(userId1, userId2) || blockRepository.isBlocked(userId2, userId1);
    }

    public List<Report> getPendingReports() {
        return reportRepository.findPendingReports();
    }

    public List<Report> getAllReports() {
        return reportRepository.findAllReports();
    }

    public boolean resolveReport(String reportId, Report.ReportStatus status, String adminNotes) {
        return reportRepository.updateStatus(reportId, status, adminNotes);
    }
}
