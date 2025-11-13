package app.popdratingsvc.web.mapper;

import app.popdratingsvc.model.Rating;
import app.popdratingsvc.web.dto.MovieRatingStatsResponse;
import app.popdratingsvc.web.dto.RatingResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static RatingResponse from(Rating rating) {

        return RatingResponse.builder()
                .value(rating.getValue())
                .movieId(rating.getMovieId())
                .userId(rating.getUserId())
                .createdOn(rating.getCreatedOn())
                .updatedOn(rating.getUpdatedOn())
                .build();
    }

    public static MovieRatingStatsResponse from(Double averageRating, Integer totalRatings) {

        return MovieRatingStatsResponse.builder()
                .averageRating(averageRating)
                .totalRatings(totalRatings)
                .build();
    }

}
