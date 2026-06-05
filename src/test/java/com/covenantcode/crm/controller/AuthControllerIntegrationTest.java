package com.covenantcode.crm.controller;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.dto.auth.RegisterRequest;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.UserRepository;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ExtendWith(MockitoExtension.class)
public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    static {
        System.setProperty("com.github.dockerjava.api.version", "1.40");
    }
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(RoleName.MANAGER).isEmpty()) {
            Role role = new Role();
            role.setName(RoleName.MANAGER);
            roleRepository.save(role);
        }
    }

    @Test
    public void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Ivan",
                "Ivanov",
                "ivan@example.com",
                "password123",
                "+79991234567"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("MANAGER"));
        User user = userRepository.findByEmail("ivan@example.com").orElseThrow();

        org.junit.jupiter.api.Assertions.assertNotEquals("password123", user.getPassword());
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("password123", user.getPassword()));
    }

    @Test
    public void register_conflict_sameEmail() throws Exception {
        Role role = roleRepository.findByName(RoleName.MANAGER).orElseThrow();

        User existing = new User();
        existing.setFirstName("Alex");
        existing.setLastName("Stone");
        existing.setEmail("busy@example.com");
        existing.setPassword(passwordEncoder.encode("encoded-pass"));
        existing.setRole(role);
        userRepository.save(existing);

        RegisterRequest request = new RegisterRequest(
                "New",
                "User",
                "busy@example.com",
                "password123",
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.type").value("conflict-error"));
    }

    @Test
    public void register_validationError() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "",
                "Ivanov",
                "not-an-email",
                "password123",
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasItems(
                        "email: Некорректный формат email",
                        "firstName: Имя обязательно"
                )));
    }

}