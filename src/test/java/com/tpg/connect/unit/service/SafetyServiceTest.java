package com.tpg.connect.unit.service;

import com.tpg.connect.conversation.repository.ConversationRepository;
import com.tpg.connect.safety.model.entity.Block;
import com.tpg.connect.safety.model.entity.Report;
import com.tpg.connect.safety.model.request.ReportUserRequest;
import com.tpg.connect.safety.model.response.SafetyActionResponse;
import com.tpg.connect.safety.repository.BlockRepository;
import com.tpg.connect.safety.repository.ReportRepository;
import com.tpg.connect.safety.service.SafetyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SafetyServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private ConversationRepository conversationRepository;

    private SafetyService safetyService;

    @BeforeEach
    void setUp() {
        safetyService = new SafetyService(reportRepository, blockRepository, conversationRepository);
    }

    @Test
    void reportUser_createsReportSuccessfully() {
        ReportUserRequest request = new ReportUserRequest(
                Report.ReportReason.HARASSMENT,
                "Test details",
                "conv_123"
        );
        Report savedReport = Report.builder()
                .reportId("report_123")
                .reporterConnectId(12345L)
                .reportedConnectId(67890L)
                .reason(Report.ReportReason.HARASSMENT)
                .details("Test details")
                .conversationId("conv_123")
                .status(Report.ReportStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(reportRepository.save(any())).thenReturn(savedReport);

        SafetyActionResponse response = safetyService.reportUser(12345L, 67890L, request);

        assertTrue(response.success());
        assertEquals("report_123", response.actionId());
        assertEquals("User reported successfully", response.message());
        verify(reportRepository).save(any());
    }

    @Test
    void blockUser_createsBlockSuccessfully() {
        Block savedBlock = Block.builder()
                .blockId("block_123")
                .blockerConnectId(12345L)
                .blockedConnectId(67890L)
                .createdAt(Instant.now())
                .build();

        when(blockRepository.isBlocked(12345L, 67890L)).thenReturn(false);
        when(blockRepository.save(any())).thenReturn(savedBlock);

        SafetyActionResponse response = safetyService.blockUser(12345L, 67890L);

        assertTrue(response.success());
        assertEquals("block_123", response.actionId());
        assertEquals("User blocked successfully", response.message());
        verify(blockRepository).save(any());
    }

    @Test
    void blockUser_alreadyBlockedReturnsSuccess() {
        when(blockRepository.isBlocked(12345L, 67890L)).thenReturn(true);

        SafetyActionResponse response = safetyService.blockUser(12345L, 67890L);

        assertTrue(response.success());
        assertEquals("User already blocked", response.message());
        verify(blockRepository, never()).save(any());
    }

    @Test
    void unblockUser_deletesBlockSuccessfully() {
        when(blockRepository.delete(12345L, 67890L)).thenReturn(true);

        SafetyActionResponse response = safetyService.unblockUser(12345L, 67890L);

        assertTrue(response.success());
        assertEquals("User unblocked successfully", response.message());
        verify(blockRepository).delete(12345L, 67890L);
    }

    @Test
    void unblockUser_failsToDelete() {
        when(blockRepository.delete(12345L, 67890L)).thenReturn(false);

        SafetyActionResponse response = safetyService.unblockUser(12345L, 67890L);

        assertFalse(response.success());
        assertEquals("Failed to unblock user", response.message());
    }

    @Test
    void unmatch_deletesConversationSuccessfully() {
        when(conversationRepository.delete("conv_123")).thenReturn(true);

        SafetyActionResponse response = safetyService.unmatch(12345L, "conv_123");

        assertTrue(response.success());
        assertEquals("conv_123", response.actionId());
        assertEquals("Unmatched successfully", response.message());
    }

    @Test
    void unmatch_failsToDelete() {
        when(conversationRepository.delete("conv_123")).thenReturn(false);

        SafetyActionResponse response = safetyService.unmatch(12345L, "conv_123");

        assertFalse(response.success());
        assertEquals("Failed to unmatch", response.message());
    }

    @Test
    void getBlockedUserIds_returnsList() {
        when(blockRepository.findBlockedUserIds(12345L)).thenReturn(List.of(67890L, 11111L));

        List<Long> blocked = safetyService.getBlockedUserIds(12345L);

        assertEquals(2, blocked.size());
        assertTrue(blocked.contains(67890L));
        assertTrue(blocked.contains(11111L));
    }

    @Test
    void getAllBlockRelatedUserIds_combinesBlockedAndBlockedBy() {
        when(blockRepository.findBlockedUserIds(12345L)).thenReturn(List.of(67890L));
        when(blockRepository.findBlockedByUserIds(12345L)).thenReturn(List.of(11111L));

        Set<Long> allBlocked = safetyService.getAllBlockRelatedUserIds(12345L);

        assertEquals(2, allBlocked.size());
        assertTrue(allBlocked.contains(67890L));
        assertTrue(allBlocked.contains(11111L));
    }

    @Test
    void isUserBlocked_returnsTrueWhenBlocked() {
        when(blockRepository.isBlocked(12345L, 67890L)).thenReturn(true);

        assertTrue(safetyService.isUserBlocked(12345L, 67890L));
    }

    @Test
    void isUserBlocked_returnsFalseWhenNotBlocked() {
        when(blockRepository.isBlocked(12345L, 67890L)).thenReturn(false);

        assertFalse(safetyService.isUserBlocked(12345L, 67890L));
    }

    @Test
    void isEitherUserBlocked_returnsTrueIfFirstBlocked() {
        when(blockRepository.isBlocked(12345L, 67890L)).thenReturn(true);

        assertTrue(safetyService.isEitherUserBlocked(12345L, 67890L));
    }

    @Test
    void isEitherUserBlocked_returnsTrueIfSecondBlocked() {
        when(blockRepository.isBlocked(12345L, 67890L)).thenReturn(false);
        when(blockRepository.isBlocked(67890L, 12345L)).thenReturn(true);

        assertTrue(safetyService.isEitherUserBlocked(12345L, 67890L));
    }

    @Test
    void isEitherUserBlocked_returnsFalseIfNeitherBlocked() {
        when(blockRepository.isBlocked(12345L, 67890L)).thenReturn(false);
        when(blockRepository.isBlocked(67890L, 12345L)).thenReturn(false);

        assertFalse(safetyService.isEitherUserBlocked(12345L, 67890L));
    }

    @Test
    void getPendingReports_returnsList() {
        Report report = Report.builder()
                .reportId("report_1")
                .status(Report.ReportStatus.PENDING)
                .build();
        when(reportRepository.findPendingReports()).thenReturn(List.of(report));

        List<Report> reports = safetyService.getPendingReports();

        assertEquals(1, reports.size());
        assertEquals("report_1", reports.get(0).reportId());
    }

    @Test
    void resolveReport_updatesStatus() {
        when(reportRepository.updateStatus("report_1", Report.ReportStatus.RESOLVED, "Handled"))
                .thenReturn(true);

        boolean result = safetyService.resolveReport("report_1", Report.ReportStatus.RESOLVED, "Handled");

        assertTrue(result);
    }
}
