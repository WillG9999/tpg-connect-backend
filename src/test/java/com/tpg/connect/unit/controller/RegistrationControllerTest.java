package com.tpg.connect.unit.controller;

import com.tpg.connect.user_registration.controller.RegistrationController;
import com.tpg.connect.user_registration.model.entity.request.UserRegistrationRequest;
import com.tpg.connect.user_registration.model.entity.response.UserRegistrationResponse;
import com.tpg.connect.user_registration.service.RegisterUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationControllerTest {

    @Mock private RegisterUserService registerUserService;
    private RegistrationController underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new RegistrationController(registerUserService);
    }

    @Test
    void registerUser_returnsOkWithBearerToken() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "1990-01-01",
                "male",
                "New York"
        );
        UserRegistrationResponse expectedResponse = new UserRegistrationResponse("jwt-token-123");
        when(registerUserService.registerUser(request)).thenReturn(expectedResponse);

        ResponseEntity<UserRegistrationResponse> response = underTest.registerUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token-123", response.getBody().bearer());
        verify(registerUserService, times(1)).registerUser(request);
    }

    @Test
    void registerUser_delegatesToServiceWithCorrectRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "jane@example.com",
                "securePass",
                "Jane",
                "Smith",
                "1985-05-15",
                "female",
                "Los Angeles"
        );
        UserRegistrationResponse expectedResponse = new UserRegistrationResponse("another-token");
        when(registerUserService.registerUser(request)).thenReturn(expectedResponse);

        underTest.registerUser(request);

        verify(registerUserService).registerUser(argThat(req ->
                req.email().equals("jane@example.com") &&
                req.firstName().equals("Jane") &&
                req.lastName().equals("Smith")
        ));
    }

    @Test
    void registerUser_propagatesExceptionFromService() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "fail@example.com",
                "password",
                "Test",
                "User",
                "2000-01-01",
                "other",
                "Chicago"
        );
        when(registerUserService.registerUser(request))
                .thenThrow(new RuntimeException("Registration failed"));

        assertThrows(RuntimeException.class, () -> underTest.registerUser(request));
        verify(registerUserService, times(1)).registerUser(request);
    }
}