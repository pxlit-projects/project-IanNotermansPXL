package be.pxl.service.services;

import be.pxl.service.client.PostClient;
import be.pxl.service.domain.Comment;
import be.pxl.service.domain.dto.request.CommentRequest;
import be.pxl.service.domain.dto.response.CommentResponse;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.CommentNotFoundException;
import be.pxl.service.exceptions.NotYourCommentException;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Transactional
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommentServiceTests {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CommentRepository commentRepository;
    @MockBean
    private PostClient postClient;
    @Autowired
    private CommentService commentService;

    @Container
    private static PostgreSQLContainer sqlContainer = new PostgreSQLContainer(("postgres:16-alpine"));

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
    }
    @BeforeEach
    void setUp() {
        commentRepository.deleteAll(); // Clean up the database before each test
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setPostId(1L);
        comment.setText("Sample Comment");
        comment.setCommenter("user1");
        commentRepository.save(comment); // Save a test comment to the database
    }

    @Test
    void getCommentsByPostId_ReturnsComments_WhenPostExists() {
        Long postId = 1L;
        String user = "testUser";
        String role = "user";

        // Mock the Feign client to return a valid post response
        PostResponse postResponse = new PostResponse();
        postResponse.setId(postId);
        postResponse.setTitle("Post Title");
        postResponse.setContent("Post Content");

        when(postClient.getPostByIdWithoutComments(postId, user, role)).thenReturn(postResponse);

        // Call the service method
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId, user, role);

        // Assertions
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("Sample Comment", comments.get(0).getText());
    }


    @Test
    void getCommentsByPostId_ThrowsPostNotFoundException_WhenPostDoesNotExist() {
        Long postId = 1L;
        String user = "testUser";
        String role = "user";

        // Ensure this stubbing is meaningful
        when(postClient.getPostByIdWithoutComments(postId, user, role)).thenReturn(null);

        // Call the service and verify the exception is thrown
        assertThrows(PostNotFoundException.class, () -> commentService.getCommentsByPostId(postId, user, role));
    }


    @Test
    void getCommentsByPostId_ThrowsCommentNotFoundException_WhenNoCommentsExist() {
        Long postId = 2L; // No comments for this post ID
        String user = "testUser";
        String role = "user";

        PostResponse postResponse = new PostResponse();
        postResponse.setId(postId);

        when(postClient.getPostByIdWithoutComments(postId, user, role)).thenReturn(postResponse);

        assertThrows(CommentNotFoundException.class, () -> commentService.getCommentsByPostId(postId, user, role));
    }

    @Test
    void addComment_SuccessfullyAddsComment_WhenPostExists() {
        Long postId = 1L;
        String user = "testUser";
        String role = "user";
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setText("New Comment");

        PostResponse postResponse = new PostResponse();
        postResponse.setId(postId);

        when(postClient.getPostByIdWithoutComments(postId, user, role)).thenReturn(postResponse);

        CommentResponse response = commentService.addComment(request, user, role);

        assertNotNull(response);
        assertEquals(postId, response.getPostId());
        assertEquals("New Comment", response.getText());
        assertEquals(user, response.getCommenter());
    }

    @Test
    void addComment_ThrowsPostNotFoundException_WhenPostDoesNotExist() {
        Long postId = 1L;
        String user = "testUser";
        String role = "user";
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setText("New Comment");

        when(postClient.getPostByIdWithoutComments(postId, user, role)).thenReturn(null);

        assertThrows(PostNotFoundException.class, () -> commentService.addComment(request, user, role));
    }

    @Test
    void deleteComment_SuccessfullyDeletesComment_WhenCommentExistsAndUserIsOwner() {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();
        String user = comment.getCommenter();

        assertDoesNotThrow(() -> commentService.deleteComment(commentId, user));
        assertTrue(commentRepository.findById(commentId).isEmpty());
    }

    @Test
    void deleteComment_ThrowsCommentNotFoundException_WhenCommentDoesNotExist() {
        Long commentId = 999L;
        String user = "testUser";

        assertThrows(CommentNotFoundException.class, () -> commentService.deleteComment(commentId, user));
    }

    @Test
    void updateComment_SuccessfullyUpdatesComment_WhenCommentExistsAndUserIsOwner() {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();
        String user = comment.getCommenter();
        CommentRequest request = new CommentRequest();
        request.setText("Updated Comment");
        request.setPostId(comment.getPostId());

        CommentResponse response = commentService.updateComment(commentId, request, user);

        assertNotNull(response);
        assertEquals(commentId, response.getId());
        assertEquals("Updated Comment", response.getText());
    }

    @Test
    void updateComment_ThrowsCommentNotFoundException_WhenCommentDoesNotExist() {
        Long commentId = 999L; // Non-existent ID
        String user = "testUser";
        CommentRequest request = new CommentRequest();
        request.setText("Updated Comment");
        request.setPostId(1L);

        assertThrows(CommentNotFoundException.class, () -> commentService.updateComment(commentId, request, user));
    }

    @Test
    void addComment_ThrowsIllegalArgumentException_WhenTextIsEmpty() {
        CommentRequest request = new CommentRequest();
        request.setPostId(1L);
        request.setText("");
        String user = "testUser";
        String role = "user";

        assertThrows(IllegalArgumentException.class, () -> commentService.addComment(request, user, role));
    }

    @Test
    void addComment_ThrowsIllegalArgumentException_WhenPostIdIsNull() {
        CommentRequest request = new CommentRequest();
        request.setPostId(null);
        request.setText("New Comment");
        String user = "testUser";
        String role = "user";

        assertThrows(IllegalArgumentException.class, () -> commentService.addComment(request, user, role));
    }

    @Test
    void addComment_ThrowsIllegalArgumentException_WhenUserIsNull() {
        CommentRequest request = new CommentRequest();
        request.setPostId(1L);
        request.setText("New Comment");
        String user = null;
        String role = "user";

        assertThrows(IllegalArgumentException.class, () -> commentService.addComment(request, user, role));
    }

    @Test
    void addComment_ThrowsIllegalArgumentException_WhenRoleIsNull() {
        CommentRequest request = new CommentRequest();
        request.setPostId(1L);
        request.setText("New Comment");
        String user = "testUser";
        String role = null;

        assertThrows(IllegalArgumentException.class, () -> commentService.addComment(request, user, role));
    }

    @Test
    void updateComment_ThrowsIllegalArgumentException_WhenTextIsEmpty() {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();
        CommentRequest request = new CommentRequest();
        request.setPostId(comment.getPostId());
        request.setText("");
        String user = "user1";

        assertThrows(IllegalArgumentException.class, () -> commentService.updateComment(commentId, request, user));
    }

    @Test
    void updateComment_ThrowsIllegalArgumentException_WhenPostIdIsNull() {
        Comment comment = commentRepository.findAll().get(0);
        Long commentId = comment.getId();
        CommentRequest request = new CommentRequest();
        request.setPostId(null);
        request.setText("Updated Comment");
        String user = "user1";

        assertThrows(IllegalArgumentException.class, () -> commentService.updateComment(commentId, request, user));
    }

    @Test
    void deleteComment_ThrowsNotYourCommentException_WhenUserIsNotOwner() {
        Comment comment = Comment.builder()
                .postId(1L)
                .text("Another Comment")
                .commenter("otherUser")
                .addedAt(LocalDateTime.now())
                .build();
        commentRepository.save(comment);

        assertThrows(NotYourCommentException.class, () -> commentService.deleteComment(comment.getId(), "user1"));
    }

    @Test
    void updateComment_ThrowsNotYourCommentException_WhenUserIsNotOwner() {
        Comment comment = Comment.builder()
                .postId(1L)
                .text("Another Comment")
                .commenter("otherUser")
                .addedAt(LocalDateTime.now())
                .build();
        commentRepository.save(comment);

        CommentRequest request = new CommentRequest();
        request.setPostId(1L);
        request.setText("Updated Comment");

        assertThrows(NotYourCommentException.class, () -> commentService.updateComment(comment.getId(), request, "user1"));
    }
}
