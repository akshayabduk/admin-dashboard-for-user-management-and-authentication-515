package com.example.fido2backend.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication helper endpoints")
public class AuthController {

    // PUBLIC_INTERFACE
    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Returns the current authenticated principal based on JWT.")
    public Map<String, Object> me(Authentication auth) {
        /** Returns name and authorities if authenticated, otherwise anonymous. */
        if (auth == null) {
            return Map.of("authenticated", false);
        }
        return Map.of(
                "authenticated", true,
                "name", auth.getName(),
                "authorities", auth.getAuthorities()
        );
    }
}
