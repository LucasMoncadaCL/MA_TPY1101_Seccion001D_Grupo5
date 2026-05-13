package com.panol_project.backendpanol.modules.catalog.implement.application;

import com.panol_project.backendpanol.modules.catalog.stock.application.contract.InventoryMovementQueryContract;
import com.panol_project.backendpanol.modules.users.application.contract.UserDirectoryContract;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ImplementDetailFacadeService implements ImplementDetailFacade {

    private final InventoryMovementQueryContract inventoryMovementQueryContract;
    private final UserDirectoryContract userDirectoryContract;

    public ImplementDetailFacadeService(
            InventoryMovementQueryContract inventoryMovementQueryContract,
            UserDirectoryContract userDirectoryContract
    ) {
        this.inventoryMovementQueryContract = inventoryMovementQueryContract;
        this.userDirectoryContract = userDirectoryContract;
    }

    @Override
    public List<ImplementRecentMovement> getRecentMovements(UUID implementUuid) {
        return inventoryMovementQueryContract.obtenerUltimosMovimientosPorImplemento(implementUuid)
                .stream()
                .map(movement -> new ImplementRecentMovement(
                        movement.id(),
                        movement.implementUuid(),
                        movement.action(),
                        movement.quantity(),
                        movement.performedByUuid(),
                        movement.timestamp(),
                        movement.notes()
                ))
                .toList();
    }

    @Override
    public Map<UUID, String> getUserNamesByUuid(List<UUID> userUuids) {
        return userDirectoryContract.getNombresUsuariosByUuid(userUuids);
    }
}
