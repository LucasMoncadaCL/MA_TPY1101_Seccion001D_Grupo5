package com.panol_project.backendpanol.modules.users.api;

import com.panol_project.backendpanol.modules.users.api.dto.ChangeRoleRequest;
import com.panol_project.backendpanol.modules.users.api.dto.CreateUserRequest;
import com.panol_project.backendpanol.modules.users.application.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTOR')")
    ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserRequest request, @AuthenticationPrincipal Jwt jwt) {
        userAdminService.createUser(request, jwt);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('DIRECTOR')")
    ResponseEntity<Void> changeRole(
            @PathVariable Integer userId,
            @Valid @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        userAdminService.changeRole(userId, request.role(), jwt);
        return ResponseEntity.noContent().build();
    }
}

