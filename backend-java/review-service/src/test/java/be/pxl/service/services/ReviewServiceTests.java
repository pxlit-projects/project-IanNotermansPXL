package be.pxl.service.services;

import be.pxl.service.client.PostClient;
import be.pxl.service.domain.Review;
import be.pxl.service.domain.dto.request.ReviewRequest;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReviewServiceTests {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @MockBean
    private PostClient postClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

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
        reviewRepository.deleteAll(); // Clear the database before each test
        reviewRequest = new ReviewRequest();
        reviewRequest.setApproved(true);
        reviewRequest.setReviewComment("This is a review.");
    }

    @Test
    void addReview_SuccessfullyAddsReview_WhenPostExists() {
        Long postId = 1L;
        String user = "editorUser";

        // Simulate an existing post from PostClient
        PostResponse postResponse = new PostResponse();
        postResponse.setId(postId);
        when(postClient.getPostById(postId, user, "editor")).thenReturn(postResponse);

        // Perform the service call
        reviewService.addReview(postId, reviewRequest, user);

        // Verify the review is saved in the database
        Review savedReview = reviewRepository.findByPostId(postId);
        assertEquals(postId, savedReview.getPostId());
        assertEquals("This is a review.", savedReview.getReviewComment());
        assertEquals(true, savedReview.isApproved());

        // Verify RabbitTemplate was called
        verify(rabbitTemplate, times(1)).convertAndSend(eq("review"), any(Review.class));
    }

    @Test
    void addReview_UpdatesExistingReview_WhenReviewExists() {
        Long postId = 1L;
        String user = "editorUser";

        // Simulate an existing post from PostClient
        PostResponse postResponse = new PostResponse();
        postResponse.setId(postId);
        when(postClient.getPostById(postId, user, "editor")).thenReturn(postResponse);

        // Save an existing review in the database
        Review existingReview = Review.builder()
                .postId(postId)
                .editor(user)
                .approved(false)
                .reviewComment("Old review comment")
                .build();
        reviewRepository.save(existingReview);

        // Perform the service call
        reviewService.addReview(postId, reviewRequest, user);

        // Verify the review is updated in the database
        Review updatedReview = reviewRepository.findByPostId(postId);
        assertEquals(postId, updatedReview.getPostId());
        assertEquals("This is a review.", updatedReview.getReviewComment());
        assertEquals(true, updatedReview.isApproved());

        // Verify RabbitTemplate was called
        verify(rabbitTemplate, times(1)).convertAndSend(eq("review"), any(Review.class));
    }

    @Test
    void addReview_ThrowsPostNotFoundException_WhenPostDoesNotExist() {
        Long postId = 1L;
        String user = "editorUser";

        // Simulate a null response from PostClient
        when(postClient.getPostById(postId, user, "editor")).thenReturn(null);

        // Expect PostNotFoundException to be thrown
        assertThrows(PostNotFoundException.class, () -> reviewService.addReview(postId, reviewRequest, user));

        // Verify no review is saved in the database
        assertEquals(0, reviewRepository.count());

        // Verify RabbitTemplate was not called
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Review.class));
    }
}
