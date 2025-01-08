package be.pxl.service.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTests {

    @Test
    void commentBuilder_CreatesCommentSuccessfully() {
        LocalDateTime now = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(1L)
                .postId(100L)
                .commenter("testUser")
                .text("This is a test comment.")
                .addedAt(now)
                .build();

        assertNotNull(comment);
        assertEquals(1L, comment.getId());
        assertEquals(100L, comment.getPostId());
        assertEquals("testUser", comment.getCommenter());
        assertEquals("This is a test comment.", comment.getText());
        assertEquals(now, comment.getAddedAt());
    }

    @Test
    void noArgsConstructor_CreatesCommentWithNullValues() {
        Comment comment = new Comment();

        assertNotNull(comment);
        assertNull(comment.getId());
        assertNull(comment.getPostId());
        assertNull(comment.getCommenter());
        assertNull(comment.getText());
        assertNull(comment.getAddedAt());
    }

    @Test
    void allArgsConstructor_CreatesCommentSuccessfully() {
        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment(1L, 100L, "testUser", "This is a test comment.", now);

        assertNotNull(comment);
        assertEquals(1L, comment.getId());
        assertEquals(100L, comment.getPostId());
        assertEquals("testUser", comment.getCommenter());
        assertEquals("This is a test comment.", comment.getText());
        assertEquals(now, comment.getAddedAt());
    }

    @Test
    void settersAndGetters_WorkAsExpected() {
        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setPostId(100L);
        comment.setCommenter("testUser");
        comment.setText("This is a test comment.");
        comment.setAddedAt(now);

        assertEquals(1L, comment.getId());
        assertEquals(100L, comment.getPostId());
        assertEquals("testUser", comment.getCommenter());
        assertEquals("This is a test comment.", comment.getText());
        assertEquals(now, comment.getAddedAt());
    }

    @Test
    void equalsAndHashCode_WorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        Comment comment1 = Comment.builder()
                .id(1L)
                .postId(100L)
                .commenter("testUser")
                .text("This is a test comment.")
                .addedAt(now)
                .build();

        Comment comment2 = Comment.builder()
                .id(1L)
                .postId(100L)
                .commenter("testUser")
                .text("This is a test comment.")
                .addedAt(now)
                .build();

        assertEquals(comment1, comment2);
        assertEquals(comment1.hashCode(), comment2.hashCode());
    }

    @Test
    void toString_ReturnsNonNullString() {
        LocalDateTime now = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(1L)
                .postId(100L)
                .commenter("testUser")
                .text("This is a test comment.")
                .addedAt(now)
                .build();

        String commentString = comment.toString();

        assertNotNull(commentString);
        assertTrue(commentString.contains("testUser"));
        assertTrue(commentString.contains("This is a test comment."));
    }
}
