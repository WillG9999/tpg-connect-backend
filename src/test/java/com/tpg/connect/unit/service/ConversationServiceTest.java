package com.tpg.connect.unit.service;

import com.tpg.connect.conversation.mapper.ConversationMapper;
import com.tpg.connect.conversation.mapper.MessageMapper;
import com.tpg.connect.conversation.model.entity.Conversation;
import com.tpg.connect.conversation.model.entity.Message;
import com.tpg.connect.conversation.model.response.ConversationResponse;
import com.tpg.connect.conversation.model.response.MessageResponse;
import com.tpg.connect.conversation.model.response.PaginatedMessagesResponse;
import com.tpg.connect.conversation.model.response.PaginatedResult;
import com.tpg.connect.conversation.repository.ConversationRepository;
import com.tpg.connect.conversation.repository.MessageRepository;
import com.tpg.connect.conversation.service.ConversationService;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private ConversationService conversationService;

    private Conversation testConversation;
    private Message testMessage;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testConversation = Conversation.builder()
                .conversationId("12345_67890")
                .participants(List.of(12345L, 67890L))
                .createdAt(Instant.now())
                .build();

        testMessage = Message.builder()
                .messageId("msg_123")
                .conversationId("12345_67890")
                .senderId(12345L)
                .content("Hello!")
                .timestamp(Instant.now())
                .build();

        testProfile = UserProfile.builder()
                .connectId(67890L)
                .firstName("Jane")
                .lastName("Doe")
                .photoUrls(List.of("http://example.com/photo.jpg"))
                .build();
    }

    @Test
    void getConversationsForUser_returnsConversations() {
        when(conversationRepository.findByParticipant(12345L)).thenReturn(List.of(testConversation));
        when(profileRepository.findByConnectId(67890L)).thenReturn(Optional.of(testProfile));
        when(messageRepository.countUnreadMessages("12345_67890", 12345L)).thenReturn(2);
        when(conversationMapper.toResponse(any(), eq(12345L), anyString(), anyString(), eq(2)))
                .thenReturn(new ConversationResponse("12345_67890", 67890L, "Jane Doe",
                        "http://example.com/photo.jpg", null, null, null, 2, false));

        List<ConversationResponse> result = conversationService.getConversationsForUser(12345L);

        assertEquals(1, result.size());
        assertEquals("12345_67890", result.get(0).conversationId());
        verify(conversationRepository).findByParticipant(12345L);
    }

    @Test
    void getOrCreateConversation_returnsExisting() {
        when(conversationRepository.findById("12345_67890")).thenReturn(Optional.of(testConversation));

        Conversation result = conversationService.getOrCreateConversation(12345L, 67890L);

        assertEquals("12345_67890", result.conversationId());
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void getOrCreateConversation_createsNew() {
        when(conversationRepository.findById("12345_67890")).thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenReturn(true);

        Conversation result = conversationService.getOrCreateConversation(12345L, 67890L);

        assertEquals("12345_67890", result.conversationId());
        verify(conversationRepository).save(any());
    }

    @Test
    void sendMessage_success() {
        when(conversationRepository.findById("12345_67890")).thenReturn(Optional.of(testConversation));
        when(messageRepository.save(any())).thenReturn(testMessage);
        when(messageMapper.toResponse(testMessage)).thenReturn(
                new MessageResponse("msg_123", 12345L, "Hello!", testMessage.timestamp(), null));

        MessageResponse result = conversationService.sendMessage("12345_67890", 12345L, "Hello!");

        assertEquals("msg_123", result.messageId());
        assertEquals("Hello!", result.content());
        verify(conversationRepository).updateLastMessage(eq("12345_67890"), eq("Hello!"), eq(12345L), anyLong());
    }

    @Test
    void sendMessage_conversationNotFound_throwsException() {
        when(conversationRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                conversationService.sendMessage("nonexistent", 12345L, "Hello!"));
    }

    @Test
    void sendMessage_userNotParticipant_throwsException() {
        when(conversationRepository.findById("12345_67890")).thenReturn(Optional.of(testConversation));

        assertThrows(IllegalArgumentException.class, () ->
                conversationService.sendMessage("12345_67890", 99999L, "Hello!"));
    }

    @Test
    void getMessages_returnsMessages() {
        when(conversationRepository.findById("12345_67890")).thenReturn(Optional.of(testConversation));
        when(messageRepository.findByConversationId("12345_67890", 50, null)).thenReturn(List.of(testMessage));
        when(messageMapper.toResponse(testMessage)).thenReturn(
                new MessageResponse("msg_123", 12345L, "Hello!", testMessage.timestamp(), null));

        List<MessageResponse> result = conversationService.getMessages("12345_67890", 12345L, 50);

        assertEquals(1, result.size());
        assertEquals("msg_123", result.get(0).messageId());
    }

    @Test
    void markAsRead_success() {
        when(conversationRepository.findById("12345_67890")).thenReturn(Optional.of(testConversation));
        when(messageRepository.markMessagesAsRead("12345_67890", 12345L)).thenReturn(true);

        boolean result = conversationService.markAsRead("12345_67890", 12345L);

        assertTrue(result);
        verify(messageRepository).markMessagesAsRead("12345_67890", 12345L);
    }

    @Test
    void getMessagesWithCursor_returnsMessages() {
        PaginatedResult paginatedResult = new PaginatedResult(List.of(testMessage), "1234567890", true);
        when(conversationRepository.findById("12345_67890")).thenReturn(Optional.of(testConversation));
        when(messageRepository.findByConversationIdWithCursor("12345_67890", 50, null)).thenReturn(paginatedResult);
        when(messageMapper.toResponse(testMessage)).thenReturn(
                new MessageResponse("msg_123", 12345L, "Hello!", testMessage.timestamp(), null));

        PaginatedMessagesResponse result = conversationService.getMessagesWithCursor("12345_67890", 12345L, 50, null);

        assertEquals(1, result.messages().size());
        assertEquals("msg_123", result.messages().get(0).messageId());
        assertEquals("1234567890", result.nextCursor());
        assertTrue(result.hasMore());
    }

    @Test
    void getMessagesWithCursor_userNotParticipant_returnsEmpty() {
        when(conversationRepository.findById("12345_67890")).thenReturn(Optional.of(testConversation));

        PaginatedMessagesResponse result = conversationService.getMessagesWithCursor("12345_67890", 99999L, 50, null);

        assertTrue(result.messages().isEmpty());
        assertNull(result.nextCursor());
        assertFalse(result.hasMore());
    }

    @Test
    void generateConversationId_ordersIdsCorrectly() {
        String id1 = Conversation.generateConversationId(12345L, 67890L);
        String id2 = Conversation.generateConversationId(67890L, 12345L);

        assertEquals(id1, id2);
        assertEquals("12345_67890", id1);
    }
}
