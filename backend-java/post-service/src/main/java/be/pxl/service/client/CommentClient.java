package be.pxl.service.client;

import be.pxl.service.domain.dto.response.CommentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "comment-service")
public interface CommentClient {
    @GetMapping("/api/comments/{postId}")
    List<CommentResponse> getCommentsByPostId(@PathVariable Long postId, @RequestHeader  String user, @RequestHeader String role) ;
}
