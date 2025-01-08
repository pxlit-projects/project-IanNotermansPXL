package be.pxl.service.services;

import be.pxl.service.domain.PostStatus;
import be.pxl.service.domain.dto.request.PostRequest;
import be.pxl.service.domain.dto.response.PostCommentsResponse;
import be.pxl.service.domain.dto.response.PostResponse;

import java.util.List;


public interface IPostService {
    List<PostResponse> getAllPosts();
    PostResponse addPost(PostRequest request, String user);
    PostCommentsResponse getPostById(Long id, String user, String role);
    List<PostResponse> getPostsByStatus(PostStatus status);
    PostResponse updatePost(Long id, PostRequest request, String user);
    PostResponse publishPost(Long id);
    List<PostResponse> getAllNotPublishedPosts();
    List<PostCommentsResponse> getPublishedPosts(String user, String role);
    PostResponse getPostByIdWithoutComments(Long id, String user, String role);
    }
