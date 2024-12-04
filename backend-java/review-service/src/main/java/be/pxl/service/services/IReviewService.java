package be.pxl.service.services;

import be.pxl.service.domain.dto.request.ReviewRequest;

public interface IReviewService {
    void addReview(Long id, ReviewRequest reviewRequest);
}
