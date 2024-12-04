package be.pxl.service.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private String editor;
    @JsonProperty("approved")
    private boolean approved;
}
