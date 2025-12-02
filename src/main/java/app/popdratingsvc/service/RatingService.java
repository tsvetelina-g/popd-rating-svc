package app.popdratingsvc.service;

import app.popdratingsvc.exception.NotFoundException;
import app.popdratingsvc.model.Rating;
import app.popdratingsvc.repository.RatingRepository;
import app.popdratingsvc.web.dto.RatingRequest;
import app.popdratingsvc.web.dto.RatingResponse;
import app.popdratingsvc.web.mapper.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public Rating upsert(RatingRequest ratingRequest) {
        Optional<Rating> ratingOpt = ratingRepository.findByUserIdAndMovieId(ratingRequest.getUserId(), ratingRequest.getMovieId());

        if (ratingOpt.isPresent()) {
            Rating rating = ratingOpt.get();
            rating.setRating(ratingRequest.getRating());
            rating.setUpdatedOn(LocalDateTime.now());
            
            Rating savedRating = ratingRepository.save(rating);
            log.info("Successfully updated rating with id {} for user with id {} and movie with id {}",
                savedRating.getId(), savedRating.getUserId(), savedRating.getMovieId());
            return savedRating;
        }

        Rating rating = Rating.builder()
                .rating(ratingRequest.getRating())
                .movieId(ratingRequest.getMovieId())
                .userId(ratingRequest.getUserId())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Rating savedRating = ratingRepository.save(rating);
        log.info("Successfully created new rating with id {} for user with id {} and movie with id {}",
            savedRating.getId(), savedRating.getUserId(), savedRating.getMovieId());
        return savedRating;
    }

    public Rating findByUserIdAndMovieId(UUID userId, UUID movieId) {
        return ratingRepository.findByUserIdAndMovieId(userId, movieId).orElseThrow(() -> new NotFoundException("Rating with user id [%s] and movie id [%s] not found".formatted(userId, movieId)));
    }

    public void removeRating(UUID userId, UUID movieId) {
        Rating rating = findByUserIdAndMovieId(userId, movieId);
        ratingRepository.delete(rating);
        log.info("Successfully removed rating with id {} for user with id {} and movie with id {}",
            rating.getId(), userId, movieId);
    }

    public Double getAverageRatingForAMovie(UUID movieId) {
        List<Rating> ratings = ratingRepository.findAllByMovieId(movieId);

        if (ratings.isEmpty()) {
            throw new NotFoundException("No ratings found for movie with id [%s]".formatted(movieId));
        }

        double sum = ratings.stream()
                .mapToInt(Rating::getRating)
                .sum();

        Double average = sum / ratings.size();
        log.info("Calculated average rating {} for movie {} based on {} ratings", average, movieId, ratings.size());
        return average;
    }

    public Integer getAllRatingsForAMovieCount(UUID movieId) {
        List<Rating> ratings = ratingRepository.findAllByMovieId(movieId);

        if (ratings.isEmpty()) {
            throw new NotFoundException("No ratings found for movie with id [%s]".formatted(movieId));
        }

        Integer count = ratings.size();
        log.info("Found {} ratings for movie {}", count, movieId);
        return count;
    }

    public Integer getAllRatedMoviesCountByUser(UUID userId) {
        List<Rating> ratings = ratingRepository.findAllByUserId(userId);

        if (ratings.isEmpty()) {
            throw new NotFoundException("No movies rated by user with id [%s]".formatted(userId));
        }

        Integer count = ratings.size();
        log.info("User {} has rated {} movies", userId, count);
        return count;
    }

    public List<RatingResponse> getLatestRatingsByUserId(UUID userId) {
        List<Rating> latestRatings = ratingRepository.findAllByUserIdOrderByUpdatedOnDesc(userId);

        if (latestRatings.isEmpty()) {
            throw new NotFoundException("Latest Ratings not found for user with id [%s]".formatted(userId));
        }

        List<RatingResponse> responses = latestRatings.stream().map(DtoMapper::from).toList();
        List<RatingResponse> limitedResponses = responses.stream().limit(20).toList();
        log.info("Retrieved {} latest ratings for user {} (limited from {})", 
            limitedResponses.size(), userId, responses.size());
        return limitedResponses;
    }
}
