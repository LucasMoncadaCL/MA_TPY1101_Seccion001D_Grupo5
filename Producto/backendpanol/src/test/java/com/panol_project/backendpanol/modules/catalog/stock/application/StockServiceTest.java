package com.panol_project.backendpanol.modules.catalog.stock.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.panol_project.backendpanol.modules.catalog.stock.domain.IndividualItem;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockCounters;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockDetail;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockItemType;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockRepository;
import com.panol_project.backendpanol.shared.outbox.application.OutboxService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository repository;

    @Mock
    private OutboxService outboxService;

    @Test
    void getStockDetailDebeUsarStockItemTypeLocalYDerivarContadoresDeIndividuales() {
        UUID implementUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        when(repository.findImplementContext(implementUuid))
                .thenReturn(Optional.of(new StockRepository.ImplementStockContext(
                        implementUuid,
                        locationUuid,
                        StockItemType.INDIVIDUAL,
                        true
                )));
        when(repository.findStockByImplementUuid(implementUuid))
                .thenReturn(Optional.of(new StockCounters(0, 2, 0, 0, 0, 0)));
        when(repository.findActiveIndividualsByImplementUuid(implementUuid))
                .thenReturn(List.of(
                        new IndividualItem(UUID.randomUUID(), implementUuid, "A1", "available", "good", locationUuid, true),
                        new IndividualItem(UUID.randomUUID(), implementUuid, "A2", "blocked", "good", locationUuid, true),
                        new IndividualItem(UUID.randomUUID(), implementUuid, "A3", "damaged", "damaged_no_diagnosis", locationUuid, true)
                ));

        StockService service = new StockService(repository, outboxService);
        StockDetail detail = service.getStockDetail(implementUuid);

        assertEquals(StockItemType.INDIVIDUAL, detail.itemType());
        assertEquals(3, detail.stock().totalStock());
        assertEquals(1, detail.stock().available());
        assertEquals(1, detail.stock().reserved());
        assertEquals(1, detail.stock().damaged());
    }
}
