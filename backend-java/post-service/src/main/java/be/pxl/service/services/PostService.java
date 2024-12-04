package be.pxl.service.services;

import be.pxl.service.domain.Post;
import be.pxl.service.domain.PostStatus;
import be.pxl.service.domain.Review;
import be.pxl.service.domain.dto.request.PostRequest;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    @Override
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponse addPost(PostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(request.getAuthor());
        post.setCreatedAt(request.getCreatedAt());
        post.setStatus(request.getStatus());

        Post savedPost = postRepository.save(post);
        return mapPostToResponse(savedPost);
    }

    @Override
    public PostResponse getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::mapPostToResponse)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
    }

    @Override
    public List<PostResponse> getAllNotPublishedPosts() {
        return postRepository.findAllExceptPublished()
                .stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponse> getPostsByStatus(PostStatus status) {
        return postRepository.findByStatus(status).stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(request.getAuthor());
        post.setCreatedAt(request.getCreatedAt());
        post.setStatus(PostStatus.CONCEPT);

        Post updatedPost = postRepository.save(post);
        return mapPostToResponse(updatedPost);
    }

    @Override
    public PostResponse publishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (post.getStatus() != PostStatus.APPROVED) {
            throw new RuntimeException("Post is not approved for publication");
        }

        post.setStatus(PostStatus.PUBLISHED);

        Post updatedPost = postRepository.save(post);
        return mapPostToResponse(updatedPost);
    }

    @RabbitListener(queues = "review", containerFactory = "rabbitListenerContainerFactory")
    public void receiveReview(Review review) {
        log.info("Received review for post with id: {}", review.getPostId());
        Post post = postRepository.findById(review.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (post.getStatus() == PostStatus.PUBLISHED) {
            log.error("Post is already published");
            throw new RuntimeException("Post is already published");
        }

        if (review.isApproved()) {
            log.info("Review is approved");
            post.setStatus(PostStatus.APPROVED);
        } else {
            log.info("Review is rejected with comment: {}", review.getReviewComment());
            post.setStatus(PostStatus.REJECTED);
            post.setReviewComment(review.getReviewComment());
        }
        postRepository.save(post);
    }

    private PostResponse mapPostToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .status(post.getStatus())
                .reviewComment(post.getReviewComment())
                .build();
    }
}
