package be.pxl.services.repository;

import be.pxl.services.domain.Post;
import be.pxl.services.domain.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface PostRepository extends JpaRepository<Post, Long> {
    Collection<Post> findByStatus(PostStatus status);
}

