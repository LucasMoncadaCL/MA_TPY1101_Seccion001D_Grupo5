package com.panol_project.backendpanol.modules.catalog.implement.api;

import com.panol_project.backendpanol.modules.catalog.implement.api.dto.CreateImplementRequest;
import com.panol_project.backendpanol.modules.catalog.implement.api.dto.ImplementResponse;
import com.panol_project.backendpanol.modules.catalog.implement.api.dto.ImplementCategorySummaryResponse;
import com.panol_project.backendpanol.modules.catalog.implement.api.dto.ImplementLocationSummaryResponse;
import com.panol_project.backendpanol.modules.catalog.implement.api.dto.ImplementSummaryResponse;
import com.panol_project.backendpanol.modules.catalog.implement.api.dto.UpdateImplementRequest;
import com.panol_project.backendpanol.modules.catalog.implement.application.ImplementService;
import com.panol_project.backendpanol.modules.catalog.implement.domain.Implemento;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/implements")
public class ImplementController {

    private final ImplementService service;

    public ImplementController(ImplementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COORDINADOR')")
    ImplementResponse crear(@Valid @RequestBody CreateImplementRequest request) {
        Implemento created = service.crear(
                request.name(),
                request.description(),
                request.categoryId(),
                request.locationId(),
                request.itemType(),
                request.minStock(),
                request.observations()
        );
        var summary = service.obtenerSummary(created.id());
        return toResponse(created, summary, service.obtenerStockMinimo(created.id()), request.observations());
    }

    @PutMapping("/{id}")
    ImplementResponse editar(@PathVariable Integer id, @Valid @RequestBody UpdateImplementRequest request) {
        Implemento updated = service.editar(id, request.name(), request.description(), request.categoryId(), request.locationId());
        var summary = service.obtenerSummary(updated.id());
        Integer minStock = service.obtenerStockMinimo(updated.id());
        return toResponse(updated, summary, minStock, null);
    }

    @GetMapping("/{id}")
    ImplementResponse obtener(@PathVariable Integer id) {
        Implemento implemento = service.obtener(id);
        var summary = service.obtenerSummary(id);
        Integer minStock = service.obtenerStockMinimo(implemento.id());
        return toResponse(implemento, summary, minStock, null);
    }

    @GetMapping
    List<ImplementSummaryResponse> listar() {
        return service.listar().stream()
                .map(implemento -> new ImplementSummaryResponse(
                        implemento.id(),
                        implemento.name(),
                        implemento.category() == null
                                ? null
                                : new ImplementCategorySummaryResponse(
                                        implemento.category().id(),
                                        implemento.category().name(),
                                        implemento.category().active()
                                ),
                        new ImplementLocationSummaryResponse(
                                implemento.location().id(),
                                implemento.location().name(),
                                implemento.location().description()
                        )
                ))
                .toList();
    }

    private ImplementResponse toResponse(Implemento implemento) {
        // Fallback para casos donde no tengamos el summary (idealmente, siempre lo tenemos en GET/POST/PUT).
        return toResponse(implemento, null, null, null);
    }

    private ImplementResponse toResponse(
            Implemento implemento,
            com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementSummary summary,
            Integer minStock,
            String observations
    ) {
        return new ImplementResponse(
                implemento.id(),
                implemento.nombre(),
                implemento.descripcion(),
                implemento.itemType() == null ? null : implemento.itemType().literal(),
                summary == null || summary.category() == null
                        ? null
                        : new ImplementCategorySummaryResponse(
                                summary.category().id(),
                                summary.category().name(),
                                summary.category().active()
                        ),
                summary == null || summary.location() == null
                        ? null
                        : new ImplementLocationSummaryResponse(
                                summary.location().id(),
                                summary.location().name(),
                                summary.location().description()
                        ),
                implemento.categoriaId(),
                implemento.locationId(),
                minStock,
                observations,
                implemento.activo(),
                implemento.createdAt(),
                implemento.updatedAt()
        );
    }
}
