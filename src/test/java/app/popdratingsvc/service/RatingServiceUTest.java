package app.popdratingsvc.service;

import app.popdratingsvc.exception.NotFoundException;
import app.popdratingsvc.model.Rating;
import app.popdratingsvc.repository.RatingRepository;
import app.popdratingsvc.web.dto.RatingRequest;
import app.popdratingsvc.web.dto.RatingResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceUTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingService ratingService;

    @Test
    void whenUpsert_andRatingDoesNotExist_thenCreateNewRatingAndPersist() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        RatingRequest request = RatingRequest.builder()
                .userId(userId)
                .movieId(movieId)
                .value(5)
                .build();
        when(ratingRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setId(UUID.randomUUID());
            return rating;
        });

        Rating result = ratingService.upsert(request);

        assertNotNull(result);
        assertEquals(5, result.getValue());
        assertEquals(userId, result.getUserId());
        assertEquals(movieId, result.getMovieId());
        assertThat(result.getCreatedOn()).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        assertThat(result.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void whenUpsert_andRatingAlreadyExists_thenUpdateExistingRatingAndPersist() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();
        RatingRequest request = RatingRequest.builder()
                .userId(userId)
                .movieId(movieId)
                .value(4)
                .build();
        Rating existingRating = Rating.builder()
                .id(ratingId)
                .userId(userId)
                .movieId(movieId)
                .value(3)
                .createdOn(LocalDateTime.now().minusDays(1))
                .updatedOn(LocalDateTime.now().minusDays(1))
                .build();
        when(ratingRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.of(existingRating));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rating result = ratingService.upsert(request);

        assertNotNull(result);
        assertEquals(ratingId, result.getId());
        assertEquals(4, result.getValue());
        assertThat(result.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        verify(ratingRepository).save(existingRating);
    }

    @Test
    void whenFindByUserIdAndMovieId_andRatingExists_thenReturnRating() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .value(5)
                .build();
        when(ratingRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.of(rating));

        Rating result = ratingService.findByUserIdAndMovieId(userId, movieId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(movieId, result.getMovieId());
    }

    @Test
    void whenFindByUserIdAndMovieId_andRatingDoesNotExist_thenThrowException() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        when(ratingRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ratingService.findByUserIdAndMovieId(userId, movieId));
    }

    @Test
    void whenRemoveRating_andRatingExists_thenDeleteRating() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .value(5)
                .build();
        when(ratingRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.of(rating));

        ratingService.removeRating(userId, movieId);

        verify(ratingRepository).delete(rating);
    }

    @Test
    void whenRemoveRating_andRatingDoesNotExist_thenThrowException() {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        when(ratingRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ratingService.removeRating(userId, movieId));
        verify(ratingRepository, never()).delete(any());
    }

    @Test
    void whenGetAverageRatingForAMovie_andRatingsExist_thenReturnAverage() {
        UUID movieId = UUID.randomUUID();
        List<Rating> ratings = List.of(
                Rating.builder().movieId(movieId).value(5).build(),
                Rating.builder().movieId(movieId).value(4).build(),
                Rating.builder().movieId(movieId).value(3).build()
        );
        when(ratingRepository.findAllByMovieId(movieId)).thenReturn(ratings);

        Double result = ratingService.getAverageRatingForAMovie(movieId);

        assertEquals(4.0, result);
    }

    @Test
    void whenGetAverageRatingForAMovie_andNoRatingsExist_thenThrowException() {
        UUID movieId = UUID.randomUUID();
        when(ratingRepository.findAllByMovieId(movieId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> ratingService.getAverageRatingForAMovie(movieId));
    }

    @Test
    void whenGetAllRatingsForAMovieCount_andRatingsExist_thenReturnCount() {
        UUID movieId = UUID.randomUUID();
        List<Rating> ratings = List.of(
                Rating.builder().movieId(movieId).value(5).build(),
                Rating.builder().movieId(movieId).value(4).build()
        );
        when(ratingRepository.findAllByMovieId(movieId)).thenReturn(ratings);

        Integer result = ratingService.getAllRatingsForAMovieCount(movieId);

        assertEquals(2, result);
    }

    @Test
    void whenGetAllRatingsForAMovieCount_andNoRatingsExist_thenThrowNotFoundException() {
        UUID movieId = UUID.randomUUID();
        when(ratingRepository.findAllByMovieId(movieId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> ratingService.getAllRatingsForAMovieCount(movieId));
    }

    @Test
    void whenGetAllRatedMoviesCountByUser_andRatingsExist_thenReturnCount() {
        UUID userId = UUID.randomUUID();
        List<Rating> ratings = List.of(
                Rating.builder().userId(userId).movieId(UUID.randomUUID()).value(5).build(),
                Rating.builder().userId(userId).movieId(UUID.randomUUID()).value(4).build(),
                Rating.builder().userId(userId).movieId(UUID.randomUUID()).value(3).build()
        );
        when(ratingRepository.findAllByUserId(userId)).thenReturn(ratings);

        Integer result = ratingService.getAllRatedMoviesCountByUser(userId);

        assertEquals(3, result);
    }

    @Test
    void whenGetAllRatedMoviesCountByUser_andNoRatingsExist_thenThrowNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(ratingRepository.findAllByUserId(userId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> ratingService.getAllRatedMoviesCountByUser(userId));
    }

    @Test
    void whenGetLatestRatingsByUserId_andRatingsExist_thenReturnLimitedRatingResponses() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<Rating> ratings = List.of(
                Rating.builder().userId(userId).movieId(UUID.randomUUID()).value(5).createdOn(now).updatedOn(now).build(),
                Rating.builder().userId(userId).movieId(UUID.randomUUID()).value(4).createdOn(now).updatedOn(now).build()
        );
        when(ratingRepository.findAllByUserIdOrderByUpdatedOnDesc(userId)).thenReturn(ratings);

        List<RatingResponse> result = ratingService.getLatestRatingsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void whenGetLatestRatingsByUserId_andNoRatingsExist_thenThrowNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(ratingRepository.findAllByUserIdOrderByUpdatedOnDesc(userId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> ratingService.getLatestRatingsByUserId(userId));
    }

    @Test
    void whenGetLatestRatingsByUserId_andMoreThan20RatingsExist_thenReturnOnly20() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<Rating> ratings = new java.util.ArrayList<>();
        for (int i = 0; i < 25; i++) {
            ratings.add(Rating.builder()
                    .userId(userId)
                    .movieId(UUID.randomUUID())
                    .value(i % 5 + 1)
                    .createdOn(now)
                    .updatedOn(now)
                    .build());
        }
        when(ratingRepository.findAllByUserIdOrderByUpdatedOnDesc(userId)).thenReturn(ratings);

        List<RatingResponse> result = ratingService.getLatestRatingsByUserId(userId);

        assertNotNull(result);
        assertEquals(20, result.size());
    }
}
