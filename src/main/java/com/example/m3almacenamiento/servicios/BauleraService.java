package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.BauleraMapper;
import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.TipoBauleraRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BauleraService {
    private final BauleraRepositorio  bauleraRepositorio;
    private final BauleraMapper bauleraMapper;
    private final TipoBauleraRepositorio tipoBauleraRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final EmailService emailService;

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "dashboard", allEntries = true)
    public BauleraResponse crear(BauleraRequest bauleraRequest){
        if(bauleraRepositorio.existsByNroBaulera(bauleraRequest.getNroBaulera().trim())){
            throw new RuntimeException("Baulera con NRO: "+bauleraRequest.getNroBaulera()+" ya existe");
        }

        Baulera baulera = bauleraMapper.toEntity(bauleraRequest);
        baulera.setEstadoBaulera(ESTADO_BAULERA.disponible);
        if(bauleraRequest.getIdUsuario()!=null){
            baulera.setFechaAsignacion(new Date());
            baulera.setEstadoBaulera(ESTADO_BAULERA.ocupada);
        }

        TipoBaulera tipoBaulera = tipoBauleraRepositorio.findById(bauleraRequest.getIdTipoBaulera())
                .orElse(null);
        if(tipoBaulera!=null){
            baulera.setTipoBaulera(tipoBaulera);
        }

        Baulera bauleraGuardada =  bauleraRepositorio.save(baulera);
        return bauleraMapper.toResponse(bauleraGuardada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "dashboard", allEntries = true)
    public List<BauleraResponse> crearDesdeNroBaulera(Integer cantidad, Long tipoBauleraId){
        Integer nroMax = bauleraRepositorio.findMaxNroBauleraAsInteger()
                .orElse(0);

        if(cantidad <= 0){
            throw new IllegalArgumentException("Cantidad debe ser mayor a 0");
        }
        if(cantidad > 50){
            throw new IllegalArgumentException("Cantidad debe ser menor a 50");
        }
        TipoBaulera tipoBaulera = null;
        if(tipoBauleraId!=null){
            tipoBaulera = tipoBauleraRepositorio.findById(tipoBauleraId).orElseThrow();
        }

        List<Baulera> bauleras = new ArrayList<>();
        for(int i=1;i<=cantidad;i++){
            Integer nuevoNumero = nroMax+i;
            String nuevoNumeroStr =  String.valueOf(nuevoNumero);

            if(tipoBauleraRepositorio.existsByTipoBauleraNombre(nuevoNumeroStr)){
                throw new RuntimeException("Tipo Baulera ya existe");
            }

            Baulera nuevaBaulera = new Baulera();
            nuevaBaulera.setEstadoBaulera(ESTADO_BAULERA.disponible);
            nuevaBaulera.setTipoBaulera(tipoBaulera);
            nuevaBaulera.setNroBaulera(nuevoNumeroStr);
            nuevaBaulera.setFechaAsignacion(null);

            bauleras.add(nuevaBaulera);
        }
        List<Baulera> baulerasGuardadas = bauleraRepositorio.saveAll(bauleras);
        return baulerasGuardadas
                .stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());

    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<BauleraResponse> obtenerTodos (){
        List<Baulera>  bauleras = bauleraRepositorio.findAll();

        return bauleras
                .stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<BauleraResponse> obtenerTodosDisponibles (){
        List<Baulera>  bauleras = bauleraRepositorio.findAllDisponible();
        return bauleras
                .stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BauleraResponse obtenerPorId(Long id){
        Baulera baulera = bauleraRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Baulera no encontrada"));

        return bauleraMapper.toResponse(baulera);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginacionResponse<BauleraResponse> obtenerTodosPaginados(Integer pagina, Integer tamanio, String sortBy, String direction) {
        Sort sort =  buildSort(sortBy, direction);
        Pageable pageable =  PageRequest.of(pagina - 1, tamanio, sort );
        Page<Baulera> page = bauleraRepositorio.findAll(pageable);
        return getBauleraResponsePaginacionResponse(pagina,tamanio,page);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginacionResponse<BauleraResponse> obtenerPaginadoConFiltro (Integer pagina, Integer tamanio, String sortBy, String filter, String direction ) {
        Sort sort = buildSort(sortBy, direction);
        Pageable pageable = PageRequest.of(pagina - 1, tamanio, sort );
        Page<Baulera> page = bauleraRepositorio.findBySearch(filter, pageable);
        return getBauleraResponsePaginacionResponse(pagina,tamanio,page);
    }

    private PaginacionResponse<BauleraResponse> getBauleraResponsePaginacionResponse(Integer pagina, Integer tamanio, Page<Baulera> paginaBaulera) {
        List<BauleraResponse> contenido = paginaBaulera.getContent()
                .stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());

        return PaginacionResponse.<BauleraResponse>builder()
                .contenido(contenido)
                .pagina(pagina)
                .tamanio(tamanio)
                .totalElementos(paginaBaulera.getTotalElements())
                .totalPaginas(paginaBaulera.getTotalPages())
                .esUltima(paginaBaulera.isLast())
                .esPrimera(paginaBaulera.isFirst())
                .build();
    }

    @PreAuthorize("hasRole('SEMIADMIN')")
    public List<BauleraResponse> obtenerBaulerasSemiAdmin (){
        List<Baulera> bauleras = bauleraRepositorio.findAllSemiAdmin();
        return bauleras.stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Sort buildSort(String sortBy, String direction){
        Map<String,String> mapeoCampos = Map.of(
                "idBaulera", "idBaulera",
                "nroBaulera", "nroBaulera",
                "tipoBauleraNombre", "tipoBaulera.tipoBauleraNombre",
                "estadoBaulera", "estadoBaulera",
                "nombreUsuario", "usuarioAsignado.nombreCompleto"
        );

        String campoReal = mapeoCampos.getOrDefault(sortBy, "idBaulera");
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir,campoReal);
    }

    @PreAuthorize("hasRole('ADMIN') or @preSecurityService.esMismoUsuario(#idUsuario)")
    public List<BauleraResponse> obtenerPorIdUsuario(Long idUsuario){
        List<Baulera> listaBauleras = bauleraRepositorio.findByUsuarioAsignado_IdUsuario(idUsuario);
        return listaBauleras
                .stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "dashboard", allEntries = true)
    public boolean eliminar (Long id){
        if(!bauleraRepositorio.existsById(id)){
            throw new RuntimeException("Baulera no encontrada");
        }

        Baulera baulera = bauleraRepositorio.findById(id).orElseThrow();
        if(baulera.getUsuarioAsignado()!=null){
            baulera.setUsuarioAsignado(null);
        }
        if(baulera.getTipoBaulera()!=null){
            baulera.setTipoBaulera(null);
        }
        bauleraRepositorio.delete(baulera);
        return true;
    }

    protected List<Baulera> setUsuarioNull(List<Baulera> bauleras, Usuario usuario){
        List<Baulera> copy =  new ArrayList<>(bauleras);
        for(Baulera baulera : copy){
            baulera.setUsuarioAsignado(null);
            baulera.setEstadoBaulera(ESTADO_BAULERA.disponible);
            usuario.getBauleras().remove(baulera);
            bauleraRepositorio.save(baulera);
        }
        return copy;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "dashboard", allEntries = true)
    public BauleraResponse asignarBaulera(Long idBaulera, Long idUsuario){
        System.out.println("Asignando Baulera "+idBaulera);
        Baulera baulera = bauleraRepositorio.findById(idBaulera)
                .orElseThrow(() -> new RuntimeException("Baulera no encontrada"));

        Usuario usuario = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        baulera.setUsuarioAsignado(usuario);
        baulera.setEstadoBaulera(ESTADO_BAULERA.ocupada);
        baulera.setFechaAsignacion(new Date());

        Baulera bauleraGuardada =  bauleraRepositorio.save(baulera);

        emailService.enviarNotificacionDeAsignacion(usuario,bauleraGuardada);

        return bauleraMapper.toResponse(bauleraGuardada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BauleraResponse desasignarBaulera(Long idBaulera){
        Baulera b = bauleraRepositorio.findById(idBaulera)
                .orElseThrow(()-> new RuntimeException("Baulera no encontrada"));

        b.setUsuarioAsignado(null);
        b.setEstadoBaulera(ESTADO_BAULERA.disponible);
        Baulera bauleraGuardada =  bauleraRepositorio.save(b);
        return bauleraMapper.toResponse(bauleraGuardada);
    }

}
