package be.pxl.service.controller;

import be.pxl.service.domain.PostStatus;
import be.pxl.service.domain.dto.request.PostRequest;
import be.pxl.service.domain.dto.response.PostCommentsResponse;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.NotYourPostException;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.services.PostService;
import jakarta.ws.rs.NotAuthorizedException;
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
    public ResponseEntity<List<PostResponse>> getAllPosts(@RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized get Posts", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Getting all posts");
            List<PostResponse> posts = postService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/publishedPosts")
    public ResponseEntity<List<PostCommentsResponse>> getPublishedPosts(@RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized get published posts", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Getting all published posts");
            List<PostCommentsResponse> posts = postService.getPublishedPosts(user, role);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<PostResponse> addPost(@RequestBody PostRequest request, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("editor")) {
                log.info("User: {} is not authorized add a post", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Adding new post");
            PostResponse postResponse = postService.addPost(request, user);
            return ResponseEntity.ok(postResponse);
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostCommentsResponse> getPostById(@PathVariable Long id, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized get this post", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Getting post by id: {}", id);
            PostCommentsResponse postResponse = postService.getPostById(id, user, role);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            log.info("Post with id: {} not found", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/without-comments")
    public ResponseEntity<PostResponse> getPostByIdWithoutComments(@PathVariable Long id, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized get this post", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Getting post by id: {}", id);
            PostResponse postResponse = postService.getPostByIdWithoutComments(id, user, role);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            log.info("Post with id: {} not found", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/not-published")
    public ResponseEntity<List<PostResponse>> getAllNotPublishedPosts(@RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("editor")) {
                log.info("User: {} is not authorized get not published posts", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Getting all not published posts (Concepts, approved, rejected)");
            List<PostResponse> posts = postService.getAllNotPublishedPosts();
            return ResponseEntity.ok(posts);
        } catch (PostNotFoundException e) {
            log.info("No not published posts found");
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PostResponse>> getPostsByStatus(@PathVariable PostStatus status , @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("user") && !role.equals("editor")) {
                log.info("User: {} is not authorized get these posts", user);
                return ResponseEntity.status(403).build();
            }
        log.info("Getting posts by status: {}", status);
        List<PostResponse> posts = postService.getPostsByStatus(status);
        return ResponseEntity.ok(posts);
        } catch (PostNotFoundException e) {
            log.info("No posts found with status: {}", status);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody PostRequest request, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("editor")) {
                log.info("User: {} is not authorized to update this post", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Updating post with id: {}", id);
            PostResponse postResponse = postService.updatePost(id, request, user);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            log.info("Post to update with id: {} not found", id);
            return ResponseEntity.notFound().build();
        } catch (NotYourPostException e) {
            log.info(String.valueOf(e));
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<PostResponse> publishPost(@PathVariable Long id, @RequestHeader String user, @RequestHeader String role) {
        try {
            if (!role.equals("editor")) {
                log.info("User: {} is not authorized to publish this post", user);
                return ResponseEntity.status(403).build();
            }
            log.info("Publishing post with id: {}", id);
            PostResponse postResponse = postService.publishPost(id);
            return ResponseEntity.ok(postResponse);
        } catch (PostNotFoundException e) {
            log.info("Post to publish with id: {} not found", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.badRequest().build();
        }
    }
}
