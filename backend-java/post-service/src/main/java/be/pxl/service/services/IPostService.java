package be.pxl.service.services;

import be.pxl.service.domain.PostStatus;
import be.pxl.service.domain.dto.request.PostRequest;
import be.pxl.service.domain.dto.response.PostResponse;

import java.util.List;


public interface IPostService {
    List<PostResponse> getAllPosts();
    PostResponse addPost(PostRequest request);
    PostResponse getPostById(Long id);
    List<PostResponse> getPostsByStatus(PostStatus status);
    PostResponse updatePost(Long id, PostRequest request);
    PostResponse publishPost(Long id);
    List<PostResponse> getAllNotPublishedPosts();
    }
