package com.tpg.connect.unit.service;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenProvider;
import com.tpg.connect.user_registration.components.ConnectIdGenerator;
import com.tpg.connect.user_registration.exceptions.UserRegistrationException;
import com.tpg.connect.user_registration.model.dto.BearerTokenDTO;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.model.entity.request.UserRegistrationRequest;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import com.tpg.connect.user_registration.service.RegisterUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterUserServiceTest {

    @Mock
    private ConnectIdGenerator connectIdGenerator;
    @Mock
    private RegisterUserRepository registerUserRepository;
    @Mock
    private JsonWebTokenProvider jsonWebTokenService;

    private RegisterUserService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new RegisterUserService(connectIdGenerator, registerUserRepository, jsonWebTokenService);
    }

    @Test
    void registerUser_returnsResponseWithBearerToken() {
        UserRegistrationRequest request = createRequest();
        when(connectIdGenerator.generateConnectId()).thenReturn(123456L);
        when(registerUserRepository.saveUser(any(RegisteredUser.class))).thenReturn(true);
        when(jsonWebTokenService.generateToken(anyLong(), anyString())).thenReturn("jwt-token");

        BearerTokenDTO response = underTest.registerUser(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.bearer());
    }

    @Test
    void registerUser_generatesTokenWithCorrectParameters() {
        UserRegistrationRequest request = createRequest();
        when(connectIdGenerator.generateConnectId()).thenReturn(999L);
        when(registerUserRepository.saveUser(any(RegisteredUser.class))).thenReturn(true);
        when(jsonWebTokenService.generateToken(anyLong(), anyString())).thenReturn("token");

        underTest.registerUser(request);

        verify(jsonWebTokenService).generateToken(999L, "test@example.com");
    }

    @Test
    void saveUserToFirestore_returnsRegisteredUserOnSuccess() {
        UserRegistrationRequest request = createRequest();
        when(connectIdGenerator.generateConnectId()).thenReturn(12345L);
        when(registerUserRepository.saveUser(any(RegisteredUser.class))).thenReturn(true);

        RegisteredUser result = underTest.saveUserToFirestore(request);

        assertNotNull(result);
        assertEquals(12345L, result.connectId());
        assertEquals("test@example.com", result.email());
        assertEquals("password123", result.password());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("1990-01-01", result.dateOfBirth());
        assertEquals("male", result.gender());
        assertEquals("New York", result.location());
        assertNotNull(result.createdAt());
    }

    @Test
    void saveUserToFirestore_throwsExceptionWhenSaveFails() {
        UserRegistrationRequest request = createRequest();
        when(connectIdGenerator.generateConnectId()).thenReturn(12345L);
        when(registerUserRepository.saveUser(any(RegisteredUser.class))).thenReturn(false);

        UserRegistrationException exception = assertThrows(
                UserRegistrationException.class,
                () -> underTest.saveUserToFirestore(request)
        );

        assertEquals("User Registration failed", exception.getMessage());
    }

    @Test
    void saveUserToFirestore_callsRepositoryWithCorrectUser() {
        UserRegistrationRequest request = createRequest();
        when(connectIdGenerator.generateConnectId()).thenReturn(777L);
        when(registerUserRepository.saveUser(any(RegisteredUser.class))).thenReturn(true);

        underTest.saveUserToFirestore(request);

        ArgumentCaptor<RegisteredUser> captor = ArgumentCaptor.forClass(RegisteredUser.class);
        verify(registerUserRepository).saveUser(captor.capture());

        RegisteredUser savedUser = captor.getValue();
        assertEquals(777L, savedUser.connectId());
        assertEquals("test@example.com", savedUser.email());
        assertEquals("John", savedUser.firstName());
    }

    private UserRegistrationRequest createRequest() {
        return new UserRegistrationRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "1990-01-01",
                "male",
                "New York"
        );
    }
}