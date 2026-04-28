package com.panol_project.backendpanol.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panol_project.backendpanol.bootstrap.config.SecurityConfig;
import com.panol_project.backendpanol.modules.catalog.implement.api.ImplementController;
import com.panol_project.backendpanol.modules.catalog.implement.api.dto.CreateImplementRequest;
import com.panol_project.backendpanol.modules.catalog.implement.application.ImplementService;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import com.panol_project.backendpanol.modules.catalog.implement.domain.Implemento;
import com.panol_project.backendpanol.shared.error.security.RestAccessDeniedHandler;
import com.panol_project.backendpanol.shared.error.security.RestAuthenticationEntryPoint;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ImplementController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties = "app.security.enabled=true")
class ImplementCreateEndpointSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImplementService service;

    @MockBean
    private JwtDecoder jwtDecoder;

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
        when(service.crear(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Implemento(
                        1,
                        "Guantes",
                        "Desc",
                        2,
                        10,
                        ImplementItemType.REUSABLE,
                        true,
                        now,
                        now
                ));

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

        verify(service, never()).crear(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getImplementDebeRetornarStockMinimoCuandoExiste() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        when(service.obtener(7)).thenReturn(new Implemento(
                7,
                "Jeringa",
                "Desc",
                2,
                10,
                ImplementItemType.REUSABLE,
                true,
                now,
                now
        ));
        when(service.obtenerStockMinimo(7)).thenReturn(5);

        mvc.perform(get("/api/implements/7")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COORDINADOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.min_stock").value(5));
    }

    private CreateImplementRequest validRequest() {
        return new CreateImplementRequest(
                "Guantes",
                "Desc",
                2,
                10,
                "reusable",
                3,
                "Observacion"
        );
    }
}
