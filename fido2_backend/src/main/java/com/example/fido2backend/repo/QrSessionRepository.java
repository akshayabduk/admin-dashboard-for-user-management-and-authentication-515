package com.example.fido2backend.repo;

import com.example.fido2backend.domain.QrSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QrSessionRepository extends JpaRepository<QrSession, UUID> {
    Optional<QrSession> findBySessionToken(String token);
}
