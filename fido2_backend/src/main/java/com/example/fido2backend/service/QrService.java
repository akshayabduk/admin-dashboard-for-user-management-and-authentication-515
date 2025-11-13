package com.example.fido2backend.service;

import com.example.fido2backend.domain.QrSession;
import com.example.fido2backend.domain.User;
import com.example.fido2backend.dto.WebAuthnDtos;
import com.example.fido2backend.repo.QrSessionRepository;
import com.example.fido2backend.security.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class QrService {

    private final QrSessionRepository sessions;
    private final WebAuthnService webAuthnService;
    private final TokenService tokenService;
    private final SecureRandom random = new SecureRandom();

    public QrService(QrSessionRepository sessions, WebAuthnService webAuthnService, TokenService tokenService) {
        this.sessions = sessions;
        this.webAuthnService = webAuthnService;
        this.tokenService = tokenService;
    }

    private String randomToken() {
        byte[] b = new byte[16];
        random.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    // PUBLIC_INTERFACE
    public QrSession createSession(String clientIp, long ttlSeconds) {
        /** Create a new QR login session. */
        QrSession s = new QrSession();
        s.setSessionToken(randomToken());
        s.setStatus("PENDING");
        s.setClientIp(clientIp);
        s.setCreatedAt(OffsetDateTime.now());
        s.setExpiresAt(OffsetDateTime.now().plusSeconds(ttlSeconds));
        return sessions.save(s);
    }

    // PUBLIC_INTERFACE
    public Optional<QrSession> getSession(UUID id) {
        /** Get session by id. */
        return sessions.findById(id);
    }

    // PUBLIC_INTERFACE
    public WebAuthnDtos.PublicKeyCredentialRequestOptions getAuthOptionsForSession(QrSession s, User user, String rpId) {
        /** Bind an auth challenge to a QR session. */
        s.setStatus("SCANNED");
        s.setScannedAt(OffsetDateTime.now());
        s.setUser(user);
        sessions.save(s);
        return webAuthnService.beginAuthentication(user, rpId, 120);
    }

    // PUBLIC_INTERFACE
    public void verifyForSession(QrSession s, String challengeId, WebAuthnDtos.AssertionResponse response) {
        /** Verify the assertion and store JWT in session result. */
        webAuthnService.finishAuthentication(challengeId, response);
        s.setStatus("APPROVED");
        s.setApprovedAt(OffsetDateTime.now());
        // For demo, non-admin token
        String token = tokenService.issueToken(
                s.getUser().getUsername(),
                3600,
                java.util.Map.of("admin", false, "uid", s.getUser().getId().toString())
        );
        s.setResultToken(token);
        sessions.save(s);
    }
}
