package be.pxl.service.repository;

import be.pxl.service.domain.Post;
import be.pxl.service.domain.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface PostRepository extends JpaRepository<Post, Long> {
    Collection<Post> findByStatus(PostStatus status);
}

