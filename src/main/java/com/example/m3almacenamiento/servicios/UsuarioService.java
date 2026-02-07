package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.UsuarioMapper;
import com.example.m3almacenamiento.modelo.DTO.request.UsuarioRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.UsuarioResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_USUARIO;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final BauleraService bauleraService;

    @CacheEvict(value = "dashboard", allEntries = true)
    public UsuarioResponse crear(UsuarioRequest request){
        if(usuarioRepositorio.existsByEmail(request.getEmail())){
            throw new RuntimeException("Usuario con este mail ya existe");
        }

        if(usuarioRepositorio.existsByDni(request.getDni())){
            throw new RuntimeException("usuario con Dni: "+ request.getDni() +" ya existe");
        }

        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado(ESTADO_USUARIO.activo);
        usuario.setFechaCreacion(new Date());

        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    public PaginacionResponse<UsuarioResponse> obtenerTodosPaginados(Integer pagina, Integer tamanio, String sortBy){
        List<String> casosPermitidos = Arrays.asList("idUsuario", "nombreCompleto", "email", "deudaAcumulada");

        if(!casosPermitidos.contains(sortBy)){
            sortBy = "idUsuario";
        }

        Pageable pageable = PageRequest.of(pagina -1, tamanio, Sort.by(sortBy).descending());
        Page<Usuario> paginaUsuarios = usuarioRepositorio.findAll(pageable);

        return getUsuarioResponsePaginacionResponse(pagina, tamanio, paginaUsuarios);
    }

    public PaginacionResponse<UsuarioResponse> obtenerPaginadoConFiltro(Integer pagina, Integer tamanio, String sortBy, String filter){
        Pageable pageable = PageRequest.of(pagina-1, tamanio, Sort.by(sortBy).descending());
        Page<Usuario> paginaUsuarios = usuarioRepositorio.findBySearch(filter,pageable);

        return getUsuarioResponsePaginacionResponse(pagina, tamanio, paginaUsuarios);
    }

    private PaginacionResponse<UsuarioResponse> getUsuarioResponsePaginacionResponse(Integer pagina, Integer tamanio, Page<Usuario> paginaUsuarios) {
        List<UsuarioResponse> contenido = paginaUsuarios.getContent()
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());

        return PaginacionResponse.<UsuarioResponse>builder()
                .contenido(contenido)
                .pagina(pagina)
                .tamanio(tamanio)
                .totalElementos(paginaUsuarios.getTotalElements())
                .totalPaginas(paginaUsuarios.getTotalPages())
                .esUltima(paginaUsuarios.isLast())
                .esPrimera(paginaUsuarios.isFirst())
                .build();
    }

    public List<UsuarioResponse> obtenerTodos(){
        List<Usuario> usuarios = usuarioRepositorio.findAll();

        return usuarios
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UsuarioResponse obtenerPorId(Long id){
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(()-> new RuntimeException("Usuario no encontrado con ID: "+ id));
        return  usuarioMapper.toResponse(usuario);
    }

    @CacheEvict(value = "dashboard", allEntries = true)
    public UsuarioResponse actualizar(UsuarioRequest request, Long id){
        Usuario usuarioExistente = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: "+ id));

        if(request.getEmail() != null &&
            !request.getEmail().equals(usuarioExistente.getEmail()) &&
                usuarioRepositorio.existsByEmail(request.getEmail())){
            throw new RuntimeException("Usuario con email ya existe");
        }

        Usuario usuarioActualizado = usuarioMapper.toEntity(request);

        usuarioActualizado.setIdUsuario(id);
        usuarioActualizado.setEstado(usuarioExistente.getEstado());
        usuarioActualizado.setFechaCreacion(usuarioExistente.getFechaCreacion());
        usuarioActualizado.setBauleras(usuarioExistente.getBauleras());

        if(request.getPassword() != null && !request.getPassword().trim().isEmpty()){
            usuarioActualizado.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }else{
            usuarioActualizado.setPasswordHash(usuarioExistente.getPasswordHash());
        }

        Usuario usuarioGuardado = usuarioRepositorio.save(usuarioActualizado);
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @CacheEvict(value = "dashboard", allEntries = true)
    public void eliminar(Long id){
        if(!usuarioRepositorio.existsById(id)){
            throw new RuntimeException("Usuario no encontrado con ID: "+ id);
        }

        Usuario usuario = usuarioRepositorio.findById(id).orElseThrow();
        if (usuario.getBauleras() != null && !usuario.getBauleras().isEmpty()) {
            //Setear a esas bauleras el usuario en null
            return;
        }
        usuarioRepositorio.deleteById(id);
    }

    @CacheEvict(value = "dashboard", allEntries = true)
    public UsuarioResponse darDeBaja(Long id){

        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(()-> new RuntimeException("Usuario no encontrado con ID: "+ id));
        List<Baulera> bauleras = usuario.getBauleras();
        List<Baulera> baulerasCopia = bauleraService.setUsuarioNull(bauleras,usuario );

        usuario.setEstado(ESTADO_USUARIO.inactivo);
        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);

        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @CacheEvict(value = "dashboard", allEntries = true)
    public UsuarioResponse darDeAlataUsuarioCreado (Long idUsuario){
        Usuario u = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(()-> new RuntimeException("Usuario no encontrado con ID: "+ idUsuario));
        u.setEstado(ESTADO_USUARIO.activo);

        Usuario usuarioGuardado = usuarioRepositorio.save(u);
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @CacheEvict(value = "dashboard", allEntries = true)
    public UsuarioResponse asignarBauleras(Long usuarioId, List<Long> idBauleras){
        //Metodo que depende de bauleraService
        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: "+ usuarioId));

        List<BauleraResponse> baulerasAsignadas = new ArrayList<>();

        for(Long b: idBauleras){
            baulerasAsignadas.add(bauleraService.asignarBaulera(b,usuarioId));
        }

        Usuario usuarioActualizado = usuarioRepositorio.save(usuario);

        return usuarioMapper.toResponse(usuarioActualizado);
    }

    @CacheEvict(value = "dashboard", allEntries = true)
    public UsuarioResponse reducirDeuda(Long idUsuario, Integer montoAReducir){
        if(montoAReducir == null || montoAReducir <= 0){
            log.error("Monto menor o igual a 0: {}", montoAReducir);
            throw new IllegalArgumentException("El monto a reducir debe ser mayor a 0");
        }

        Usuario u = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(()-> new RuntimeException("Usuario no encontrado con ID: "+ idUsuario));

        BigDecimal deudaAcumulada = u.getDeudaAcumulada();
        BigDecimal monto = BigDecimal.valueOf(montoAReducir);

        if(deudaAcumulada.compareTo(monto) < 0){
            log.error("Monto ({}) es mayor a la deuda ({})",monto,deudaAcumulada);
            throw new RuntimeException("Monto a reducir mayor a la deuda");
        }

        BigDecimal nuevaDeuda = deudaAcumulada.subtract(monto);
        u.setDeudaAcumulada(nuevaDeuda);

        Usuario usuarioActualizado = usuarioRepositorio.save(u);

        log.info("Deuda reducida para usuario {}: {} -> {}",
                idUsuario, deudaAcumulada, nuevaDeuda);

        return usuarioMapper.toResponse(usuarioActualizado);
    }
}
