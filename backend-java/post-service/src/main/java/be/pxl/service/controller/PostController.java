package be.pxl.service.controller;

import be.pxl.service.domain.PostStatus;
import be.pxl.service.domain.dto.request.PostRequest;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.services.PostService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        log.info("Getting all posts");
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<PostResponse> addPost(@RequestBody PostRequest request) {
        log.info("Adding new post");
        PostResponse postResponse = postService.addPost(request);
        return ResponseEntity.ok(postResponse);

    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        try {
            log.info("Getting post by id: {}", id);
            PostResponse postResponse = postService.getPostById(id);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            log.info("Post with id: {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/not-published")
    public ResponseEntity<List<PostResponse>> getAllNotPublishedPosts() {
        log.info("Getting all not published posts (Concepts, approved, rejected)");
        List<PostResponse> posts = postService.getAllNotPublishedPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PostResponse>> getPostsByStatus(@PathVariable PostStatus status) {
        log.info("Getting posts by status: {}", status);
        List<PostResponse> posts = postService.getPostsByStatus(status);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody PostRequest request) {
        try {
            log.info("Updating post with id: {}", id);
            PostResponse postResponse = postService.updatePost(id, request);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            log.info("Post to update with id: {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<PostResponse> publishPost(@PathVariable Long id) {
        try {
            log.info("Publishing post with id: {}", id);
            PostResponse postResponse = postService.publishPost(id);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            log.info("Post to publish with id: {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }
}
