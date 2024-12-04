package be.pxl.service.repository;

import be.pxl.service.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository  extends JpaRepository<Review, Long> {
    Review findByPostId(Long postId);
}
