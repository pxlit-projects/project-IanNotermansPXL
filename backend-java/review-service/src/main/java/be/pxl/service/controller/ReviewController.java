package be.pxl.service.controller;

import be.pxl.service.domain.dto.request.ReviewRequest;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.services.IReviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final IReviewService reviewService;
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);


    @PostMapping("/{id}")
    public ResponseEntity<?> addReview(@PathVariable Long id, @RequestBody ReviewRequest request, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("editor")) {
                log.info("User: {} is not authorized to add a review", user);
                return ResponseEntity.status(403).build();
            }
            reviewService.addReview(id, request, user);
            return ResponseEntity.ok().build();
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
