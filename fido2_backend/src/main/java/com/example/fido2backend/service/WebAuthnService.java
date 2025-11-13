package com.example.fido2backend.service;

import com.example.fido2backend.domain.Authenticator;
import com.example.fido2backend.domain.Challenge;
import com.example.fido2backend.domain.User;
import com.example.fido2backend.dto.WebAuthnDtos.*;
import com.example.fido2backend.repo.AuthenticatorRepository;
import com.example.fido2backend.repo.ChallengeRepository;
import com.example.fido2backend.util.Base64UrlUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides WebAuthn ceremonies: generation of options and verification.
 * This implementation provides the structure and essential state management;
 * cryptographic verification should be expanded with a full WebAuthn validation library in production.
 */
@Service
@Transactional
public class WebAuthnService {

    private final ChallengeRepository challenges;
    private final AuthenticatorRepository authenticators;
    private final SecureRandom random = new SecureRandom();

    public WebAuthnService(ChallengeRepository challenges, AuthenticatorRepository authenticators) {
        this.challenges = challenges;
        this.authenticators = authenticators;
    }

    private byte[] randomChallenge(int len) {
        byte[] b = new byte[len];
        random.nextBytes(b);
        return b;
    }

    // PUBLIC_INTERFACE
    public PublicKeyCredentialCreationOptions beginRegistration(User user, String rpId, String rpName, long timeoutSeconds) {
        /** Create a registration challenge and return options to client. */
        byte[] challengeBytes = randomChallenge(32);
        Challenge c = new Challenge();
        c.setUser(user);
        c.setChallengeType("registration");
        c.setChallenge(challengeBytes);
        c.setCreatedAt(OffsetDateTime.now());
        c.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        challenges.save(c);

        PublicKeyCredentialCreationOptions opts = new PublicKeyCredentialCreationOptions();
        opts.challenge = Base64UrlUtil.toBase64Url(challengeBytes);
        opts.rp = new PublicKeyCredentialRpEntity();
        opts.rp.id = rpId;
        opts.rp.name = rpName;

        PublicKeyCredentialUserEntity u = new PublicKeyCredentialUserEntity();
        u.id = Base64UrlUtil.toBase64Url(user.getId().toString().getBytes());
        u.name = user.getUsername();
        u.displayName = Optional.ofNullable(user.getDisplayName()).orElse(user.getUsername());
        opts.user = u;

        List<PublicKeyCredentialParameters> params = new ArrayList<>();
        // Common algorithms
        int[] algs = new int[]{ -7, -257 }; // ES256, RS256
        for (int alg : algs) {
            PublicKeyCredentialParameters p = new PublicKeyCredentialParameters();
            p.alg = alg;
            params.add(p);
        }
        opts.pubKeyCredParams = params;
        opts.timeout = timeoutSeconds * 1000;
        opts.excludeCredentials = new ArrayList<>();
        // In production, populate excludeCredentials with existing credential IDs for the user

        return opts;
    }

    // PUBLIC_INTERFACE
    public void finishRegistration(String challengeId, User user, AttestationResponse response) {
        /** Consume challenge and persist authenticator (minimal parsing stub). */
        Challenge c = challenges.findByIdAndConsumedAtIsNullAndExpiresAtAfter(UUID.fromString(challengeId), OffsetDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired challenge"));
        c.setConsumedAt(OffsetDateTime.now());
        challenges.save(c);

        // In real implementation, parse attestationObject (CBOR), extract credentialId and publicKey (COSE),
        // verify clientDataJSON type/origin/challenge, verify attestation and RP ID hash, then store values.

        // For now, we store placeholders using the attestationObject hash as publicKey surrogate.
        byte[] attBytes = Base64UrlUtil.fromBase64Url(response.attestationObject);
        byte[] credId = ("cred-" + user.getId()).getBytes(); // placeholder, replace with actual from attestation
        Authenticator a = new Authenticator();
        a.setUser(user);
        a.setCredentialId(credId);
        a.setPublicKey(attBytes); // placeholder
        a.setTransports(response.transports);
        a.setSignCount(0);
        authenticators.save(a);
    }

    // PUBLIC_INTERFACE
    public PublicKeyCredentialRequestOptions beginAuthentication(User user, String rpId, long timeoutSeconds) {
        /** Create an authentication challenge and return options to client. */
        byte[] challengeBytes = randomChallenge(32);
        Challenge c = new Challenge();
        c.setUser(user);
        c.setChallengeType("authentication");
        c.setChallenge(challengeBytes);
        c.setCreatedAt(OffsetDateTime.now());
        c.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        challenges.save(c);

        PublicKeyCredentialRequestOptions opts = new PublicKeyCredentialRequestOptions();
        opts.challenge = Base64UrlUtil.toBase64Url(challengeBytes);
        opts.timeout = timeoutSeconds * 1000;
        opts.rpId = rpId;
        // allowCredentials could be populated with user's registered authenticators
        return opts;
    }

    // PUBLIC_INTERFACE
    public void finishAuthentication(String challengeId, AssertionResponse response) {
        /** Consume challenge and verify assertion (minimal stub updates signCount). */
        Challenge c = challenges.findByIdAndConsumedAtIsNullAndExpiresAtAfter(UUID.fromString(challengeId), OffsetDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired challenge"));
        c.setConsumedAt(OffsetDateTime.now());
        challenges.save(c);

        byte[] credId = Base64UrlUtil.fromBase64Url(response.credentialId);
        Authenticator a = authenticators.findByCredentialId(credId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown credential"));

        // In real implementation, verify:
        // - clientDataJSON, type = "webauthn.get", challenge match
        // - authenticatorData RP ID hash, flags, counter, signCount monotonicity
        // - signature over clientDataHash + authenticatorData using stored public key
        // For now, we just bump signCount as placeholder.
        a.setSignCount(a.getSignCount() + 1);
        authenticators.save(a);
    }
}
