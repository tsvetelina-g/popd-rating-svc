package app.popdratingsvc.web;

import app.popdratingsvc.model.Rating;
import app.popdratingsvc.service.RatingService;
import app.popdratingsvc.web.dto.MovieRatingStatsResponse;
import app.popdratingsvc.web.dto.RatingRequest;
import app.popdratingsvc.web.dto.RatingResponse;
import app.popdratingsvc.web.dto.UserRatingStatsResponse;
import app.popdratingsvc.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/ratings")
    public ResponseEntity<RatingResponse> upsertRating(@RequestBody RatingRequest ratingRequest) {

        Rating rating = ratingService.upsert(ratingRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DtoMapper.from(rating));
    }

    @GetMapping("/ratings/{userId}/{movieId}")
    public ResponseEntity<RatingResponse> getRatingByUserAndMovie(@PathVariable UUID userId, @PathVariable UUID movieId) {

        Rating rating = ratingService.findByUserIdAndMovieId(userId, movieId);

        if (rating == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DtoMapper.from(rating));
    }

    @DeleteMapping("/ratings/{userId}/{movieId}")
    public ResponseEntity<Void> deleteRating(@PathVariable UUID userId, @PathVariable UUID movieId) {

        Boolean isDeleted = ratingService.removeBy(userId, movieId);

        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ratings/{movieId}")
    public ResponseEntity<MovieRatingStatsResponse> movieRatingStats(@PathVariable UUID movieId) {

        Double averageRating = ratingService.getAverageRatingForAMovie(movieId);
        Integer allRatingsCount = ratingService.getAllRatingForAMovieCount(movieId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DtoMapper.from(averageRating, allRatingsCount));
    }

    @GetMapping("/ratings/{userId}/user")
    public ResponseEntity<UserRatingStatsResponse> userRatingStats(@PathVariable UUID userId) {

        Integer moviesRatedCount = ratingService.getAllRatedMoviesCountByUser(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DtoMapper.from(moviesRatedCount));
    }
}
