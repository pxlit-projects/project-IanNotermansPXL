package be.pxl.service.controller;

import be.pxl.service.domain.dto.request.ReviewRequest;
import be.pxl.service.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/{id}")
    public ResponseEntity<?> addReview(@PathVariable Long id, @RequestBody ReviewRequest request) {
        reviewService.addReview(id, request);
        return ResponseEntity.ok().build();
    }
}
