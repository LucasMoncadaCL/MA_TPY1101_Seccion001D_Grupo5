package com.panol_project.backendpanol.modules.catalog.stock.infrastructure.mongo;

import com.panol_project.backendpanol.modules.catalog.stock.domain.InventoryMovement;
import com.panol_project.backendpanol.modules.catalog.stock.domain.InventoryMovementRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryMovementMongoAdapter implements InventoryMovementRepository {

    private final InventoryMovementRepositoryMongo repository;

    public InventoryMovementMongoAdapter(InventoryMovementRepositoryMongo repository) {
        this.repository = repository;
    }

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        InventoryMovementDocument doc = toDocument(movement);
        InventoryMovementDocument saved = repository.save(doc);
        return toDomain(saved);
    }

    @Override
    public List<InventoryMovement> findTop10ByImplementUuidOrderByTimestampDesc(UUID implementUuid) {
        return repository.findTop10ByImplementUuidOrderByTimestampDesc(implementUuid).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<InventoryMovement> findAllByOrderByTimestampDesc() {
        return repository.findAllByOrderByTimestampDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    private InventoryMovementDocument toDocument(InventoryMovement domain) {
        InventoryMovementDocument doc = new InventoryMovementDocument();
        doc.setId(domain.getId());
        doc.setImplementUuid(domain.getImplementUuid());
        doc.setAction(domain.getAction());
        doc.setQuantity(domain.getQuantity());
        doc.setPerformedByUuid(domain.getPerformedByUuid());
        doc.setTimestamp(domain.getTimestamp());
        doc.setNotes(domain.getNotes());
        return doc;
    }

    private InventoryMovement toDomain(InventoryMovementDocument doc) {
        InventoryMovement movement = new InventoryMovement(
                doc.getImplementUuid(),
                doc.getAction(),
                doc.getQuantity(),
                doc.getPerformedByUuid(),
                doc.getTimestamp(),
                doc.getNotes()
        );
        movement.setId(doc.getId());
        return movement;
    }
}
