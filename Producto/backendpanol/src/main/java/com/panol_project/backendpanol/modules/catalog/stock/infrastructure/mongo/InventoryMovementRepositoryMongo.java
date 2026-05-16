package com.panol_project.backendpanol.modules.catalog.stock.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepositoryMongo extends MongoRepository<InventoryMovementDocument, String> {
    List<InventoryMovementDocument> findTop10ByImplementUuidOrderByTimestampDesc(UUID implementUuid);
    List<InventoryMovementDocument> findAllByOrderByTimestampDesc();
}
