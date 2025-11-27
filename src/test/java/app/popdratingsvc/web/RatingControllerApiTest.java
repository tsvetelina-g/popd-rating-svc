package app.popdratingsvc.web;

import app.popdratingsvc.exception.NotFoundException;
import app.popdratingsvc.model.Rating;
import app.popdratingsvc.service.RatingService;
import app.popdratingsvc.web.dto.RatingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatingController.class)
public class RatingControllerApiTest {

    @MockitoBean
    private RatingService ratingService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postUpsertRating_shouldReturn201CreatedAndReturnRatingResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Rating rating = Rating.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .value(5)
                .createdOn(now)
                .updatedOn(now)
                .build();
        when(ratingService.upsert(any())).thenReturn(rating);

        String requestBody = """
                {
                    "userId": "%s",
                    "movieId": "%s",
                    "value": 5
                }
                """.formatted(userId, movieId);

        MockHttpServletRequestBuilder httpRequest = post("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(httpRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.movieId").value(movieId.toString()))
                .andExpect(jsonPath("$.value").value(5));

        verify(ratingService).upsert(any());
    }

    @Test
    void getRatingByUserAndMovie_shouldReturn200OkAndReturnRatingResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Rating rating = Rating.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .movieId(movieId)
                .value(4)
                .createdOn(now)
                .updatedOn(now)
                .build();
        when(ratingService.findByUserIdAndMovieId(userId, movieId)).thenReturn(rating);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/ratings/{userId}/{movieId}", userId, movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.movieId").value(movieId.toString()))
                .andExpect(jsonPath("$.value").value(4));

        verify(ratingService).findByUserIdAndMovieId(userId, movieId);
    }

    @Test
    void getRatingByUserAndMovie_whenRatingNotFound_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        when(ratingService.findByUserIdAndMovieId(userId, movieId))
                .thenThrow(new NotFoundException("Rating not found"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/ratings/{userId}/{movieId}", userId, movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(ratingService).findByUserIdAndMovieId(userId, movieId);
    }

    @Test
    void deleteRating_shouldReturn204NoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        doNothing().when(ratingService).removeRating(userId, movieId);

        MockHttpServletRequestBuilder httpRequest = delete("/api/v1/ratings/{userId}/{movieId}", userId, movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNoContent());

        verify(ratingService).removeRating(userId, movieId);
    }

    @Test
    void deleteRating_whenRatingNotFound_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        doThrow(new NotFoundException("Rating not found")).when(ratingService).removeRating(userId, movieId);

        MockHttpServletRequestBuilder httpRequest = delete("/api/v1/ratings/{userId}/{movieId}", userId, movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(ratingService).removeRating(userId, movieId);
    }

    @Test
    void getMovieRatingStats_shouldReturn200OkAndReturnMovieRatingStatsResponse() throws Exception {
        UUID movieId = UUID.randomUUID();
        when(ratingService.getAverageRatingForAMovie(movieId)).thenReturn(4.5);
        when(ratingService.getAllRatingsForAMovieCount(movieId)).thenReturn(10);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/ratings/{movieId}/stats", movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalRatings").value(10));

        verify(ratingService).getAverageRatingForAMovie(movieId);
        verify(ratingService).getAllRatingsForAMovieCount(movieId);
    }

    @Test
    void getMovieRatingStats_whenNoRatingsFound_shouldReturn404NotFound() throws Exception {
        UUID movieId = UUID.randomUUID();
        when(ratingService.getAverageRatingForAMovie(movieId))
                .thenThrow(new NotFoundException("No ratings found"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/ratings/{movieId}/stats", movieId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(ratingService).getAverageRatingForAMovie(movieId);
    }

    @Test
    void getUserRatingStats_shouldReturn200OkAndReturnUserRatingStatsResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(ratingService.getAllRatedMoviesCountByUser(userId)).thenReturn(5);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/ratings/{userId}/user", userId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratedMovies").value(5));

        verify(ratingService).getAllRatedMoviesCountByUser(userId);
    }

    @Test
    void getUserRatingStats_whenNoRatingsFound_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(ratingService.getAllRatedMoviesCountByUser(userId))
                .thenThrow(new NotFoundException("No movies rated"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/ratings/{userId}/user", userId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(ratingService).getAllRatedMoviesCountByUser(userId);
    }

    @Test
    void getLatestRatingsByUser_shouldReturn200OkAndReturnListOfRatingResponses() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<RatingResponse> responses = List.of(
                RatingResponse.builder()
                        .userId(userId)
                        .movieId(UUID.randomUUID())
                        .value(5)
                        .createdOn(now)
                        .updatedOn(now)
                        .build(),
                RatingResponse.builder()
                        .userId(userId)
                        .movieId(UUID.randomUUID())
                        .value(4)
                        .createdOn(now)
                        .updatedOn(now)
                        .build()
        );
        when(ratingService.getLatestRatingsByUserId(userId)).thenReturn(responses);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/ratings/{userId}/latest-ratings", userId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].value").value(5))
                .andExpect(jsonPath("$[1].value").value(4));

        verify(ratingService).getLatestRatingsByUserId(userId);
    }

    @Test
    void getLatestRatingsByUser_whenNoRatingsFound_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(ratingService.getLatestRatingsByUserId(userId))
                .thenThrow(new NotFoundException("No ratings found"));

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/ratings/{userId}/latest-ratings", userId);

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound());

        verify(ratingService).getLatestRatingsByUserId(userId);
    }
}
