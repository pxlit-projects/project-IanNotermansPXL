package be.pxl.service.controller;

import be.pxl.service.client.PostClient;
import be.pxl.service.domain.Comment;
import be.pxl.service.domain.dto.request.CommentRequest;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.CommentNotFoundException;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
@Testcontainers
public class CommentControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PostClient postClient; // Mock external dependency

    @Container
    private static final PostgreSQLContainer<?> sqlContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll(); // Clear database before tests
        Comment comment = Comment.builder()
                .postId(1L)
                .text("Sample Comment")
                .commenter("user1")
                .addedAt(LocalDateTime.now())
                .build();
        commentRepository.save(comment); // Seed with sample data
    }

    @Test
    void getCommentsByPostId_ReturnsComments_WhenAuthorizedAndPostExists() throws Exception {
        Long postId = 1L;
        String user = "testUser";
        String role = "user";

        PostResponse postResponse = new PostResponse();
        postResponse.setId(postId);
        postResponse.setTitle("Post Title");
        postResponse.setContent("Post Content");

        when(postClient.getPostByIdWithoutComments(postId, user, role)).thenReturn(postResponse);

        mockMvc.perform(get("/api/comments/{postId}", postId)
                        .header("user", user)
                        .header("role", role))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Sample Comment"));
    }

    @Test
    void getCommentsByPostId_Returns403_WhenRoleIsInvalid() throws Exception {
        Long postId = 1L;

        mockMvc.perform(get("/api/comments/{postId}", postId)
                        .header("user", "testUser")
                        .header("role", "invalidRole"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_SuccessfullyDeletesComment_WhenAuthorizedAndOwner() throws Exception {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("user", "user1")
                        .header("role", "user"))
                .andExpect(status().isNoContent());

        assertTrue(commentRepository.findById(commentId).isEmpty());
    }

    @Test
    void deleteComment_Returns403_WhenNotOwner() throws Exception {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("user", "otherUser")
                        .header("role", "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addComment_SuccessfullyAddsComment_WhenAuthorizedAndPostExists() throws Exception {
        Long postId = 1L;
        String user = "testUser";
        String role = "user";
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setText("New Comment");

        PostResponse postResponse = new PostResponse();
        postResponse.setId(postId);

        when(postClient.getPostByIdWithoutComments(postId, user, role)).thenReturn(postResponse);

        mockMvc.perform(post("/api/comments")
                        .header("user", user)
                        .header("role", role)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("New Comment"));
    }

    @Test
    void updateComment_SuccessfullyUpdatesComment_WhenAuthorizedAndOwner() throws Exception {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();
        String user = "user1";
        String role = "user";

        CommentRequest request = new CommentRequest();
        request.setText("Updated Comment");
        request.setPostId(1L);

        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .header("user", user)
                        .header("role", role)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated Comment"));
    }

    @Test
    void updateComment_Returns403_WhenNotOwner() throws Exception {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();

        CommentRequest request = new CommentRequest();
        request.setText("Updated Comment");
        request.setPostId(1L);

        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .header("user", "otherUser")
                        .header("role", "user")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCommentsByPostId_HandlesCommentNotFoundException() throws Exception {
        Long postId = 1L;
        String user = "testUser";
        String role = "user";

        when(postClient.getPostByIdWithoutComments(postId, user, role))
                .thenThrow(new CommentNotFoundException("Comments not found"));

        mockMvc.perform(get("/api/comments/{postId}", postId)
                        .header("user", user)
                        .header("role", role))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_Returns404_WhenCommentNotFound() throws Exception {
        Long commentId = 999L; // Non-existent comment ID

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("user", "user1")
                        .header("role", "user"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_Returns403_WhenNotYourCommentExceptionThrown() throws Exception {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("user", "otherUser") // Not the owner
                        .header("role", "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addComment_Returns404_WhenPostNotFoundExceptionThrown() throws Exception {
        Long postId = 1L;
        String user = "user1";
        String role = "user";

        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setText("New Comment");

        when(postClient.getPostByIdWithoutComments(postId, user, role))
                .thenThrow(new PostNotFoundException("Post not found"));

        mockMvc.perform(post("/api/comments")
                        .header("user", user)
                        .header("role", role)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateComment_Returns404_WhenCommentNotFoundExceptionThrown() throws Exception {
        Long commentId = 999L; // Non-existent comment ID
        String user = "user1";
        String role = "user";

        CommentRequest request = new CommentRequest();
        request.setText("Updated Comment");
        request.setPostId(1L);

        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .header("user", user)
                        .header("role", role)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateComment_Returns403_WhenNotYourCommentExceptionThrown() throws Exception {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();

        CommentRequest request = new CommentRequest();
        request.setText("Updated Comment");
        request.setPostId(1L);

        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .header("user", "otherUser") // Not the owner
                        .header("role", "user")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateComment_Returns400_OnGenericException() throws Exception {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();

        CommentRequest request = new CommentRequest();
        request.setText(null); // Invalid request data

        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .header("user", "user1")
                        .header("role", "user")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void getCommentsByPostId_Returns400_OnGeneralException() throws Exception {
        Long postId = 1L;
        String user = "testUser";
        String role = "user";

        when(postClient.getPostByIdWithoutComments(postId, user, role))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/comments/{postId}", postId)
                        .header("user", user)
                        .header("role", role))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_Returns403_ForInvalidRole() throws Exception {
        Long postId = 1L;
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setText("New Comment");

        mockMvc.perform(post("/api/comments")
                        .header("user", "testUser")
                        .header("role", "invalidRole")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addComment_Returns400_OnGeneralException() throws Exception {
        Long postId = 1L;
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setText("New Comment");

        when(postClient.getPostByIdWithoutComments(postId, "user1", "user"))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/comments")
                        .header("user", "user1")
                        .header("role", "user")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_Returns403_ForInvalidRole() throws Exception {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();

        CommentRequest request = new CommentRequest();
        request.setText("Updated Comment");
        request.setPostId(1L);

        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .header("user", "testUser")
                        .header("role", "invalidRole")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCommentsByPostId_Returns404_WhenPostNotFound() throws Exception {
        Long postId = 999L; // Non-existent post ID
        String user = "testUser";
        String role = "user";

        // Mock the PostClient to simulate that the post does not exist
        when(postClient.getPostByIdWithoutComments(postId, user, role))
                .thenThrow(new PostNotFoundException("Post with id: " + postId + " does not exist"));

        // Perform the GET request and verify the response
        mockMvc.perform(get("/api/comments/{postId}", postId)
                        .header("user", user)
                        .header("role", role))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_Returns403_WhenRoleIsInvalid() throws Exception {
        Long commentId = 1L; // Any valid comment ID

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("user", "testUser")
                        .header("role", "invalidRole")) // Invalid role
                .andExpect(status().isForbidden());
    }


}
