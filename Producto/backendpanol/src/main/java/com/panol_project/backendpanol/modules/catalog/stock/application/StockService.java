package com.panol_project.backendpanol.modules.catalog.stock.application;

import com.panol_project.backendpanol.modules.catalog.stock.domain.IndividualItem;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockCounters;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockDetail;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockItemType;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockMovementType;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockRepository;
import com.panol_project.backendpanol.shared.error.BadRequestException;
import com.panol_project.backendpanol.shared.error.ConflictException;
import com.panol_project.backendpanol.shared.error.NotFoundException;
import com.panol_project.backendpanol.shared.outbox.application.OutboxService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private static final Set<String> VALID_INDIVIDUAL_STATUS = Set.of(
            "available", "loaned", "maintenance", "damaged", "blocked", "retired"
    );

    private static final Set<String> VALID_INDIVIDUAL_CONDITION = Set.of(
            "good", "damaged_repairable", "damaged_no_diagnosis", "irreparable"
    );

    private final StockRepository repository;
    private final OutboxService outboxService;

    public StockService(StockRepository repository, OutboxService outboxService) {
        this.repository = repository;
        this.outboxService = outboxService;
    }

    @Transactional(readOnly = true)
    public StockDetail getStockDetail(UUID implementUuid) {
        var context = requireContext(implementUuid);

        StockCounters counters = repository.findStockByImplementUuid(implementUuid)
                .orElse(new StockCounters(0, 0, 0, 0, 0, 0));

        List<IndividualItem> individuals = context.itemType() == StockItemType.INDIVIDUAL
                ? repository.findActiveIndividualsByImplementUuid(implementUuid)
                : List.of();

        if (context.itemType() == StockItemType.INDIVIDUAL) {
            counters = deriveCountersForIndividuals(counters, individuals);
        }

        return new StockDetail(implementUuid, context.itemType(), counters, individuals);
    }

    @Transactional
    public StockDetail addEntry(UUID implementUuid, Integer quantity, List<String> assetCodes) {
        var context = requireContext(implementUuid);
        int qty = requirePositiveQuantity(quantity);

        repository.ensureStockRow(implementUuid);

        if (context.itemType() == StockItemType.INDIVIDUAL) {
            List<String> normalizedCodes = normalizeAssetCodes(assetCodes, qty);
            try {
                repository.createIndividuals(implementUuid, context.locationUuid(), normalizedCodes);
            } catch (DataIntegrityViolationException ex) {
                throw new ConflictException("INDIVIDUAL_ASSET_CODE_DUPLICATE", "Uno o mas codigos de activo ya existen");
            }
        }

        repository.updateStock(implementUuid, qty, qty, 0, 0, 0);
        outboxService.enqueue("implement", implementUuid, "StockEntryAdded", null, java.util.Map.of("quantity", qty));

        return getStockDetail(implementUuid);
    }

    @Transactional
    public StockDetail applyMovement(UUID implementUuid, String movementTypeRaw, Integer quantity, List<UUID> individualUuids, String conditionRaw) {
        var context = requireContext(implementUuid);
        StockMovementType movementType = StockMovementType.fromLiteral(movementTypeRaw)
                .orElseThrow(() -> new BadRequestException(
                        "STOCK_MOVEMENT_TYPE_INVALID",
                        "movement_type invalido. Usa increase_available, decrease_available, reserve, release_reserve, loan, return, damage o repair (o legacy INGRESO, AJUSTE, EGRESO)"
                ));

        repository.ensureStockRow(implementUuid);

        if (context.itemType() == StockItemType.INDIVIDUAL) {
            applyMovementForIndividualImplement(context, movementType, individualUuids, conditionRaw);
        } else {
            int qty = requirePositiveQuantity(quantity);
            applyMovementDelta(context.implementUuid(), movementType, qty);
        }
        outboxService.enqueue("implement", implementUuid, "StockMovementApplied", null, java.util.Map.of("movement_type", movementType.literal()));

        return getStockDetail(implementUuid);
    }

    @Transactional
    public StockDetail updateIndividual(UUID implementUuid, UUID individualUuid, String statusRaw, String conditionRaw, UUID locationUuid, Boolean active) {
        var context = requireContext(implementUuid);
        if (context.itemType() != StockItemType.INDIVIDUAL) {
            throw new BadRequestException("INDIVIDUAL_NOT_ALLOWED", "Solo los implementos de tipo individual tienen registros individuales");
        }

        String status = normalizeOptionalLiteral(statusRaw, VALID_INDIVIDUAL_STATUS, "INDIVIDUAL_STATUS_INVALID", "status invalido");
        String condition = normalizeOptionalLiteral(conditionRaw, VALID_INDIVIDUAL_CONDITION, "INDIVIDUAL_CONDITION_INVALID", "condition invalido");

        List<IndividualItem> items = repository.findActiveIndividualsByUuids(implementUuid, List.of(individualUuid));
        if (items.isEmpty()) {
            throw new NotFoundException("INDIVIDUAL_NOT_FOUND", "Individual no encontrado para el implemento");
        }

        repository.updateIndividualsState(List.of(individualUuid), status, condition, locationUuid, active);
        syncStockRowForIndividuals(implementUuid);
        outboxService.enqueue("implement", implementUuid, "StockIndividualUpdated", null, java.util.Map.of("individual_uuid", individualUuid.toString()));
        return getStockDetail(implementUuid);
    }

    private StockCounters deriveCountersForIndividuals(StockCounters current, List<IndividualItem> individuals) {
        int total = individuals.size();
        int available = 0;
        int reserved = 0;
        int loaned = 0;
        int damaged = 0;

        for (IndividualItem individual : individuals) {
            String status = individual.status() == null ? "" : individual.status();
            switch (status) {
                case "available" -> available++;
                case "blocked" -> reserved++;
                case "loaned" -> loaned++;
                case "damaged", "maintenance" -> damaged++;
                default -> {
                }
            }
        }

        return new StockCounters(total, current.minStock(), available, reserved, loaned, damaged);
    }

    private void syncStockRowForIndividuals(UUID implementUuid) {
        StockCounters current = repository.findStockByImplementUuid(implementUuid)
                .orElse(new StockCounters(0, 0, 0, 0, 0, 0));
        List<IndividualItem> individuals = repository.findActiveIndividualsByImplementUuid(implementUuid);
        StockCounters computed = deriveCountersForIndividuals(current, individuals);

        boolean isDifferent = safe(current.totalStock()) != safe(computed.totalStock())
                || safe(current.available()) != safe(computed.available())
                || safe(current.reserved()) != safe(computed.reserved())
                || safe(current.loaned()) != safe(computed.loaned())
                || safe(current.damaged()) != safe(computed.damaged());

        if (isDifferent) {
            repository.replaceStock(
                    implementUuid,
                    safe(computed.totalStock()),
                    safe(computed.available()),
                    safe(computed.reserved()),
                    safe(computed.loaned()),
                    safe(computed.damaged())
            );
        }
    }

    private void applyMovementForIndividualImplement(StockRepository.ImplementStockContext context, StockMovementType movementType, List<UUID> individualUuids, String conditionRaw) {
        List<UUID> uuids = normalizeIndividualUuids(individualUuids);
        List<IndividualItem> selected = repository.findActiveIndividualsByUuids(context.implementUuid(), uuids);
        if (selected.size() != uuids.size()) {
            throw new BadRequestException("INDIVIDUAL_SELECTION_INVALID", "Algunos individuales no pertenecen al implemento o no estan activos");
        }

        int qty = selected.size();
        String condition = normalizeOptionalLiteral(conditionRaw, VALID_INDIVIDUAL_CONDITION, "INDIVIDUAL_CONDITION_INVALID", "condition invalido");

        switch (movementType) {
            case INCREASE_AVAILABLE -> throw new BadRequestException("INDIVIDUAL_MOVEMENT_INVALID", "Para sumar stock individual usa /entries con asset_codes");
            case DECREASE_AVAILABLE -> {
                applyMovementDelta(context.implementUuid(), movementType, qty);
                repository.updateIndividualsState(uuids, "retired", condition == null ? "irreparable" : condition, null, false);
            }
            case RESERVE -> {
                applyMovementDelta(context.implementUuid(), movementType, qty);
                repository.updateIndividualsState(uuids, "blocked", condition, null, null);
            }
            case RELEASE_RESERVE -> {
                applyMovementDelta(context.implementUuid(), movementType, qty);
                repository.updateIndividualsState(uuids, "available", condition, null, null);
            }
            case LOAN -> {
                applyMovementDelta(context.implementUuid(), movementType, qty);
                repository.updateIndividualsState(uuids, "loaned", condition, null, null);
            }
            case RETURN -> {
                applyMovementDelta(context.implementUuid(), movementType, qty);
                repository.updateIndividualsState(uuids, "available", condition == null ? "good" : condition, context.locationUuid(), null);
            }
            case DAMAGE -> {
                applyMovementDelta(context.implementUuid(), movementType, qty);
                repository.updateIndividualsState(uuids, "damaged", condition == null ? "damaged_no_diagnosis" : condition, null, null);
            }
            case REPAIR -> {
                applyMovementDelta(context.implementUuid(), movementType, qty);
                repository.updateIndividualsState(uuids, "available", condition == null ? "good" : condition, null, null);
            }
        }
    }

    private void applyMovementDelta(UUID implementUuid, StockMovementType movementType, int qty) {
        StockCounters current = repository.findStockByImplementUuid(implementUuid)
                .orElse(new StockCounters(0, 0, 0, 0, 0, 0));

        int total = safe(current.totalStock());
        int available = safe(current.available());
        int reserved = safe(current.reserved());
        int loaned = safe(current.loaned());
        int damaged = safe(current.damaged());

        int totalDelta = 0;
        int availableDelta = 0;
        int reservedDelta = 0;
        int loanedDelta = 0;
        int damagedDelta = 0;

        switch (movementType) {
            case INCREASE_AVAILABLE -> {
                totalDelta = qty;
                availableDelta = qty;
            }
            case DECREASE_AVAILABLE -> {
                totalDelta = -qty;
                availableDelta = -qty;
            }
            case RESERVE -> {
                availableDelta = -qty;
                reservedDelta = qty;
            }
            case RELEASE_RESERVE -> {
                availableDelta = qty;
                reservedDelta = -qty;
            }
            case LOAN -> {
                availableDelta = -qty;
                loanedDelta = qty;
            }
            case RETURN -> {
                availableDelta = qty;
                loanedDelta = -qty;
            }
            case DAMAGE -> {
                availableDelta = -qty;
                damagedDelta = qty;
            }
            case REPAIR -> {
                availableDelta = qty;
                damagedDelta = -qty;
            }
        }

        int nextTotal = total + totalDelta;
        int nextAvailable = available + availableDelta;
        int nextReserved = reserved + reservedDelta;
        int nextLoaned = loaned + loanedDelta;
        int nextDamaged = damaged + damagedDelta;

        if (nextTotal < 0 || nextAvailable < 0 || nextReserved < 0 || nextLoaned < 0 || nextDamaged < 0) {
            throw new BadRequestException("STOCK_INSUFFICIENT", "El movimiento genera stock negativo");
        }

        if ((nextAvailable + nextReserved + nextLoaned + nextDamaged) > nextTotal) {
            throw new BadRequestException("STOCK_INVARIANT_BROKEN", "El movimiento rompe la invariante de stock");
        }

        repository.updateStock(implementUuid, totalDelta, availableDelta, reservedDelta, loanedDelta, damagedDelta);
    }

    private StockRepository.ImplementStockContext requireContext(UUID implementUuid) {
        var context = repository.findImplementContext(implementUuid)
                .orElseThrow(() -> new NotFoundException("IMPLEMENT_NOT_FOUND", "Implemento no encontrado"));

        if (!Boolean.TRUE.equals(context.active())) {
            throw new BadRequestException("IMPLEMENT_INACTIVE", "No se pueden gestionar stocks de un producto inactivo");
        }

        if (context.itemType() == null) {
            throw new BadRequestException("IMPLEMENT_ITEM_TYPE_MISSING", "El implemento no tiene item_type configurado");
        }

        return context;
    }

    private int requirePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("STOCK_QUANTITY_INVALID", "quantity debe ser un entero positivo");
        }
        return quantity;
    }

    private List<String> normalizeAssetCodes(List<String> assetCodes, int quantity) {
        if (assetCodes == null || assetCodes.size() != quantity) {
            throw new BadRequestException("INDIVIDUAL_ASSET_CODES_INVALID", "asset_codes debe tener la misma cantidad que quantity");
        }

        List<String> normalized = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String code : assetCodes) {
            String candidate = code == null ? "" : code.trim();
            if (candidate.isEmpty()) {
                throw new BadRequestException("INDIVIDUAL_ASSET_CODE_EMPTY", "asset_codes no puede incluir vacios");
            }
            if (!seen.add(candidate.toLowerCase())) {
                throw new BadRequestException("INDIVIDUAL_ASSET_CODE_DUPLICATE_IN_REQUEST", "asset_codes no puede incluir duplicados");
            }
            normalized.add(candidate);
        }

        return normalized;
    }

    private List<UUID> normalizeIndividualUuids(List<UUID> individualUuids) {
        if (individualUuids == null || individualUuids.isEmpty()) {
            throw new BadRequestException("INDIVIDUAL_IDS_REQUIRED", "individual_uuids es obligatorio para implementos individuales");
        }

        Set<UUID> unique = new HashSet<>();
        for (UUID uuid : individualUuids) {
            if (uuid == null) {
                throw new BadRequestException("INDIVIDUAL_UUID_INVALID", "individual_uuids contiene valores invalidos");
            }
            unique.add(uuid);
        }
        return new ArrayList<>(unique);
    }

    private String normalizeOptionalLiteral(String raw, Set<String> allowed, String code, String message) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return null;
        }
        if (!allowed.contains(normalized)) {
            throw new BadRequestException(code, message);
        }
        return normalized;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
