package com.panol_project.backendpanol.modules.catalog.category.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.panol_project.backendpanol.modules.catalog.category.domain.Categoria;
import com.panol_project.backendpanol.modules.catalog.category.domain.CategoriaRepository;
import com.panol_project.backendpanol.shared.error.BadRequestException;
import com.panol_project.backendpanol.shared.error.ConflictException;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    private CategoriaService service;

    @BeforeEach
    void setUp() {
        service = new CategoriaService(repository);
    }

    @Test
    void crearDebeFallarConBadRequestSiNombreDuplicado() {
        when(repository.existsByNombre("Reactivos", null)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.crear("Reactivos", null));

        assertEquals("CATEGORY_NAME_DUPLICATE", ex.getCode());
        assertEquals("Ya existe una categoria con el nombre 'Reactivos'", ex.getMessage());
    }

    @Test
    void desactivarDebeRetornarConflictSiHayImplementsActivosSinForce() {
        Categoria record = new Categoria(10, "Reactivos", null, true, OffsetDateTime.now());

        when(repository.findById(10)).thenReturn(Optional.of(record));
        when(repository.countActiveImplementsByCategoryId(10)).thenReturn(3);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.desactivar(10, false));

        assertEquals("CATEGORY_HAS_ACTIVE_IMPLEMENTS", ex.getCode());
        verify(repository, never()).deactivate(any());
    }

    @Test
    void eliminarDebeFallarSiTieneImplementsAsociados() {
        Categoria record = new Categoria(7, "Insumos", null, true, OffsetDateTime.now());

        when(repository.findById(7)).thenReturn(Optional.of(record));
        when(repository.countImplementsByCategoryId(7)).thenReturn(2);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.eliminar(7));

        assertEquals("CATEGORY_HAS_IMPLEMENTS", ex.getCode());
        verify(repository, never()).deleteById(any());
    }

    @Test
    void validarCategoriaActivaParaImplementoDebeFallarSiInactiva() {
        when(repository.findActiveById(2)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.validarCategoriaActivaParaImplemento(2));

        assertEquals("CATEGORY_INACTIVE_OR_NOT_FOUND", ex.getCode());
    }

    @Test
    void desactivarConForceDebeDesactivarYRetornarCategoria() {
        Categoria active = new Categoria(5, "Activa", null, true, OffsetDateTime.now());

        when(repository.findById(5)).thenReturn(Optional.of(active));
        when(repository.countActiveImplementsByCategoryId(5)).thenReturn(1);

        Categoria response = service.desactivar(5, true);

        assertEquals(5, response.id());
        assertEquals(false, response.activa());
        verify(repository).deactivate(eq(5));
    }
}
