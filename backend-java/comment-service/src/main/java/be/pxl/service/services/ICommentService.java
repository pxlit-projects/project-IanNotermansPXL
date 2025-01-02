package be.pxl.service.services;

import be.pxl.service.domain.dto.request.CommentRequest;
import be.pxl.service.domain.dto.response.CommentResponse;

import java.util.List;

public interface ICommentService {
    CommentResponse addComment(CommentRequest commentRequest, String user, String role);
    List<CommentResponse> getCommentsByPostId(Long postId, String user, String role);
    void deleteComment(Long commentId, String user);
    CommentResponse updateComment(Long commentId, CommentRequest commentRequest, String user);
}
