package be.pxl.service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    @Column(length = 10000)
    private String content;
    private String author;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private PostStatus status;
    @Column(nullable = true)
    private String reviewComment;
}
