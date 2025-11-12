package app.popdratingsvc.web;

import app.popdratingsvc.model.Rating;
import app.popdratingsvc.service.RatingService;
import app.popdratingsvc.web.dto.RatingRequest;
import app.popdratingsvc.web.dto.RatingResponse;
import app.popdratingsvc.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
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
}
