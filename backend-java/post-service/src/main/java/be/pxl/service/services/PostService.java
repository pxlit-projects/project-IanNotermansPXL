package be.pxl.service.services;

import be.pxl.service.client.CommentClient;
import be.pxl.service.domain.Post;
import be.pxl.service.domain.PostStatus;
import be.pxl.service.domain.Review;
import be.pxl.service.domain.dto.request.PostRequest;
import be.pxl.service.domain.dto.response.CommentResponse;
import be.pxl.service.domain.dto.response.PostCommentsResponse;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.NotYourPostException;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.repository.PostRepository;
import jakarta.ws.rs.NotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private final CommentClient commentClient;

    @Autowired
    private MailService mailService;

    @Override
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        if (posts.isEmpty()) {
            throw new PostNotFoundException("No posts found");
        }
        return posts.stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponse addPost(PostRequest request, String user) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(user);
        post.setCreatedAt(request.getCreatedAt());
        post.setStatus(request.getStatus());

        Post savedPost = postRepository.save(post);
        return mapPostToResponse(savedPost);
    }

    @Override
    public PostCommentsResponse getPostById(Long id, String user, String role) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        List<CommentResponse> comments;
        try {
            comments = commentClient.getCommentsByPostId(post.getId(), user, role);
        } catch (Exception e) {
            log.error("Failed to fetch comments for post with id: {}", post.getId(), e);
            comments = List.of();
        }

        return mapPostToCommentsResponse(post, comments);
    }



    @Override
    public List<PostResponse> getAllNotPublishedPosts() {
        List<Post> posts = postRepository.findAllExceptPublished();
        if (posts.isEmpty()) {
            throw new PostNotFoundException("No posts found");
        }

        return postRepository.findAllExceptPublished()
                .stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostCommentsResponse> getPublishedPosts(String user, String role) {
        Collection<Post> posts = postRepository.findByStatus(PostStatus.PUBLISHED);
        List<PostCommentsResponse> PostsWithComments = posts.stream().map(post -> {
            List<CommentResponse> comments;
            try {
                comments = commentClient.getCommentsByPostId(post.getId(), user, role);
            } catch (Exception e) {
                log.error("Failed to fetch comments for post with id: {}", post.getId(), e);
                comments = List.of();
            }
            return mapPostToCommentsResponse(post, comments);
        }).toList();
        if (PostsWithComments.isEmpty()) {
            throw new PostNotFoundException("There are no published posts");
        }
        return PostsWithComments;
    }

    @Override
    public PostResponse getPostByIdWithoutComments(Long id, String user, String role) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
        return mapPostToResponse(post);
    }

    @Override
    public List<PostResponse> getPostsByStatus(PostStatus status) {
        List <PostResponse> posts = postRepository.findByStatus(status).stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
                if (posts.isEmpty()) {
                    throw new PostNotFoundException("There are no posts with status: " + status);
                }
                return posts;
    }

    @Override
    public PostResponse updatePost(Long id, PostRequest request, String user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (!user.equals(post.getAuthor())) {
            throw new NotYourPostException("You cannot edit a post that is not yours");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
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
            postRepository.save(post);

            mailService.sendMail("Post Approved " + review.getPostId(), "Your post has been approved. by " + review.getEditor());
        } else {
            log.info("Review is rejected with comment: {}", review.getReviewComment());
            post.setStatus(PostStatus.REJECTED);
            post.setReviewComment(review.getReviewComment());
            postRepository.save(post);
            mailService.sendMail("Post Rejected " + post.getId(), "Your post has been rejected. By " + review.getEditor() + " with comment " + review.getReviewComment());
        }
        log.info("Post with ID: {} has a new review", review.getPostId());
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

    private PostCommentsResponse mapPostToCommentsResponse(Post post, List<CommentResponse> comments) {
        return PostCommentsResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .status(post.getStatus())
                .commentResponseList(comments)
                .build();
    }
}
