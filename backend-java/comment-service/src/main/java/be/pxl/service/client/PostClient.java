package be.pxl.service.client;

import be.pxl.service.domain.dto.response.PostResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "post-service")
public interface PostClient {
    @GetMapping("/api/posts/{id}")
    PostResponse getPostById(@PathVariable Long id, @RequestHeader String user, @RequestHeader String role);
}
