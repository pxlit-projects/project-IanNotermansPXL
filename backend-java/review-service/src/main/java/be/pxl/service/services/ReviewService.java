package be.pxl.service.services;

import be.pxl.service.domain.Review;
import be.pxl.service.domain.dto.request.ReviewRequest;
import be.pxl.service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void addReview(Long id, ReviewRequest reviewRequest) {
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
