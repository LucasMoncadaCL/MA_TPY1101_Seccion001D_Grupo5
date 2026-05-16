package com.panol_project.backendpanol.modules.catalog.stock.infrastructure.mongo;

import com.panol_project.backendpanol.modules.catalog.stock.domain.MovementAction;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "inventory_movement")
public class InventoryMovementDocument {

    @Id
    private String id;

    @Field("implement_uuid")
    private UUID implementUuid;

    private MovementAction action;
    private Integer quantity;

    @Field("performed_by_uuid")
    private UUID performedByUuid;

    private Instant timestamp;
    private String notes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public UUID getImplementUuid() { return implementUuid; }
    public void setImplementUuid(UUID implementUuid) { this.implementUuid = implementUuid; }
    public MovementAction getAction() { return action; }
    public void setAction(MovementAction action) { this.action = action; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public UUID getPerformedByUuid() { return performedByUuid; }
    public void setPerformedByUuid(UUID performedByUuid) { this.performedByUuid = performedByUuid; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
