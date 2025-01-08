package be.pxl.service.services;

import be.pxl.service.client.CommentClient;
import be.pxl.service.domain.Post;
import be.pxl.service.domain.PostStatus;
import be.pxl.service.domain.Review;
import be.pxl.service.domain.dto.request.PostRequest;
import be.pxl.service.domain.dto.response.CommentResponse;
import be.pxl.service.domain.dto.response.PostCommentsResponse;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.NotYourPostException;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.repository.PostRepository;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest()
@Testcontainers
@AutoConfigureMockMvc
@Transactional
@ExtendWith(MockitoExtension.class)
public class PostServiceTests {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostService postService;
    @MockBean
    private CommentClient commentClient;
    @MockBean
    private JavaMailSender javaMailSender;
    @MockBean
    private MailService mailService;

    @Container
    private static final PostgreSQLContainer<?> sqlContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        Post post = Post.builder()
                .id(1L)
                .title("Sample Post")
                .content("Sample Content")
                .author("testUser")
                .createdAt(LocalDateTime.now())
                .status(PostStatus.CONCEPT)
                .build();
        postRepository.save(post);

        doNothing().when(mailService).sendMail(anyString(), anyString());
    }

    @Test
    void getAllPosts_ReturnsPosts() {
        List<PostResponse> posts = postService.getAllPosts();

        assertNotNull(posts);
        assertEquals(1, posts.size());
        assertEquals("Sample Post", posts.get(0).getTitle());
    }

    @Test
    void addPost_SuccessfullyAddsPost() {
        PostRequest request = new PostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");
        request.setCreatedAt(LocalDateTime.now());
        request.setStatus(PostStatus.CONCEPT);

        PostResponse response = postService.addPost(request, "testUser");

        assertNotNull(response);
        assertEquals("New Post", response.getTitle());
        assertEquals("testUser", response.getAuthor());
    }

    @Test
    void getPostById_ReturnsPostWithComments() {
        Post post = postRepository.findAll().get(0);
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setText("Test Comment");
        commentResponse.setCommenter("testUser");

        when(commentClient.getCommentsByPostId(post.getId(), "testUser", "user"))
                .thenReturn(List.of(commentResponse));

        PostCommentsResponse response = postService.getPostById(post.getId(), "testUser", "user");

        assertNotNull(response);
        assertEquals("Sample Post", response.getTitle());
        assertEquals(1, response.getCommentResponseList().size());
        assertEquals("Test Comment", response.getCommentResponseList().get(0).getText());
    }

    @Test
    void getPostById_ThrowsPostNotFoundException_WhenPostDoesNotExist() {
        assertThrows(PostNotFoundException.class, () -> postService.getPostById(999L, "testUser", "user"));
    }

    @Test
    void getAllNotPublishedPosts_ReturnsPosts() {
        List<PostResponse> posts = postService.getAllNotPublishedPosts();

        assertNotNull(posts);
        assertEquals(1, posts.size());
        assertEquals("Sample Post", posts.get(0).getTitle());
    }

    @Test
    void getPublishedPosts_ThrowsPostNotFoundException_WhenNoPublishedPostsExist() {
        assertThrows(PostNotFoundException.class, () -> postService.getPublishedPosts("testUser", "user"));
    }

    @Test
    void updatePost_SuccessfullyUpdatesPost() {
        Post post = postRepository.findAll().get(0);
        PostRequest request = new PostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setCreatedAt(post.getCreatedAt());
        request.setStatus(PostStatus.CONCEPT);

        PostResponse response = postService.updatePost(post.getId(), request, "testUser");

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
    }

    @Test
    void updatePost_ThrowsPostNotFoundException_WhenPostDoesNotExist() {
        PostRequest request = new PostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");

        assertThrows(PostNotFoundException.class, () -> postService.updatePost(999L, request, "testUser"));
    }

    @Test
    void updatePost_ThrowsNotYourPostException_WhenUserIsNotAuthor() {
        Post post = postRepository.findAll().get(0);
        PostRequest request = new PostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");

        assertThrows(NotYourPostException.class, () -> postService.updatePost(post.getId(), request, "otherUser"));
    }

    @Test
    void publishPost_SuccessfullyPublishesPost() {
        Post post = postRepository.findAll().get(0);
        post.setStatus(PostStatus.APPROVED);
        postRepository.save(post);

        PostResponse response = postService.publishPost(post.getId());

        assertNotNull(response);
        assertEquals(PostStatus.PUBLISHED, response.getStatus());
    }

    @Test
    void publishPost_ThrowsRuntimeException_WhenPostIsNotApproved() {
        Post post = postRepository.findAll().get(0);

        assertThrows(RuntimeException.class, () -> postService.publishPost(post.getId()));
    }

    @Test
    void receiveReview_ApprovesPostSuccessfully() {
        Post post = postRepository.findAll().get(0);
        Review review = new Review();
        review.setPostId(post.getId());
        review.setApproved(true);
        review.setEditor("editorUser");

        postService.receiveReview(review);

        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        assertEquals(PostStatus.APPROVED, updatedPost.getStatus());
        verify(mailService, times(1)).sendMail(
                eq("Post Approved " + post.getId()),
                eq("Your post has been approved. by editorUser")
        );
    }

    @Test
    void receiveReview_RejectsPostSuccessfully() {
        Post post = postRepository.findAll().get(0);
        Review review = new Review();
        review.setPostId(post.getId());
        review.setApproved(false);
        review.setEditor("editorUser");
        review.setReviewComment("Not up to standard");

        postService.receiveReview(review);

        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        assertEquals(PostStatus.REJECTED, updatedPost.getStatus());
        assertEquals("Not up to standard", updatedPost.getReviewComment());
        verify(mailService, times(1)).sendMail(
                eq("Post Rejected " + post.getId()),
                eq("Your post has been rejected. By editorUser with comment Not up to standard")
        );
    }

    @Test
    void getPostByIdWithoutComments_ReturnsPostSuccessfully() {
        Post post = postRepository.findAll().get(0);
        PostResponse response = postService.getPostByIdWithoutComments(post.getId(), "testUser", "user");

        assertNotNull(response);
        assertEquals("Sample Post", response.getTitle());
        assertNull(response.getReviewComment());
    }

    @Test
    void getPostByIdWithoutComments_ThrowsPostNotFoundException_WhenPostDoesNotExist() {
        assertThrows(PostNotFoundException.class, () -> postService.getPostByIdWithoutComments(999L, "testUser", "user"));
    }

    @Test
    void getPostsByStatus_ReturnsPostsSuccessfully() {
        List<PostResponse> responses = postService.getPostsByStatus(PostStatus.CONCEPT);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(PostStatus.CONCEPT, responses.get(0).getStatus());
    }

    @Test
    void getPostsByStatus_ThrowsPostNotFoundException_WhenNoPostsExistForGivenStatus() {
        assertThrows(PostNotFoundException.class, () -> postService.getPostsByStatus(PostStatus.PUBLISHED));
    }

    @Test
    void receiveReview_ThrowsRuntimeException_WhenPostIsAlreadyPublished() {
        Post post = postRepository.findAll().get(0);
        post.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post);

        Review review = new Review();
        review.setPostId(post.getId());
        review.setApproved(true);

        assertThrows(RuntimeException.class, () -> postService.receiveReview(review));
    }

    @Test
    void receiveReview_ThrowsPostNotFoundException_WhenPostDoesNotExist() {
        Review review = new Review();
        review.setPostId(999L);
        review.setApproved(true);

        assertThrows(PostNotFoundException.class, () -> postService.receiveReview(review));
    }
}

