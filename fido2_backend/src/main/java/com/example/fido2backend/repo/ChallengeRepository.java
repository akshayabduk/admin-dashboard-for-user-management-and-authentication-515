package com.example.fido2backend.repo;

import com.example.fido2backend.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {
    Optional<Challenge> findByIdAndConsumedAtIsNullAndExpiresAtAfter(UUID id, OffsetDateTime now);
}
