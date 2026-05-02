package com.panol_project.backendpanol.modules.catalog.stock.application;

import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import com.panol_project.backendpanol.modules.catalog.stock.domain.IndividualItem;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockCounters;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockDetail;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockMovementType;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockRepository;
import com.panol_project.backendpanol.shared.error.BadRequestException;
import com.panol_project.backendpanol.shared.error.ConflictException;
import com.panol_project.backendpanol.shared.error.NotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public StockDetail getStockDetail(Integer implementId) {
        var context = requireContext(implementId);

        StockCounters counters = repository.findStockByImplementId(implementId)
                .orElse(new StockCounters(0, 0, 0, 0, 0, 0));

        List<IndividualItem> individuals = context.itemType() == ImplementItemType.INDIVIDUAL
                ? repository.findActiveIndividualsByImplementId(implementId)
                : List.of();

        if (context.itemType() == ImplementItemType.INDIVIDUAL) {
            counters = deriveCountersForIndividuals(counters, individuals);
        }

        return new StockDetail(implementId, context.itemType(), counters, individuals);
    }

    @Transactional
    public StockDetail addEntry(Integer implementId, Integer quantity, List<String> assetCodes) {
        var context = requireContext(implementId);
        int qty = requirePositiveQuantity(quantity);

        repository.ensureStockRow(implementId);

        if (context.itemType() == ImplementItemType.INDIVIDUAL) {
            List<String> normalizedCodes = normalizeAssetCodes(assetCodes, qty);
            try {
                repository.createIndividuals(implementId, context.locationId(), normalizedCodes);
            } catch (DataIntegrityViolationException ex) {
                throw new ConflictException("INDIVIDUAL_ASSET_CODE_DUPLICATE", "Uno o mas codigos de activo ya existen");
            }
        }

        repository.updateStock(implementId, qty, qty, 0, 0, 0);

        return getStockDetail(implementId);
    }

    @Transactional
    public StockDetail applyMovement(Integer implementId, String movementTypeRaw, Integer quantity, List<Integer> individualIds, String conditionRaw) {
        var context = requireContext(implementId);
        StockMovementType movementType = StockMovementType.fromLiteral(movementTypeRaw)
                .orElseThrow(() -> new BadRequestException(
                        "STOCK_MOVEMENT_TYPE_INVALID",
                        "movement_type invalido. Usa increase_available, decrease_available, reserve, release_reserve, loan, return, damage o repair"
                ));

        repository.ensureStockRow(implementId);

        if (context.itemType() == ImplementItemType.INDIVIDUAL) {
            applyMovementForIndividualImplement(context, movementType, individualIds, conditionRaw);
        } else {
            int qty = requirePositiveQuantity(quantity);
            applyMovementDelta(context.implementId(), movementType, qty);
        }

        return getStockDetail(implementId);
    }

    @Transactional
    public StockDetail updateIndividual(Integer implementId, Integer individualId, String statusRaw, String conditionRaw, Integer locationId, Boolean active) {
        var context = requireContext(implementId);
        if (context.itemType() != ImplementItemType.INDIVIDUAL) {
            throw new BadRequestException("INDIVIDUAL_NOT_ALLOWED", "Solo los implementos de tipo individual tienen registros individuales");
        }

        String status = normalizeOptionalLiteral(statusRaw, VALID_INDIVIDUAL_STATUS, "INDIVIDUAL_STATUS_INVALID", "status invalido");
        String condition = normalizeOptionalLiteral(conditionRaw, VALID_INDIVIDUAL_CONDITION, "INDIVIDUAL_CONDITION_INVALID", "condition invalido");

        List<IndividualItem> items = repository.findActiveIndividualsByIds(implementId, List.of(individualId));
        if (items.isEmpty()) {
            throw new NotFoundException("INDIVIDUAL_NOT_FOUND", "Individual no encontrado para el implemento");
        }

        repository.updateIndividualsState(List.of(individualId), status, condition, locationId, active);
        syncStockRowForIndividuals(implementId);
        return getStockDetail(implementId);
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
                    // estados no inventariables en KPI (por ejemplo retired) se mantienen solo en total.
                }
            }
        }

        return new StockCounters(total, current.minStock(), available, reserved, loaned, damaged);
    }

    private void syncStockRowForIndividuals(Integer implementId) {
        StockCounters current = repository.findStockByImplementId(implementId)
                .orElse(new StockCounters(0, 0, 0, 0, 0, 0));
        List<IndividualItem> individuals = repository.findActiveIndividualsByImplementId(implementId);
        StockCounters computed = deriveCountersForIndividuals(current, individuals);

        boolean isDifferent = safe(current.totalStock()) != safe(computed.totalStock())
                || safe(current.available()) != safe(computed.available())
                || safe(current.reserved()) != safe(computed.reserved())
                || safe(current.loaned()) != safe(computed.loaned())
                || safe(current.damaged()) != safe(computed.damaged());

        if (isDifferent) {
            repository.replaceStock(
                    implementId,
                    safe(computed.totalStock()),
                    safe(computed.available()),
                    safe(computed.reserved()),
                    safe(computed.loaned()),
                    safe(computed.damaged())
            );
        }
    }

    private void applyMovementForIndividualImplement(StockRepository.ImplementStockContext context, StockMovementType movementType, List<Integer> individualIds, String conditionRaw) {
        List<Integer> ids = normalizeIndividualIds(individualIds);
        List<IndividualItem> selected = repository.findActiveIndividualsByIds(context.implementId(), ids);
        if (selected.size() != ids.size()) {
            throw new BadRequestException("INDIVIDUAL_SELECTION_INVALID", "Algunos individuales no pertenecen al implemento o no estan activos");
        }

        int qty = selected.size();
        String condition = normalizeOptionalLiteral(conditionRaw, VALID_INDIVIDUAL_CONDITION, "INDIVIDUAL_CONDITION_INVALID", "condition invalido");

        switch (movementType) {
            case INCREASE_AVAILABLE -> throw new BadRequestException("INDIVIDUAL_MOVEMENT_INVALID", "Para sumar stock individual usa /entries con asset_codes");
            case DECREASE_AVAILABLE -> {
                applyMovementDelta(context.implementId(), movementType, qty);
                repository.updateIndividualsState(ids, "retired", condition == null ? "irreparable" : condition, null, false);
            }
            case RESERVE -> {
                applyMovementDelta(context.implementId(), movementType, qty);
                repository.updateIndividualsState(ids, "blocked", condition, null, null);
            }
            case RELEASE_RESERVE -> {
                applyMovementDelta(context.implementId(), movementType, qty);
                repository.updateIndividualsState(ids, "available", condition, null, null);
            }
            case LOAN -> {
                applyMovementDelta(context.implementId(), movementType, qty);
                repository.updateIndividualsState(ids, "loaned", condition, null, null);
            }
            case RETURN -> {
                applyMovementDelta(context.implementId(), movementType, qty);
                repository.updateIndividualsState(ids, "available", condition == null ? "good" : condition, context.locationId(), null);
            }
            case DAMAGE -> {
                applyMovementDelta(context.implementId(), movementType, qty);
                repository.updateIndividualsState(ids, "damaged", condition == null ? "damaged_no_diagnosis" : condition, null, null);
            }
            case REPAIR -> {
                applyMovementDelta(context.implementId(), movementType, qty);
                repository.updateIndividualsState(ids, "available", condition == null ? "good" : condition, null, null);
            }
        }
    }

    private void applyMovementDelta(Integer implementId, StockMovementType movementType, int qty) {
        StockCounters current = repository.findStockByImplementId(implementId)
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

        repository.updateStock(implementId, totalDelta, availableDelta, reservedDelta, loanedDelta, damagedDelta);
    }

    private StockRepository.ImplementStockContext requireContext(Integer implementId) {
        var context = repository.findImplementContext(implementId)
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

    private List<Integer> normalizeIndividualIds(List<Integer> individualIds) {
        if (individualIds == null || individualIds.isEmpty()) {
            throw new BadRequestException("INDIVIDUAL_IDS_REQUIRED", "individual_ids es obligatorio para implementos individuales");
        }

        Set<Integer> unique = new HashSet<>();
        for (Integer id : individualIds) {
            if (id == null || id <= 0) {
                throw new BadRequestException("INDIVIDUAL_ID_INVALID", "individual_ids contiene valores invalidos");
            }
            unique.add(id);
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
