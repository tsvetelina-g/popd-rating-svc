package app.popdratingsvc.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RatingResponse {

    private int rating;

    private UUID userId;

    private UUID movieId;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
