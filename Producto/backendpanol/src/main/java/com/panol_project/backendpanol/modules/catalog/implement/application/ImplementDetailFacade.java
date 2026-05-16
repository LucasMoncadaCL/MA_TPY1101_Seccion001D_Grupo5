package com.panol_project.backendpanol.modules.catalog.implement.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ImplementDetailFacade {
    List<ImplementRecentMovement> getRecentMovements(UUID implementUuid);
    Map<UUID, String> getUserNamesByUuid(List<UUID> userUuids);
}
