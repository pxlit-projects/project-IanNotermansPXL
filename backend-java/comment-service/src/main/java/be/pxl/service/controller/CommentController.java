package be.pxl.service.controller;

import be.pxl.service.domain.dto.request.CommentRequest;
import be.pxl.service.domain.dto.response.CommentResponse;
import be.pxl.service.exceptions.CommentNotFoundException;
import be.pxl.service.exceptions.NotYourCommentException;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.services.CommentService;
import jakarta.ws.rs.NotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized get comments", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Getting comments by post id: {}", postId);
            List<CommentResponse> comments = commentService.getCommentsByPostId(postId, user, role);
            return ResponseEntity.ok(comments);
        } catch (CommentNotFoundException e) {
            log.info("Comments not found for post id: {}", postId);
            return ResponseEntity.notFound().build();
        } catch (PostNotFoundException e) {
            log.info("Post with id: {} not found", postId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("An error occurred while getting comments by post id: {}", postId);
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized to delete comment with id: {}", user, commentId);
                return ResponseEntity.status(403).build();
            }
            log.info("Deleting comment with id: {}", commentId);
            commentService.deleteComment(commentId, user);
            return ResponseEntity.noContent().build();
        } catch (CommentNotFoundException e) {
            log.info("Comment with id: {} not found", commentId);
            return ResponseEntity.notFound().build();
        } catch (NotYourCommentException e) {
            log.info(String.valueOf(e));
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("An error occurred while deleting comment with id: {}", commentId);
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@RequestBody CommentRequest request, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized to add a comment", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Adding new comment");
            CommentResponse commentResponse = commentService.addComment(request, user, role);
            return ResponseEntity.ok(commentResponse);
        } catch (PostNotFoundException e) {
            log.info("Post with id: {} not found", request.getPostId());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("An error occurred while adding new comment");
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long commentId, @RequestBody CommentRequest request, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized to update a comment", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Updating comment with id: {}", commentId);
            CommentResponse commentResponse = commentService.updateComment(commentId, request, user);
            return ResponseEntity.ok(commentResponse);
        } catch (CommentNotFoundException e) {
            log.info("Comment to update with id: {} not found", commentId);
            return ResponseEntity.notFound().build();
        } catch (NotYourCommentException e) {
            log.info(String.valueOf(e));
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("An error occurred while updating comment with id: {}", commentId);
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }
}
