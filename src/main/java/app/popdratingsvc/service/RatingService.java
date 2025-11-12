package app.popdratingsvc.service;

import app.popdratingsvc.model.Rating;
import app.popdratingsvc.repository.RatingRepository;
import app.popdratingsvc.web.dto.RatingRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
}
