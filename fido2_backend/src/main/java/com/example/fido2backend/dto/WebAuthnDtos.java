package com.example.fido2backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/**
 * DTOs for WebAuthn ceremonies.
 */
public class WebAuthnDtos {

    public static class PublicKeyCredentialRpEntity {
        public String name;
        public String id;
    }

    public static class PublicKeyCredentialUserEntity {
        // base64url of user handle
        public String id;
        public String name;
        public String displayName;
    }

    public static class PublicKeyCredentialParameters {
        public String type = "public-key";
        public int alg;
    }

    public static class PublicKeyCredentialDescriptor {
        public String type = "public-key";
        @Schema(description = "base64url credentialId")
        public String id;
        public List<String> transports;
    }

    public static class AuthenticatorSelectionCriteria {
        public String userVerification; // preferred/required/discouraged
        public String residentKey; // preferred/required/discouraged
        public String authenticatorAttachment; // platform/cross-platform
        public Boolean requireResidentKey;
    }

    public static class PublicKeyCredentialCreationOptions {
        @Schema(description = "base64url challenge")
        public String challenge;
        public PublicKeyCredentialRpEntity rp;
        public PublicKeyCredentialUserEntity user;
        public List<PublicKeyCredentialParameters> pubKeyCredParams;
        public Long timeout;
        public List<PublicKeyCredentialDescriptor> excludeCredentials;
        public AuthenticatorSelectionCriteria authenticatorSelection;
        public String attestation = "none";
        public Map<String, Object> extensions;
    }

    public static class PublicKeyCredentialRequestOptions {
        @Schema(description = "base64url challenge")
        public String challenge;
        public Long timeout;
        public String rpId;
        public List<PublicKeyCredentialDescriptor> allowCredentials;
        public String userVerification; // preferred/required/discouraged
        public Map<String, Object> extensions;
    }

    // Registration verify input
    public static class AttestationResponse {
        @Schema(description = "base64url clientDataJSON")
        public String clientDataJSON;
        @Schema(description = "base64url attestationObject")
        public String attestationObject;
        public String transports; // csv
    }

    public static class RegistrationVerifyRequest {
        public String username;
        public String email;
        public String displayName;
        public String challengeId; // server-side challenge id
        public AttestationResponse response;
    }

    // Authentication verify input
    public static class AssertionResponse {
        @Schema(description = "base64url clientDataJSON")
        public String clientDataJSON;
        @Schema(description = "base64url authenticatorData")
        public String authenticatorData;
        @Schema(description = "base64url signature")
        public String signature;
        @Schema(description = "base64url userHandle")
        public String userHandle;
        public String credentialId; // base64url id
    }

    public static class AuthenticationVerifyRequest {
        public String usernameOrEmail;
        public String challengeId; // server-side challenge id
        public AssertionResponse response;
    }

    public static class TokenResponse {
        public String token;
        public String tokenType = "Bearer";
        public long expiresIn;
    }
}
