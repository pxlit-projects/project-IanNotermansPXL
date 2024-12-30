package be.pxl.service.controller;

import be.pxl.service.domain.dto.request.ReviewRequest;
import be.pxl.service.services.ReviewService;
import jakarta.ws.rs.NotFoundException;
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
        try {
            reviewService.addReview(id, request);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
