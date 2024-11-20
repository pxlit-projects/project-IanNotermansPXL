package be.pxl.services.services;

import be.pxl.services.domain.Post;
import be.pxl.services.domain.PostStatus;
import be.pxl.services.domain.dto.request.PostRequest;
import be.pxl.services.domain.dto.response.PostResponse;
import be.pxl.services.exceptions.PostNotFoundException;
import be.pxl.services.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService{
    private final PostRepository postRepository;

    @Override
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponse addPost(PostRequest request) {
        Post post = new Post(request.getId(),
                request.getTitle(),
                request.getContent(),
                request.getAuthor(),
                request.getCreatedAt(),
                request.getStatus());
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
    public List<PostResponse> getPostsByStatus(PostStatus status) {
        return postRepository.findByStatus(status).stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    private PostResponse mapPostToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .status(post.getStatus())
                .build();
    }
}
