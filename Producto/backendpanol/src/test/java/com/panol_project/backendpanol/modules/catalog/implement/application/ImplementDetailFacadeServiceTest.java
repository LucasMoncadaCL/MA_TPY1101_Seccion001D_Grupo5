package com.panol_project.backendpanol.modules.catalog.implement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.panol_project.backendpanol.modules.catalog.stock.application.contract.InventoryMovementQueryContract;
import com.panol_project.backendpanol.modules.catalog.stock.application.contract.InventoryMovementQueryContract.InventoryMovementView;
import com.panol_project.backendpanol.modules.users.application.contract.UserDirectoryContract;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImplementDetailFacadeServiceTest {

    @Mock
    private InventoryMovementQueryContract inventoryMovementQueryContract;

    @Mock
    private UserDirectoryContract userDirectoryContract;

    @Test
    void shouldDelegateToContracts() {
        UUID implementUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        InventoryMovementView movement = new InventoryMovementView(
                "m1",
                implementUuid,
                "STOCK_IN",
                3,
                userUuid,
                Instant.now(),
                "ok"
        );

        ImplementDetailFacadeService service = new ImplementDetailFacadeService(
                inventoryMovementQueryContract,
                userDirectoryContract
        );

        when(inventoryMovementQueryContract.obtenerUltimosMovimientosPorImplemento(implementUuid)).thenReturn(List.of(movement));
        when(userDirectoryContract.getNombresUsuariosByUuid(List.of(userUuid))).thenReturn(Map.of(userUuid, "Ana"));

        List<ImplementRecentMovement> resultMovements = service.getRecentMovements(implementUuid);
        Map<UUID, String> names = service.getUserNamesByUuid(List.of(userUuid));

        assertEquals(1, resultMovements.size());
        assertEquals("Ana", names.get(userUuid));
        verify(inventoryMovementQueryContract).obtenerUltimosMovimientosPorImplemento(implementUuid);
        verify(userDirectoryContract).getNombresUsuariosByUuid(List.of(userUuid));
    }
}
