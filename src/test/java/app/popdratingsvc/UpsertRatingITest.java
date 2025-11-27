package app.popdratingsvc;

import app.popdratingsvc.model.Rating;
import app.popdratingsvc.repository.RatingRepository;
import app.popdratingsvc.service.RatingService;
import app.popdratingsvc.web.dto.RatingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class UpsertRatingITest {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    void upsertRating_whenRatingDoesNotExist_shouldCreateNewRatingAndPersistInDatabase() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        RatingRequest ratingRequest = RatingRequest.builder()
                .userId(userId)
                .movieId(movieId)
                .value(5)
                .build();

        Rating createdRating = ratingService.upsert(ratingRequest);

        assertNotNull(createdRating.getId());
        assertEquals(userId, createdRating.getUserId());
        assertEquals(movieId, createdRating.getMovieId());
        assertEquals(5, createdRating.getValue());
        assertNotNull(createdRating.getCreatedOn());
        assertNotNull(createdRating.getUpdatedOn());

        Rating ratingFromDb = ratingRepository.findById(createdRating.getId()).orElse(null);
        assertNotNull(ratingFromDb);
        assertEquals(5, ratingFromDb.getValue());
        assertEquals(1, ratingRepository.count());
    }

    @Test
    void upsertRating_whenRatingAlreadyExists_shouldUpdateExistingRatingAndPersistInDatabase() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        LocalDateTime originalCreatedOn = LocalDateTime.now().minusDays(1);

        Rating existingRating = Rating.builder()
                .userId(userId)
                .movieId(movieId)
                .value(3)
                .createdOn(originalCreatedOn)
                .updatedOn(originalCreatedOn)
                .build();
        ratingRepository.save(existingRating);

        RatingRequest updateRequest = RatingRequest.builder()
                .userId(userId)
                .movieId(movieId)
                .value(5)
                .build();

        Rating updatedRating = ratingService.upsert(updateRequest);

        assertEquals(existingRating.getId(), updatedRating.getId());
        assertEquals(5, updatedRating.getValue());
        assertThat(updatedRating.getCreatedOn()).isCloseTo(originalCreatedOn, within(1, ChronoUnit.MICROS));
        assertTrue(updatedRating.getUpdatedOn().isAfter(originalCreatedOn));
        assertEquals(1, ratingRepository.count());

        Rating ratingFromDb = ratingRepository.findById(updatedRating.getId()).orElse(null);
        assertNotNull(ratingFromDb);
        assertEquals(5, ratingFromDb.getValue());
    }

    @Test
    void upsertRating_multipleRatingsScenario_shouldHandleCorrectlyBasedOnUserAndMovie() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID movie1 = UUID.randomUUID();
        UUID movie2 = UUID.randomUUID();

        RatingRequest user1Movie1 = RatingRequest.builder()
                .userId(user1)
                .movieId(movie1)
                .value(5)
                .build();
        RatingRequest user1Movie2 = RatingRequest.builder()
                .userId(user1)
                .movieId(movie2)
                .value(4)
                .build();
        RatingRequest user2Movie1 = RatingRequest.builder()
                .userId(user2)
                .movieId(movie1)
                .value(3)
                .build();

        ratingService.upsert(user1Movie1);
        ratingService.upsert(user1Movie2);
        ratingService.upsert(user2Movie1);

        assertEquals(3, ratingRepository.count());
        assertEquals(2, ratingRepository.findAllByUserId(user1).size());
        assertEquals(1, ratingRepository.findAllByUserId(user2).size());
        assertEquals(2, ratingRepository.findAllByMovieId(movie1).size());
        assertEquals(1, ratingRepository.findAllByMovieId(movie2).size());

        RatingRequest user1Movie1Update = RatingRequest.builder()
                .userId(user1)
                .movieId(movie1)
                .value(1)
                .build();
        Rating updated = ratingService.upsert(user1Movie1Update);

        assertEquals(3, ratingRepository.count());
        assertEquals(1, updated.getValue());
    }
}
