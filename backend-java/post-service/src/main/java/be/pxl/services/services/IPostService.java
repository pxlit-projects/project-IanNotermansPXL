package be.pxl.services.services;

import be.pxl.services.domain.PostStatus;
import be.pxl.services.domain.dto.request.PostRequest;
import be.pxl.services.domain.dto.response.PostResponse;

import java.util.List;


public interface IPostService {
    List<PostResponse> getAllPosts();
    PostResponse addPost(PostRequest request);
    PostResponse getPostById(Long id);
    List<PostResponse> getPostsByStatus(PostStatus status);
}
