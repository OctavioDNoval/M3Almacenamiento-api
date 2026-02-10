package com.example.m3almacenamiento.configuracion.audit;

import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuditUtils {
    @PersistenceContext
    private EntityManager entityManager;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AuditorAware<String> auditorAware;

    public AuditUtils(AuditorAware<String> auditorAware,
                      UsuarioRepositorio usuarioRepositorio) {
        this.auditorAware = auditorAware;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Transactional
    public void setCurrentUserForAudit() {
        String username = auditorAware.getCurrentAuditor().orElse("SISTEMA");
        Long idUsuario;

        Usuario u = usuarioRepositorio.findByEmail(username).orElse(null);

        if (u != null) {
            idUsuario = u.getIdUsuario();
        } else {
            Usuario sistemaUser = usuarioRepositorio.findByEmail("SISTEMA@SISTEMA.com")
                    .orElseGet(() -> {

                        Usuario nuevoSistema = new Usuario();
                        nuevoSistema.setEmail("SISTEMA@SISTEMA.com");
                        nuevoSistema.setNombreCompleto("Sistema Automático");

                        return usuarioRepositorio.save(nuevoSistema);
                    });
            idUsuario = sistemaUser.getIdUsuario();
        }

        // Establecer la variable de sesión CORRECTAMENTE
        Query query = entityManager.createNativeQuery("SET @usuario_actual_id = :idUsuario");
        query.setParameter("idUsuario", idUsuario); // ← PARÁMETRO CORREGIDO
        query.executeUpdate();

        // Depuración (opcional, para verificar)
        Query checkQuery = entityManager.createNativeQuery("SELECT @usuario_actual_id");
        Long idEstablecido = ((Number) checkQuery.getSingleResult()).longValue();
        System.out.println("DEBUG: Variable @usuario_actual_id establecida a: " + idEstablecido);
    }
}
