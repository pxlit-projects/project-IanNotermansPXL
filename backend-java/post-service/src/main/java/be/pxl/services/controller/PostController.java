package be.pxl.services.controller;

import be.pxl.services.domain.PostStatus;
import be.pxl.services.domain.dto.request.PostRequest;
import be.pxl.services.domain.dto.response.PostResponse;
import be.pxl.services.exceptions.PostNotFoundException;
import be.pxl.services.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<PostResponse> addPost(@RequestBody PostRequest request) {

            PostResponse postResponse = postService.addPost(request);
            return ResponseEntity.ok(postResponse);

    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        try {
            PostResponse postResponse = postService.getPostById(id);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PostResponse>> getPostsByStatus(@PathVariable PostStatus status) {
        List<PostResponse> posts = postService.getPostsByStatus(status);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody PostRequest request) {
        try {
            PostResponse postResponse = postService.updatePost(id, request);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<PostResponse> publishPost(@PathVariable Long id) {
        try {
            PostResponse postResponse = postService.publishPost(id);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
