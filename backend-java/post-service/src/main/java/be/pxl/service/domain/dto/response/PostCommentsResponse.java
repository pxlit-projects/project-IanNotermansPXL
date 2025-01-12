package be.pxl.service.domain.dto.response;

import be.pxl.service.domain.PostStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentsResponse {
    private Long id;
    private String title;
    private String content;
    private String author;
    @Enumerated(EnumType.STRING)
    private PostStatus status;
    private LocalDateTime createdAt;
    private String reviewComment;
    private List<CommentResponse> commentResponseList;
}
