package app.popdratingsvc.service;

import app.popdratingsvc.model.Rating;
import app.popdratingsvc.repository.RatingRepository;
import app.popdratingsvc.web.dto.RatingRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public Rating upsert(RatingRequest ratingRequest) {

        Optional<Rating> ratingOpt = ratingRepository.findByUserIdAndMovieId(ratingRequest.getUserId(), ratingRequest.getMovieId());

        if (ratingOpt.isPresent()){
            Rating rating = ratingOpt.get();
            rating.setValue(ratingRequest.getValue());
            rating.setUpdatedOn(LocalDateTime.now());

            return ratingRepository.save(rating);
        }

        Rating rating = Rating.builder()
                .value(ratingRequest.getValue())
                .movieId(ratingRequest.getMovieId())
                .userId(ratingRequest.getUserId())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();


        return ratingRepository.save(rating);
    }

    public Rating findByUserIdAndMovieId(UUID userId, UUID movieId) {
        Optional<Rating> ratingOpt = ratingRepository.findByUserIdAndMovieId(userId, movieId);

        return ratingOpt.orElse(null);
    }

    public Boolean removeBy(UUID userId, UUID movieId) {
        Optional<Rating> ratingOpt = ratingRepository.findByUserIdAndMovieId(userId, movieId);

        if (ratingOpt.isPresent()) {
            ratingRepository.delete(ratingOpt.get());
            return true;
        }

        return false;
    }

    public Double getAverageRatingForAMovie(UUID movieId) {

        List<Rating> ratings = ratingRepository.findAllByMovieId(movieId);

        if (ratings.isEmpty()) {
            return null;
        }

        double sum = ratings.stream()
                .mapToInt(Rating::getValue)
                .sum();

        return sum / ratings.size();
    }

    public Integer getAllRatingForAMovieCount(UUID movieId) {

        List<Rating> ratings = ratingRepository.findAllByMovieId(movieId);

        if (ratings.isEmpty()) {
            return null;
        }

       return ratings.size();
    }

    public Integer getAllRatedMoviesCountByUser(UUID userId) {

        List<Rating> ratings = ratingRepository.findAllByUserId(userId);

        if (ratings.isEmpty()){
            return null;
        }

        return ratings.size();
    }
}
