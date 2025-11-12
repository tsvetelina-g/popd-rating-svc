package app.popdratingsvc.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RatingResponse {

    private int value;

    private UUID userId;

    private UUID movieId;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
