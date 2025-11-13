package com.example.fido2backend.web;

import com.example.fido2backend.domain.Authenticator;
import com.example.fido2backend.domain.User;
import com.example.fido2backend.repo.AuthenticatorRepository;
import com.example.fido2backend.repo.AuditLogRepository;
import com.example.fido2backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin operations")
public class AdminController {

    private final UserService users;
    private final AuthenticatorRepository authenticators;
    private final AuditLogRepository audit;

    public AdminController(UserService users, AuthenticatorRepository authenticators, AuditLogRepository audit) {
        this.users = users;
        this.authenticators = authenticators;
        this.audit = audit;
    }

    // PUBLIC_INTERFACE
    @GetMapping("/users")
    @Operation(summary = "List users", description = "Returns all users.")
    public List<Map<String,Object>> listUsers() {
        /** Admin list of users. */
        List<Map<String,Object>> out = new ArrayList<>();
        for (User u : users.listAll()) {
            out.add(Map.of(
                    "id", u.getId(),
                    "username", u.getUsername(),
                    "email", u.getEmail(),
                    "displayName", u.getDisplayName(),
                    "active", u.isActive(),
                    "locked", u.isLocked()
            ));
        }
        return out;
    }

    // PUBLIC_INTERFACE
    @PostMapping("/users/{id}/toggle")
    @Operation(summary = "Toggle active", description = "Enables/disables a user.")
    public Map<String,String> toggle(@PathVariable UUID id, @RequestParam boolean active) {
        /** Toggle user active flag. */
        users.setActive(id, active);
        return Map.of("status", "ok");
    }

    // PUBLIC_INTERFACE
    @GetMapping("/users/{id}/credentials")
    @Operation(summary = "List credentials", description = "Returns credentials for a user.")
    public List<Map<String,Object>> credentials(@PathVariable UUID id) {
        /** List authenticators for a given user. */
        List<Map<String,Object>> out = new ArrayList<>();
        for (Authenticator a : authenticators.findByUser_Id(id)) {
            out.add(Map.of(
                    "id", a.getId(),
                    "signCount", a.getSignCount(),
                    "registeredAt", a.getRegisteredAt(),
                    "lastUsedAt", a.getLastUsedAt()
            ));
        }
        return out;
    }

    // PUBLIC_INTERFACE
    @DeleteMapping("/credentials/{credentialId}")
    @Operation(summary = "Delete credential", description = "Deletes an authenticator by id.")
    public Map<String,String> deleteCredential(@PathVariable UUID credentialId) {
        /** Delete authenticator. */
        authenticators.deleteById(credentialId);
        return Map.of("status", "deleted");
    }

    // PUBLIC_INTERFACE
    @GetMapping("/audit")
    @Operation(summary = "Audit logs", description = "Returns audit entries (placeholder - implement filtering/paging later).")
    public Map<String,Object> audit() {
        /** Placeholder audit endpoint. */
        return Map.of("count", audit.count());
    }
}
