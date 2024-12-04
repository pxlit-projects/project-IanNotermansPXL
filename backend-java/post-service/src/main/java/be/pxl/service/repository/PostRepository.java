package be.pxl.service.repository;

import be.pxl.service.domain.Post;
import be.pxl.service.domain.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Collection<Post> findByStatus(PostStatus status);
    @Query("SELECT p FROM Post p WHERE p.status <> 'PUBLISHED'")
    List<Post> findAllExceptPublished();
}

