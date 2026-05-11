package com.panol_project.backendpanol.modules.catalog.implement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.panol_project.backendpanol.modules.catalog.category.application.CategoriaService;
import com.panol_project.backendpanol.modules.catalog.category.domain.Categoria;
import com.panol_project.backendpanol.modules.catalog.category.domain.CategoriaRepository;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementRepository;
import com.panol_project.backendpanol.modules.catalog.implement.domain.Implemento;
import com.panol_project.backendpanol.modules.catalog.implement.domain.StockStatusFilter;
import com.panol_project.backendpanol.modules.catalog.location.application.LocationService;
import com.panol_project.backendpanol.modules.catalog.location.domain.LocationRepository;
import com.panol_project.backendpanol.shared.error.BadRequestException;
import com.panol_project.backendpanol.shared.error.NotFoundException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ImplementServiceTest {

    @Mock
    private ImplementRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private LocationRepository locationRepository;

    private ImplementService service;

    @BeforeEach
    void setUp() {
        var categoriaService = new CategoriaService(categoriaRepository);
        var locationService = new LocationService(locationRepository);
        service = new ImplementService(repository, categoriaService, locationService);
    }

    @Test
    void crearDebeValidarLocationYActualizarStockMinimo() {
        OffsetDateTime now = OffsetDateTime.now();
        UUID implementUuid = UUID.randomUUID();
        UUID categoryUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        Implemento created = new Implemento(implementUuid, "Guantes", null, categoryUuid, locationUuid, ImplementItemType.REUSABLE, null, null, null, true, now, now);

        when(categoriaRepository.findActiveByUuid(categoryUuid)).thenReturn(Optional.of(new Categoria(categoryUuid, "Cat", null, true, now)));
        when(locationRepository.existsByUuid(locationUuid)).thenReturn(true);
        when(repository.create("Guantes", null, categoryUuid, locationUuid, ImplementItemType.REUSABLE, null, null, null)).thenReturn(created);
        when(repository.updateMinStockByImplementUuid(implementUuid, 3)).thenReturn(1);

        Implemento result = service.crear("Guantes", null, categoryUuid, locationUuid, "reusable", 3, " ", null, null);

        assertEquals(implementUuid, result.uuid());
        verify(categoriaRepository).findActiveByUuid(categoryUuid);
        verify(locationRepository).existsByUuid(locationUuid);
        verify(repository).updateMinStockByImplementUuid(implementUuid, 3);
    }

    @Test
    void crearDebeFallarSiCategoriaInactivaONoExiste() {
        UUID categoryUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        when(categoriaRepository.findActiveByUuid(categoryUuid)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () ->
                service.crear("Guantes", null, categoryUuid, locationUuid, "consumable", 5, null, null, null));
        verify(repository, never()).create(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void crearDebeFallarSiItemTypeNoEsValido() {
        OffsetDateTime now = OffsetDateTime.now();
        UUID categoryUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        when(categoriaRepository.findActiveByUuid(categoryUuid)).thenReturn(Optional.of(new Categoria(categoryUuid, "Cat", null, true, now)));
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crear("Guantes", null, categoryUuid, locationUuid, "otro", 5, null, null, null));

        assertEquals("IMPLEMENT_ITEM_TYPE_INVALID", ex.getCode());
    }

    @Test
    void crearDebeFallarConBadRequestSiNombreActivoYaExiste() {
        OffsetDateTime now = OffsetDateTime.now();
        UUID categoryUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        when(categoriaRepository.findActiveByUuid(categoryUuid)).thenReturn(Optional.of(new Categoria(categoryUuid, "Cat", null, true, now)));
        when(locationRepository.existsByUuid(locationUuid)).thenReturn(true);
        when(repository.existsActiveByNameIgnoreCase("Guantes")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crear("Guantes", null, categoryUuid, locationUuid, "consumable", 4, null, null, null));

        assertEquals("IMPLEMENT_NAME_DUPLICATE", ex.getCode());
    }

    @Test
    void crearDebeRetornarBadRequestSiNombreDuplicadoPorConstraintUnico() {
        OffsetDateTime now = OffsetDateTime.now();
        UUID categoryUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        when(categoriaRepository.findActiveByUuid(categoryUuid)).thenReturn(Optional.of(new Categoria(categoryUuid, "Cat", null, true, now)));
        when(locationRepository.existsByUuid(locationUuid)).thenReturn(true);
        when(repository.create("Guantes", null, categoryUuid, locationUuid, ImplementItemType.CONSUMABLE, null, null, null))
                .thenThrow(new DataIntegrityViolationException("unique violation", new SQLException("duplicate key", "23505")));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.crear("Guantes", null, categoryUuid, locationUuid, "consumable", 3, null, null, null));

        assertEquals("IMPLEMENT_NAME_DUPLICATE", ex.getCode());
    }

    @Test
    void editarDebeFallarSiImplementoNoExiste() {
        UUID implementUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        when(repository.findByUuid(implementUuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.editar(implementUuid, "X", null, null, locationUuid, "reusable", 1, null, null, null));
    }

    @Test
    void editarDebeValidarCategoriaSiExisteImplemento() {
        OffsetDateTime now = OffsetDateTime.now();
        UUID implementUuid = UUID.randomUUID();
        UUID categoryUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        Implemento existing = new Implemento(implementUuid, "Existente", null, categoryUuid, locationUuid, ImplementItemType.REUSABLE, null, null, null, true, now, now);
        Implemento updated = new Implemento(implementUuid, "Nuevo", null, categoryUuid, locationUuid, ImplementItemType.REUSABLE, "Obs", null, null, true, now, now);

        when(categoriaRepository.findActiveByUuid(categoryUuid)).thenReturn(Optional.of(new Categoria(categoryUuid, "Cat", null, true, now)));
        when(locationRepository.existsByUuid(locationUuid)).thenReturn(true);
        when(repository.findByUuid(implementUuid)).thenReturn(Optional.of(existing));
        when(repository.update(implementUuid, "Nuevo", null, categoryUuid, locationUuid, ImplementItemType.REUSABLE, "Obs", null, null)).thenReturn(updated);
        when(repository.updateMinStockByImplementUuid(implementUuid, 1)).thenReturn(1);

        Implemento result = service.editar(implementUuid, "Nuevo", null, categoryUuid, locationUuid, "reusable", 1, "Obs", null, null);

        assertEquals(categoryUuid, result.categoriaUuid());
        verify(categoriaRepository).findActiveByUuid(categoryUuid);
        verify(locationRepository).existsByUuid(locationUuid);
        verify(repository).updateMinStockByImplementUuid(implementUuid, 1);
    }

    @Test
    void editarDebeFallarSiImplementoEstaInactivo() {
        OffsetDateTime now = OffsetDateTime.now();
        UUID implementUuid = UUID.randomUUID();
        UUID categoryUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        Implemento existing = new Implemento(implementUuid, "Existente", null, categoryUuid, locationUuid, ImplementItemType.REUSABLE, null, null, null, false, now, now);
        when(repository.findByUuid(implementUuid)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.editar(implementUuid, "Nuevo", null, categoryUuid, locationUuid, "reusable", 1, null, null, null));

        assertEquals("IMPLEMENT_INACTIVE", ex.getCode());
        verify(repository, never()).update(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void editarDebeFallarConBadRequestSiNombreActivoExisteEnOtroImplemento() {
        OffsetDateTime now = OffsetDateTime.now();
        UUID implementUuid = UUID.randomUUID();
        UUID categoryUuid = UUID.randomUUID();
        UUID locationUuid = UUID.randomUUID();
        Implemento existing = new Implemento(implementUuid, "Existente", null, categoryUuid, locationUuid, ImplementItemType.REUSABLE, null, null, null, true, now, now);
        when(categoriaRepository.findActiveByUuid(categoryUuid)).thenReturn(Optional.of(new Categoria(categoryUuid, "Cat", null, true, now)));
        when(locationRepository.existsByUuid(locationUuid)).thenReturn(true);
        when(repository.findByUuid(implementUuid)).thenReturn(Optional.of(existing));
        when(repository.existsActiveByNameIgnoreCaseAndUuidNot("Guantes", implementUuid)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.editar(implementUuid, "Guantes", null, categoryUuid, locationUuid, "reusable", 1, null, null, null));

        assertEquals("IMPLEMENT_NAME_DUPLICATE", ex.getCode());
    }

    @Test
    void listarDebeAplicarFiltrosCombinados() {
        UUID categoryUuid = UUID.randomUUID();
        when(repository.findAllSummaries("Guante", categoryUuid, null)).thenReturn(java.util.List.of());

        service.listar("  Guante ", categoryUuid, null);

        verify(repository).findAllSummaries("Guante", categoryUuid, null);
    }

    @Test
    void listarConFiltroBlockedDebePropagarsAlRepositorio() {
        when(repository.findAllSummaries(null, null, StockStatusFilter.BLOCKED)).thenReturn(java.util.List.of());

        service.listar(null, null, StockStatusFilter.BLOCKED);

        verify(repository).findAllSummaries(null, null, StockStatusFilter.BLOCKED);
    }
}
