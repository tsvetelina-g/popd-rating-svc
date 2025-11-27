package app.popdratingsvc.repository;

import app.popdratingsvc.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    Optional<Rating> findByUserIdAndMovieId(UUID userId, UUID movieId);

    List<Rating> findAllByMovieId(UUID movieId);

    List<Rating> findAllByUserId(UUID userId);

    List<Rating> findAllByUserIdOrderByUpdatedOnDesc(UUID userId);
}
