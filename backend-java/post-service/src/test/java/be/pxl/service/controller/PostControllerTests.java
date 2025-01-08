package be.pxl.service.controller;

import be.pxl.service.client.CommentClient;
import be.pxl.service.domain.Post;
import be.pxl.service.domain.PostStatus;
import be.pxl.service.domain.dto.request.PostRequest;
import be.pxl.service.domain.dto.response.PostResponse;
import be.pxl.service.repository.PostRepository;
import be.pxl.service.services.MailService;
import be.pxl.service.services.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Transactional
@ExtendWith(MockitoExtension.class)
public class PostControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostService postService;
    @Mock
    private CommentClient commentClient;
    @MockBean
    private JavaMailSender javaMailSender;
    @MockBean
    private MailService mailService;

    @Autowired
    private ObjectMapper objectMapper;

    private Post testPost;
    private PostResponse samplePostResponse;

    @Container
    private static final PostgreSQLContainer<?> sqlContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        samplePostResponse = PostResponse.builder()
                .id(1L)
                .title("Sample Post")
                .content("This is a sample post.")
                .author("testUser")
                .status(PostStatus.PUBLISHED)
                .build();

        Post post = new Post();
        post.setTitle("Sample Post");
        post.setContent("This is a sample post.");
        post.setAuthor("testUser");
        post.setStatus(PostStatus.PUBLISHED);

        testPost = new Post();
        testPost.setTitle("Sample Post");
        testPost.setContent("This is a sample post.");
        testPost.setAuthor("testUser");
        testPost.setStatus(PostStatus.CONCEPT);
    }

    @Test
    void getPublishedPosts_ReturnsPosts_WhenAuthorized() throws Exception {
        testPost.setStatus(PostStatus.PUBLISHED);
        postRepository.save(testPost);

        mockMvc.perform(get("/api/posts/publishedPosts")
                        .header("user", "testUser")
                        .header("role", "editor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample Post"));
    }

    @Test
    void addPost_SuccessfullyAddsPost_WhenAuthorized() throws Exception {
        PostRequest postRequest = PostRequest.builder()
                .title("New Post")
                .content("Post content")
                .build();

        mockMvc.perform(post("/api/posts")
                        .header("user", "editorUser")
                        .header("role", "editor")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void addPost_Returns403_WhenUnauthorized() throws Exception {
        PostRequest postRequest = PostRequest.builder()
                .title("New Post")
                .content("Post content")
                .build();

        mockMvc.perform(post("/api/posts")
                        .header("user", "testUser")
                        .header("role", "user")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPostById_ReturnsPost_WhenFound() throws Exception {
        Post post = new Post();
        post.setTitle("Sample Post");
        post.setContent("This is a sample post.");
        post.setAuthor("testUser");
        post.setStatus(PostStatus.PUBLISHED);
        post = postRepository.save(post);

        mockMvc.perform(get("/api/posts/{id}", post.getId())
                        .header("user", "testUser")
                        .header("role", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Post"));
    }

    @Test
    void getPostById_Returns404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 999L)
                        .header("user", "testUser")
                        .header("role", "user"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePost_ReturnsPost_WhenSuccessful() throws Exception {
        Post post = new Post();
        post.setTitle("Old Title");
        post.setContent("Old Content");
        post.setAuthor("editorUser");
        post.setStatus(PostStatus.CONCEPT);
        post = postRepository.save(post);

        PostRequest postRequest = PostRequest.builder()
                .title("Updated Title")
                .content("Updated Content")
                .build();

        mockMvc.perform(put("/api/posts/{id}", post.getId())
                        .header("user", "editorUser")
                        .header("role", "editor")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updatePost_Returns404_WhenPostNotFound() throws Exception {
        PostRequest postRequest = PostRequest.builder()
                .title("Updated Title")
                .content("Updated Content")
                .build();

        mockMvc.perform(put("/api/posts/{id}", 999L)
                        .header("user", "editorUser")
                        .header("role", "editor")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPosts_Returns403_WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header("user", "testUser")
                        .header("role", "invalidRole"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPosts_ReturnsPosts_WhenAuthorized() throws Exception {
        postRepository.save(testPost);
        mockMvc.perform(get("/api/posts")
                        .header("user", "testUser")
                        .header("role", "editor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void publishPost_Returns403_WhenUnauthorized() throws Exception {
        mockMvc.perform(put("/api/posts/1/publish")
                        .header("user", "testUser")
                        .header("role", "invalidRole"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publishPost_Returns404_WhenPostNotFound() throws Exception {
        mockMvc.perform(put("/api/posts/999/publish")
                        .header("user", "editorUser")
                        .header("role", "editor"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishPost_ReturnsPost_WhenSuccessful() throws Exception {
        Post post = new Post();
        post.setTitle("To Publish");
        post.setContent("Content to publish");
        post.setAuthor("editorUser");
        post.setStatus(PostStatus.APPROVED);
        post = postRepository.save(post);

        mockMvc.perform(put("/api/posts/{id}/publish", post.getId())
                        .header("user", "editorUser")
                        .header("role", "editor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void getPostsByStatus_ReturnsPosts_WhenFound() throws Exception {
        testPost.setStatus(PostStatus.PUBLISHED);
        postRepository.save(testPost);

        mockMvc.perform(get("/api/posts/status/PUBLISHED")
                        .header("user", "testUser")
                        .header("role", "user"))
                .andExpect(status().isOk());
    }

    @Test
    void getPostsByStatus_Returns404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/status/REJECTED")
                        .header("user", "testUser")
                        .header("role", "user"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllNotPublishedPosts_Returns403_WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts/not-published")
                        .header("user", "testUser")
                        .header("role", "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllNotPublishedPosts_ReturnsPosts_WhenAuthorized() throws Exception {
        postRepository.save(testPost);
        mockMvc.perform(get("/api/posts/not-published")
                        .header("user", "editorUser")
                        .header("role", "editor"))
                .andExpect(status().isOk());
    }

    @Test
    void getPostByIdWithoutComments_Returns403_WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts/1/without-comments")
                        .header("user", "testUser")
                        .header("role", "invalidRole"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePost_Returns403_WhenUnauthorized() throws Exception {
        mockMvc.perform(put("/api/posts/1")
                        .header("user", "testUser")
                        .header("role", "invalidRole")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addPost_Returns400_WhenMissingRequestBody() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .header("user", "editorUser")
                        .header("role", "editor")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPostById_ThrowsPostNotFoundException_WhenPostDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/posts/999")
                        .header("user", "editorUser")
                        .header("role", "editor"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPostByIdWithoutComments_ReturnsPost_WhenFound() throws Exception {
        Post post = new Post();
        post.setTitle("No Comments Post");
        post.setContent("Content");
        post.setAuthor("editorUser");
        postRepository.save(post);

        mockMvc.perform(get("/api/posts/{id}/without-comments", post.getId())
                        .header("user", "editorUser")
                        .header("role", "editor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("No Comments Post"));
    }

    @Test
    void updatePost_ThrowsNotYourPostException_WhenUserNotOwner() throws Exception {
        Post post = new Post();
        post.setTitle("Unauthorized Update");
        post.setContent("Content");
        post.setAuthor("anotherUser");
        postRepository.save(post);

        PostRequest updateRequest = new PostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated Content");

        mockMvc.perform(put("/api/posts/{id}", post.getId())
                        .header("user", "editorUser")
                        .header("role", "editor")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void publishPost_ThrowsPostNotFoundException_WhenPostDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/posts/999/publish")
                        .header("user", "editorUser")
                        .header("role", "editor"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublishedPosts_ThrowsMissingHeaderException_WhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/posts/publishedPosts"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllPosts_Returns403_WhenRoleIsInvalid() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header("user", "testUser")
                        .header("role", "invalidRole"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPosts_Returns200_WithPosts() throws Exception {
        postRepository.save(testPost);
        mockMvc.perform(get("/api/posts")
                        .header("user", "testUser")
                        .header("role", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample Post"));
    }

    @Test
    void updatePost_Returns403_WhenUnauthorizedUserTriesToUpdate() throws Exception {
        PostRequest postRequest = new PostRequest("Updated Title", "Updated Content", "editorUser", LocalDateTime.now() ,PostStatus.CONCEPT); ;
        postRepository.save(testPost);

        mockMvc.perform(put("/api/posts/{id}", testPost.getId())
                        .header("user", "notTheAuthor")
                        .header("role", "editor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void publishPost_SuccessfullyPublishesPost() throws Exception {
        testPost.setStatus(PostStatus.APPROVED);
        testPost = postRepository.save(testPost);

        mockMvc.perform(put("/api/posts/{id}/publish", testPost.getId())
                        .header("user", "editorUser")
                        .header("role", "editor"))
                .andExpect(status().isOk());

        assertEquals(PostStatus.PUBLISHED, postRepository.findById(testPost.getId()).get().getStatus());
    }

    @Test
    void getAllNotPublishedPosts_Returns404_WhenNoPostsFound() throws Exception {
        postRepository.deleteAll();

        mockMvc.perform(get("/api/posts/not-published")
                        .header("user", "editorUser")
                        .header("role", "editor"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPostByIdWithoutComments_Returns403_WhenUnauthorizedRole() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/without-comments", 1L)
                        .header("user", "unauthorizedUser")
                        .header("role", "guest"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPostByIdWithoutComments_Returns404_WhenPostNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/without-comments", 999L) // Assuming 999L does not exist
                        .header("user", "authorizedUser")
                        .header("role", "editor"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPostByIdWithoutComments_ReturnsPost_WhenAuthorized() throws Exception {
        testPost.setId(1L);
        postRepository.save(testPost);



        mockMvc.perform(get("/api/posts/{id}/without-comments", 1L)
                        .header("user", "authorizedUser")
                        .header("role", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)); // Adjust based on mock repository setup
    }

    @Test
    void getPostById_Returns403_WhenUnauthorizedRole() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 1L)
                        .header("user", "unauthorizedUser")
                        .header("role", "guest"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPostById_Returns404_WhenPostNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 999L) // Assuming 999L does not exist
                        .header("user", "authorizedUser")
                        .header("role", "editor"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPostById_ReturnsPost_WhenAuthorized() throws Exception {
        testPost.setId(1L);
        postRepository.save(testPost);



        mockMvc.perform(get("/api/posts/{id}", 1L)
                        .header("user", "authorizedUser")
                        .header("role", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)); // Adjust based on mock repository setup
    }

    @Test
    void getPublishedPosts_Returns403_WhenRoleIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts/publishedPosts")
                        .header("user", "testUser")
                        .header("role", "guest")) // Unauthorized role
                .andExpect(status().isForbidden());
    }

    @Test
    void getPublishedPosts_Returns200_WhenRoleIsUser() throws Exception {
        mockMvc.perform(get("/api/posts/publishedPosts")
                        .header("user", "testUser")
                        .header("role", "user")) // Authorized role
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // Adjust based on actual data
    }

    @Test
    void getPublishedPosts_Returns200_WhenRoleIsEditor() throws Exception {
        mockMvc.perform(get("/api/posts/publishedPosts")
                        .header("user", "testEditor")
                        .header("role", "editor")) // Authorized role
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // Adjust based on actual data
    }

    @Test
    void getPublishedPosts_Returns400_WhenHeadersAreMissing() throws Exception {
        mockMvc.perform(get("/api/posts/publishedPosts"))
                .andExpect(status().isBadRequest()); // Missing headers
    }

    @Test
    void getPostsByStatus_Returns403_WhenRoleIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts/status/PUBLISHED")
                        .header("user", "testUser")
                        .header("role", "guest")) // Unauthorized role
                .andExpect(status().isForbidden());
    }

    @Test
    void getPostsByStatus_Returns200_WhenRoleIsUser() throws Exception {
        mockMvc.perform(get("/api/posts/status/PUBLISHED")
                        .header("user", "testUser")
                        .header("role", "user")) // Authorized role
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // Adjust based on actual data
    }

    @Test
    void getPostsByStatus_Returns200_WhenRoleIsEditor() throws Exception {
        mockMvc.perform(get("/api/posts/status/CONCEPT")
                        .header("user", "testEditor")
                        .header("role", "editor")) // Authorized role
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // Adjust based on actual data
    }

    @Test
    void getPostsByStatus_Returns404_WhenNoPostsFound() throws Exception {
        mockMvc.perform(get("/api/posts/status/APPROVED")
                        .header("user", "testEditor")
                        .header("role", "editor")) // Status with no posts
                .andExpect(status().isNotFound());
    }

    @Test
    void getPostsByStatus_Returns400_WhenHeadersAreMissing() throws Exception {
        mockMvc.perform(get("/api/posts/status/PUBLISHED"))
                .andExpect(status().isBadRequest()); // Missing headers
    }



}
