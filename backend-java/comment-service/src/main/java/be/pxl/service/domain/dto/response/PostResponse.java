package be.pxl.service.domain.dto.response;

import be.pxl.service.domain.PostStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private PostStatus status; // CONCEPT, PUBLISHED
    private String reviewComment;
}
