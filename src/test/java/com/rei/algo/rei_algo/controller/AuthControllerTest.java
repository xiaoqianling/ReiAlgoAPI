package com.rei.algo.rei_algo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.algo.DTO.auth.LoginRequestDTO;
import com.rei.algo.DTO.auth.RegisterRequestDTO;
import com.rei.algo.DTO.user.UserDTO;
import com.rei.algo.controller.AuthController;
import com.rei.algo.security.JwtTokenProvider;
import com.rei.algo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import lombok.extern.slf4j.Slf4j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Slf4j
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    @Autowired
    private WebApplicationContext webApplicationContext;

    // Re-initialize MockMvc before each test to avoid state leakage if needed
    // Or use @Autowired MockMvc directly if setup is simple enough
    @BeforeEach
    void setUp() {
         mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    void registerUser_Success() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("test@example.com");

        UserDTO registeredUser = UserDTO.builder().userId("10000001").username("admin").build();

        when(userService.registerUser(any(RegisterRequestDTO.class))).thenReturn(registeredUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void registerUser_BadRequest_UserExists() throws Exception {
         RegisterRequestDTO registerRequest = new RegisterRequestDTO();
         registerRequest.setUsername("existinguser");
         registerRequest.setPassword("password");
         registerRequest.setEmail("existing@example.com");

         // Simulate user service throwing an exception for existing user
         when(userService.registerUser(any(RegisterRequestDTO.class)))
             .thenThrow(new IllegalArgumentException("Username or email already exists"));

         mockMvc.perform(post("/api/auth/register")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest()); // Assuming GlobalExceptionHandler handles this
                // Add more specific checks if GlobalExceptionHandler returns a specific body
    }

    @Test
    void authenticateUser_Success() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        Authentication authentication = mock(Authentication.class); // Mock the Authentication object
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("mockJwtToken");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("mockJwtToken"));
    }

    @Test
    void authenticateUser_Unauthorized() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("wronguser");
        loginRequest.setPassword("wrongpassword");

        // Simulate AuthenticationManager throwing AuthenticationException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {}); // Provide a simple anonymous class

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Assuming GlobalExceptionHandler handles this
                // Add more specific checks if GlobalExceptionHandler returns a specific body
    }
} 