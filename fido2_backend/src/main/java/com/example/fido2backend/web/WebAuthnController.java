package com.example.fido2backend.web;

import com.example.fido2backend.domain.User;
import com.example.fido2backend.dto.WebAuthnDtos.*;
import com.example.fido2backend.service.UserService;
import com.example.fido2backend.service.WebAuthnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * WebAuthn endpoints for registration and authentication ceremonies.
 */
@RestController
@RequestMapping("/webauthn")
@Tag(name = "WebAuthn", description = "Registration and authentication ceremonies")
public class WebAuthnController {

    private final WebAuthnService webAuthn;
    private final UserService users;

    public WebAuthnController(WebAuthnService webAuthn, UserService users) {
        this.webAuthn = webAuthn;
        this.users = users;
    }

    // PUBLIC_INTERFACE
    @PostMapping("/register/options")
    @Operation(summary = "Begin registration", description = "Generates PublicKeyCredentialCreationOptions. Provide username, email, displayName.")
    public Map<String, Object> registerOptions(@RequestBody Map<String, String> req) {
        /** Begin registration options generation. */
        String username = req.get("username");
        String email = req.get("email");
        String displayName = req.get("displayName");
        User u = users.upsertUser(username, email, displayName);
        PublicKeyCredentialCreationOptions opts = webAuthn.beginRegistration(u, req.getOrDefault("rpId", "localhost"), req.getOrDefault("rpName", "Demo RP"), 120);
        return Map.of(
                "options", opts
        );
    }

    // PUBLIC_INTERFACE
    @PostMapping("/register/verify")
    @Operation(summary = "Finish registration", description = "Verifies attestation response and persists authenticator.")
    public Map<String, Object> registerVerify(@RequestBody RegistrationVerifyRequest body) {
        /** Finish registration and store authenticator. */
        User u = users.findByUsernameOrEmail(body.username).orElseThrow();
        webAuthn.finishRegistration(body.challengeId, u, body.response);
        return Map.of("status", "ok");
    }

    // PUBLIC_INTERFACE
    @PostMapping("/auth/options")
    @Operation(summary = "Begin authentication", description = "Generates PublicKeyCredentialRequestOptions. Provide usernameOrEmail.")
    public Map<String, Object> authOptions(@RequestBody Map<String, String> req) {
        /** Begin authentication options generation. */
        User u = users.findByUsernameOrEmail(req.get("usernameOrEmail")).orElseThrow();
        PublicKeyCredentialRequestOptions opts = webAuthn.beginAuthentication(u, req.getOrDefault("rpId", "localhost"), 120);
        return Map.of("options", opts);
    }

    // PUBLIC_INTERFACE
    @PostMapping("/auth/verify")
    @Operation(summary = "Finish authentication", description = "Verifies assertion response and returns a token.")
    public TokenResponse authVerify(@RequestBody AuthenticationVerifyRequest body) {
        /** Verify assertion and return JWT token. */
        webAuthn.finishAuthentication(body.challengeId, body.response);
        // In a real app we would sign token with admin claim based on user's roles.
        TokenResponse tr = new TokenResponse();
        tr.token = "placeholder"; // Use AuthController or QR flow to get real tokens
        tr.expiresIn = 3600;
        return tr;
    }
}
