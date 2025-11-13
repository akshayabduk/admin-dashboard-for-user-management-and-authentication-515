package com.example.fido2backend.repo;

import com.example.fido2backend.domain.Authenticator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthenticatorRepository extends JpaRepository<Authenticator, UUID> {
    Optional<Authenticator> findByCredentialId(byte[] credentialId);
    List<Authenticator> findByUser_Id(UUID userId);
}
