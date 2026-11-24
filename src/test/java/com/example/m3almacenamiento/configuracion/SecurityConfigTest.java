package com.example.m3almacenamiento.configuracion;

import com.example.m3almacenamiento.controladores.BauleraController;
import com.example.m3almacenamiento.controladores.PagoController;
import com.example.m3almacenamiento.controladores.TipoBauleraController;
import com.example.m3almacenamiento.repositorios.PagoRepositorio;
import com.example.m3almacenamiento.servicios.BauleraService;
import com.example.m3almacenamiento.servicios.CustomUserDetailsService;
import com.example.m3almacenamiento.servicios.PagoService;
import com.example.m3almacenamiento.servicios.PdfGeneratorService;
import com.example.m3almacenamiento.servicios.TipoBauleraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the URL-based authorization rules in {@link SecurityConfig}.
 * Services are mocked: only the access decision is under test.
 */
@WebMvcTest(controllers = {BauleraController.class, TipoBauleraController.class, PagoController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class, SecurityConfigTest.NoCache.class})
class SecurityConfigTest {

    // The app class has @EnableCaching but the MVC slice has no cache auto-config
    @TestConfiguration
    static class NoCache {
        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Autowired private MockMvc mvc;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private BauleraService bauleraService;
    @MockitoBean private TipoBauleraService tipoBauleraService;
    @MockitoBean private PagoService pagoService;
    @MockitoBean private PdfGeneratorService pdfGeneratorService;
    @MockitoBean private PagoRepositorio pagoRepositorio;

    @Test
    void anonymousCannotReachAdminEndpoints() throws Exception {
        mvc.perform(get("/baulera/admin/obtenerTodos")).andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotReachBauleraAdminEndpoints() throws Exception {
        mvc.perform(get("/baulera/admin/obtenerTodos")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotReachTipoBauleraAdminEndpoints() throws Exception {
        mvc.perform(get("/tipo-baulera/admin/obtener-todos")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotReachPagos() throws Exception {
        mvc.perform(get("/pagos/admin/obtenerPaginados")).andExpect(status().isForbidden());
        mvc.perform(get("/pagos/pdf/descargar/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void clientsHaveNoAccessToTheSystem() throws Exception {
        // The old client routes are closed even for a valid USER token
        mvc.perform(get("/baulera/user/obtenerTodo/" + UUID.randomUUID())).andExpect(status().isForbidden());
        mvc.perform(get("/tipo-baulera/user/obtener-id/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    void publicSelfRegistrationIsClosed() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReachAdminEndpoints() throws Exception {
        mvc.perform(get("/baulera/admin/obtenerTodos")).andExpect(status().isOk());
        mvc.perform(get("/tipo-baulera/admin/obtener-todos")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SEMIADMIN")
    void semiadminCanReadBaulerasAndTipos() throws Exception {
        mvc.perform(get("/baulera/semiadmin/obtenerTodo")).andExpect(status().isOk());
        mvc.perform(get("/tipo-baulera/admin/obtener-todos")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SEMIADMIN")
    void semiadminCannotWrite() throws Exception {
        mvc.perform(post("/tipo-baulera/admin/new-tipo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/baulera/admin/obtenerTodos")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unknownPathsAreDenied() throws Exception {
        mvc.perform(get("/no-existe")).andExpect(status().isForbidden());
    }
}
