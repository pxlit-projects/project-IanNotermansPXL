package be.pxl.service.controller;

import be.pxl.service.client.PostClient;
import be.pxl.service.domain.dto.request.ReviewRequest;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.repository.ReviewRepository;
import be.pxl.service.services.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Transactional
@ExtendWith(MockitoExtension.class)
public class ReviewControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostClient postClient;

    @Container
    private static final PostgreSQLContainer<?> sqlContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
    }

    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        reviewRequest = new ReviewRequest();
        reviewRequest.setApproved(true);
        reviewRequest.setReviewComment("This is a review.");
    }

    @Test
    void addReview_Returns403_WhenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/review/{id}", 1L)
                        .header("user", "testUser")
                        .header("role", "viewer")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addReview_Returns404_WhenPostNotFound() throws Exception {
        doThrow(new PostNotFoundException("Post not found")).when(postClient).getPostById(1L, "editorUser", "editor");

        mockMvc.perform(post("/api/review/{id}", 1L)
                        .header("user", "editorUser")
                        .header("role", "editor")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addReview_Returns400_WhenUnexpectedExceptionOccurs() throws Exception {
        doThrow(new RuntimeException("Unexpected error")).when(postClient).getPostById(1L, "editorUser", "editor");

        mockMvc.perform(post("/api/review/{id}", 1L)
                        .header("user", "editorUser")
                        .header("role", "editor")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addReview_Returns400_WhenRoleIsMissing() throws Exception {
        mockMvc.perform(post("/api/review/{id}", 1L)
                        .header("user", "editorUser")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addReview_Returns400_WhenUserHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/review/{id}", 1L)
                        .header("role", "editor")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addReview_Returns200_WhenReviewIsAdded() throws Exception {
        PostResponse postResponse = new PostResponse();
        postResponse.setId(1L);

        when(postClient.getPostById(1L, "editorUser", "editor")).thenReturn(postResponse);

        mockMvc.perform(post("/api/review/{id}", 1L)
                        .header("user", "editorUser")
                        .header("role", "editor")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk());
    }
}

