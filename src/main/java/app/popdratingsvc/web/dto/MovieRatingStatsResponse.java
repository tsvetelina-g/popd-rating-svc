package app.popdratingsvc.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MovieRatingStatsResponse {

    private Double averageRating;
    private Integer totalRatings;
}
