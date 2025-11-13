package com.example.fido2backend.web;

import com.example.fido2backend.domain.QrSession;
import com.example.fido2backend.domain.User;
import com.example.fido2backend.dto.WebAuthnDtos;
import com.example.fido2backend.service.QrService;
import com.example.fido2backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/qr")
@Tag(name = "QR", description = "QR-based login flow")
public class QrController {

    private final QrService qrService;
    private final UserService userService;

    public QrController(QrService qrService, UserService userService) {
        this.qrService = qrService;
        this.userService = userService;
    }

    // PUBLIC_INTERFACE
    @PostMapping("/sessions")
    @Operation(summary = "Create QR session", description = "Creates a short-lived QR login session.")
    public Map<String,Object> createSession(HttpServletRequest req) {
        /** Create a new QR session for login polling. */
        QrSession s = qrService.createSession(req.getRemoteAddr(), 180);
        return Map.of(
                "id", s.getId(),
                "token", s.getSessionToken(),
                "status", s.getStatus(),
                "expiresAt", s.getExpiresAt()
        );
    }

    // PUBLIC_INTERFACE
    @GetMapping("/sessions/{id}/status")
    @Operation(summary = "Get session status", description = "Poll QR session status.")
    public Map<String,Object> status(@PathVariable UUID id) {
        /** Return current session status. */
        QrSession s = qrService.getSession(id).orElseThrow();
        return Map.of(
                "status", s.getStatus(),
                "approvedAt", s.getApprovedAt(),
                "rejectedAt", s.getRejectedAt(),
                "scannedAt", s.getScannedAt()
        );
    }

    // PUBLIC_INTERFACE
    @GetMapping("/sessions/{id}/options")
    @Operation(summary = "Get auth options for session", description = "When mobile scans QR, fetch options to perform WebAuthn authentication.")
    public Map<String,Object> options(@PathVariable UUID id, @RequestParam String usernameOrEmail) {
        /** Bind authentication options to session for the identified user. */
        QrSession s = qrService.getSession(id).orElseThrow();
        User u = userService.findByUsernameOrEmail(usernameOrEmail).orElseThrow();
        WebAuthnDtos.PublicKeyCredentialRequestOptions opts = qrService.getAuthOptionsForSession(s, u, "localhost");
        return Map.of("options", opts);
    }

    // PUBLIC_INTERFACE
    @PostMapping("/sessions/{id}/verify")
    @Operation(summary = "Verify assertion for session", description = "Mobile client posts WebAuthn assertion; on success session is approved and token stored.")
    public Map<String,Object> verify(@PathVariable UUID id, @RequestBody WebAuthnDtos.AuthenticationVerifyRequest body) {
        /** Verify user's assertion and mark session approved with token. */
        QrSession s = qrService.getSession(id).orElseThrow();
        qrService.verifyForSession(s, body.challengeId, body.response);
        return Map.of("status", "APPROVED");
    }

    // PUBLIC_INTERFACE
    @GetMapping("/sessions/{id}/result")
    @Operation(summary = "Get session result token", description = "Frontend polls and retrieves the resulting JWT after approval.")
    public Map<String,Object> result(@PathVariable UUID id) {
        /** Return stored JWT if available. */
        QrSession s = qrService.getSession(id).orElseThrow();
        return Map.of("token", s.getResultToken());
    }

    // PUBLIC_INTERFACE
    @GetMapping(value = "/session/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "QR Session HTML", description = "Optional simple HTML page to show that session exists.")
    public String sessionHtml(@PathVariable UUID id) {
        /** Simple HTML for debugging/demo. */
        return "<!doctype html><html><body><h3>QR Session " + id + "</h3><p>Scan with mobile app to sign in.</p></body></html>";
    }
}
