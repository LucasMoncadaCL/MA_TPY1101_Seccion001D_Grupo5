package com.panol_project.backendpanol.modules.catalog.stock.api;

import com.panol_project.backendpanol.modules.catalog.stock.api.dto.RegisterMovementRequest;
import com.panol_project.backendpanol.modules.catalog.stock.api.dto.InventoryMovementResponse;
import com.panol_project.backendpanol.modules.catalog.stock.application.InventoryMovementService;
import com.panol_project.backendpanol.modules.catalog.stock.domain.InventoryMovement;
import com.panol_project.backendpanol.modules.catalog.stock.domain.MovementAction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/implements")
public class InventoryMovementController {

    private final InventoryMovementService service;

    public InventoryMovementController(InventoryMovementService service) {
        this.service = service;
    }

    @PostMapping("/{id}/movements")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COORDINADOR')")
    public InventoryMovementResponse registrarMovimiento(
            @PathVariable Integer id,
            @Valid @RequestBody RegisterMovementRequest request,
            Authentication authentication
    ) {
        String performedBy = extractUserId(authentication);
        MovementAction domainAction = MovementAction.valueOf(request.action().name());
        
        InventoryMovement movement = service.registrarMovimiento(
                id, 
                domainAction, 
                request.quantity(), 
                performedBy, 
                request.notes()
        );
        return toResponse(movement);
    }

    private InventoryMovementResponse toResponse(InventoryMovement m) {
        return new InventoryMovementResponse(
                m.getId(),
                m.getImplementId(),
                m.getAction(),
                m.getQuantity(),
                m.getPerformedBy(),
                m.getTimestamp(),
                m.getNotes()
        );
    }

    private String extractUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            // Fallback for local testing without JWT security active
            return "testing-user-id";
        }
        return jwt.getSubject();
    }
}
