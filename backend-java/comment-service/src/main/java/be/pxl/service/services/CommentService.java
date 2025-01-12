package be.pxl.service.services;

import be.pxl.service.client.PostClient;
import be.pxl.service.domain.Comment;
import be.pxl.service.domain.dto.request.CommentRequest;
import be.pxl.service.domain.dto.response.CommentResponse;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.exceptions.CommentNotFoundException;
import be.pxl.service.exceptions.NotYourCommentException;
import be.pxl.service.exceptions.PostNotFoundException;
import be.pxl.service.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {
    private final CommentRepository commentRepository;
    private final PostClient postClient;

    public List<CommentResponse> getCommentsByPostId(Long postId, String user, String role){
        PostResponse post = postClient.getPostByIdWithoutComments(postId, user, role);
        if (post == null){
            throw new PostNotFoundException("Post with id: " + postId + " does not exist");
        }

        List<CommentResponse> comments = commentRepository.findByPostId(postId).stream().map(comment -> CommentResponse.builder()
                 .id(comment.getId())
                 .postId(comment.getPostId())
                 .text(comment.getText())
                 .commenter(comment.getCommenter())
                 .addedAt(comment.getAddedAt())
                 .build()).collect(Collectors.toList());

        if (comments.isEmpty()) {
            throw new CommentNotFoundException("Comments not found for post id: " + postId);
        }

        return comments;
    }

    public CommentResponse addComment(CommentRequest request, String user, String role){
        if (request.getText() == null || request.getText().isEmpty()){
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        if(request.getPostId() == null){
            throw new IllegalArgumentException("Post id cannot be null");
        }
        if(user == null || user.isEmpty()){
            throw new IllegalArgumentException("User cannot be empty");
        }
        if(role == null || role.isEmpty()){
            throw new IllegalArgumentException("Role cannot be empty");
        }

        PostResponse post = postClient.getPostByIdWithoutComments(request.getPostId(), user, role);
        if (post == null){
            throw new PostNotFoundException("Post with id: " + request.getPostId() + " does not exist");
        }


        Comment comment = Comment.builder()
                .postId(request.getPostId())
                .text(request.getText())
                .commenter(user)
                .addedAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);
       return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .text(comment.getText())
                .commenter(comment.getCommenter())
                .addedAt(comment.getAddedAt())
                .build();
    }

    public void deleteComment(Long id, String user){
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("Comment not found for id: " + id));
        if (!user.equals(comment.getCommenter())){
            throw new NotYourCommentException("You cannot delete a comment that is not yours");
        }
        commentRepository.deleteById(id);
    }

    public CommentResponse updateComment(Long id, CommentRequest request, String user){
        if (request.getText() == null || request.getText().isEmpty()){
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        if(user == null || user.isEmpty()){
            throw new IllegalArgumentException("User cannot be empty");
        }

        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("Comment not found for id: " + id));

        if (!user.equals(comment.getCommenter())){
           throw new NotYourCommentException("You cannot edit a comment that is not yours");
        }

        comment.setText(request.getText());
        commentRepository.save(comment);
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .text(comment.getText())
                .commenter(comment.getCommenter())
                .addedAt(comment.getAddedAt())
                .build();
    }
}
