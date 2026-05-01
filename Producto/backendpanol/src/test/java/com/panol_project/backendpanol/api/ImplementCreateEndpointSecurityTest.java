package com.panol_project.backendpanol.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panol_project.backendpanol.bootstrap.config.SecurityConfig;
import com.panol_project.backendpanol.modules.catalog.implement.api.ImplementController;
import com.panol_project.backendpanol.modules.catalog.implement.api.dto.CreateImplementRequest;
import com.panol_project.backendpanol.modules.catalog.category.application.CategoriaService;
import com.panol_project.backendpanol.modules.catalog.category.domain.Categoria;
import com.panol_project.backendpanol.modules.catalog.category.domain.CategoriaRepository;
import com.panol_project.backendpanol.modules.catalog.implement.application.ImplementService;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementCategorySummary;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementLocationSummary;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementSummary;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementStockSummary;
import com.panol_project.backendpanol.modules.catalog.implement.domain.Implemento;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementRepository;
import com.panol_project.backendpanol.modules.catalog.location.application.LocationService;
import com.panol_project.backendpanol.modules.catalog.location.domain.LocationRepository;
import com.panol_project.backendpanol.shared.error.security.RestAccessDeniedHandler;
import com.panol_project.backendpanol.shared.error.security.RestAuthenticationEntryPoint;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ImplementController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        ImplementCreateEndpointSecurityTest.TestServicesConfiguration.class
})
@TestPropertySource(properties = "app.security.enabled=true")
class ImplementCreateEndpointSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImplementRepository implementRepository;

    @MockBean
    private CategoriaRepository categoriaRepository;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @TestConfiguration
    static class TestServicesConfiguration {
        @Bean
        ImplementService implementService(
                ImplementRepository implementRepository,
                CategoriaRepository categoriaRepository,
                LocationRepository locationRepository
        ) {
            CategoriaService categoriaService = new CategoriaService(categoriaRepository);
            LocationService locationService = new LocationService(locationRepository);
            return new ImplementService(implementRepository, categoriaService, locationService);
        }
    }

    @Test
    void postImplementsDebeRequerirJwt() throws Exception {
        mvc.perform(post("/api/implements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postImplementsDebeRechazarSinRolCoordinador() throws Exception {
        mvc.perform(post("/api/implements")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void postImplementsDebePermitirRolCoordinador() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();

        when(categoriaRepository.findActiveById(2))
                .thenReturn(Optional.of(new Categoria(2, "Categoria", null, true, now)));
        when(locationRepository.existsById(10)).thenReturn(true);
        when(implementRepository.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Implemento(
                        1,
                        "Guantes",
                        "Desc",
                        2,
                        10,
                        ImplementItemType.REUSABLE,
                        null,
                        null,
                        "Observacion",
                        true,
                        now,
                        now
                ));
        when(implementRepository.updateMinStockByImplementId(1, 3)).thenReturn(1);
        when(implementRepository.findMinStockByImplementId(1)).thenReturn(Optional.of(3));
        when(implementRepository.findSummaryById(1)).thenReturn(Optional.of(new ImplementSummary(
                1,
                "Guantes",
                "Desc",
                null,
                null,
                true,
                new ImplementCategorySummary(2, "Categoria", true),
                new ImplementLocationSummary(10, "Ubicacion", null),
                new ImplementStockSummary(null, 3, null, null, null, null)
        )));

        mvc.perform(post("/api/implements")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COORDINADOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.item_type").value("reusable"))
                .andExpect(jsonPath("$.min_stock").value(3));
    }

    @Test
    void postImplementsDebeRetornarBadRequestSiFaltaLocationId() throws Exception {
        String payloadSinLocation = """
                {
                  "name": "Guantes",
                  "description": "Desc",
                  "category_id": 2,
                  "item_type": "reusable",
                  "min_stock": 3,
                  "observations": "Obs"
                }
                """;

        mvc.perform(post("/api/implements")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COORDINADOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadSinLocation))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("La ubicacion es obligatoria")));

        verify(implementRepository, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getImplementDebeRetornarStockMinimoCuandoExiste() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        when(implementRepository.findById(7)).thenReturn(Optional.of(new Implemento(
                7,
                "Jeringa",
                "Desc",
                2,
                10,
                ImplementItemType.REUSABLE,
                null,
                null,
                null,
                true,
                now,
                now
        )));
        when(implementRepository.findMinStockByImplementId(7)).thenReturn(Optional.of(5));
        when(implementRepository.findSummaryById(7)).thenReturn(Optional.of(new ImplementSummary(
                7,
                "Jeringa",
                "Desc",
                null,
                null,
                true,
                new ImplementCategorySummary(2, "Categoria", true),
                new ImplementLocationSummary(10, "Ubicacion", null),
                new ImplementStockSummary(null, 5, null, null, null, null)
        )));

        mvc.perform(get("/api/implements/7")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COORDINADOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.min_stock").value(5));
    }

    @Test
    void putImplementsDebeRequerirJwt() throws Exception {
        mvc.perform(put("/api/implements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdatePayload()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putImplementsDebeRechazarSinRolCoordinador() throws Exception {
        mvc.perform(put("/api/implements/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdatePayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    void putImplementsDebePermitirRolCoordinador() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        when(implementRepository.findById(1)).thenReturn(Optional.of(new Implemento(
                1,
                "Guantes",
                "Desc",
                2,
                10,
                ImplementItemType.REUSABLE,
                null,
                null,
                "Obs",
                true,
                now,
                now
        )));
        when(categoriaRepository.findActiveById(2))
                .thenReturn(Optional.of(new Categoria(2, "Categoria", null, true, now)));
        when(locationRepository.existsById(10)).thenReturn(true);
        when(implementRepository.update(1, "Guantes", "Desc", 2, 10, ImplementItemType.REUSABLE, null, null, "Obs"))
                .thenReturn(new Implemento(
                        1,
                        "Guantes",
                        "Desc",
                        2,
                        10,
                        ImplementItemType.REUSABLE,
                        null,
                        null,
                        "Obs",
                        true,
                        now,
                        now
                ));
        when(implementRepository.updateMinStockByImplementId(1, 3)).thenReturn(1);
        when(implementRepository.findMinStockByImplementId(1)).thenReturn(Optional.of(3));
        when(implementRepository.findSummaryById(1)).thenReturn(Optional.of(new ImplementSummary(
                1,
                "Guantes",
                "Desc",
                null,
                null,
                true,
                new ImplementCategorySummary(2, "Categoria", true),
                new ImplementLocationSummary(10, "Ubicacion", null),
                new ImplementStockSummary(null, 3, null, null, null, null)
        )));

        mvc.perform(put("/api/implements/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COORDINADOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdatePayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.observations").value("Obs"))
                .andExpect(jsonPath("$.min_stock").value(3));
    }

    private CreateImplementRequest validRequest() {
        return new CreateImplementRequest(
                "Guantes",
                "Desc",
                2,
                10,
                "reusable",
                3,
                "Observacion",
                null,
                null
        );
    }

    private String validUpdatePayload() throws Exception {
        return """
                {
                  "name": "Guantes",
                  "description": "Desc",
                  "category_id": 2,
                  "location_id": 10,
                  "item_type": "reusable",
                  "min_stock": 3,
                  "observations": "Obs"
                }
                """;
    }
}
