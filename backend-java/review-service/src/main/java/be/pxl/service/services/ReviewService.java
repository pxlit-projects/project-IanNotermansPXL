package be.pxl.service.services;

import be.pxl.service.client.PostClient;
import be.pxl.service.domain.Review;
import be.pxl.service.domain.dto.request.ReviewRequest;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.repository.ReviewRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PostClient postClient;

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    @Override
    public void addReview(Long id, ReviewRequest reviewRequest) {
        log.info("Finding post with id: {}", id);
        PostResponse post = postClient.getPostById(id);
        log.info("Post with id: {} found", id);
        if (post == null){
            throw new NotFoundException("Post with id: " + id + " does not exist");
        }

        Review review = reviewRepository.findByPostId(id);
        if (review != null) {
            review.setApproved(reviewRequest.isApproved());
            review.setEditor(reviewRequest.getEditor());
            review.setReviewComment(reviewRequest.getReviewComment());
            reviewRepository.save(review);
        } else {
            review = Review.builder()
                    .editor(reviewRequest.getEditor())
                    .approved(reviewRequest.isApproved())
                    .reviewComment(reviewRequest.getReviewComment())
                    .postId(id)
                    .build();
            reviewRepository.save(review);
        }
        rabbitTemplate.convertAndSend("review", review);
    }
}
