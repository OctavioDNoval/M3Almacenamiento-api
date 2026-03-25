package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.InfoDeudaEmail;


import com.example.m3almacenamiento.modelo.DTO.mapeo.RemitoMapper;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.RemitoResponse;
import com.example.m3almacenamiento.modelo.entidad.Remito;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.RemitoRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RemitoService {
    private final RemitoRepositorio remitoRepositorio;
    private final EmailService emailService;
    private final RemitoMapper remitoMapper;

    public PaginacionResponse<RemitoResponse> obtenerTodosPaginados(Integer pagina, Integer tamanio, String sortBy, String direction){
        Sort sort =  buildSort(sortBy, direction);
        Pageable pageable =  PageRequest.of(pagina - 1, tamanio, sort );
        Page<Remito> page = remitoRepositorio.findAll(pageable);
        return getRemitoResponsePaginacionResponse(pagina,tamanio,page);
    }

    public PaginacionResponse<RemitoResponse> obtenerPaginadoConFiltro(Integer pagina, Integer tamanio, String sortBy, String filter, String direction){
        Sort sort = buildSort(sortBy, direction);
        Pageable pageable = PageRequest.of(pagina - 1, tamanio, sort );
        Page<Remito> page = remitoRepositorio.findBySearch(filter, pageable);
        return getRemitoResponsePaginacionResponse(pagina,tamanio,page);
    }
    private PaginacionResponse<RemitoResponse> getRemitoResponsePaginacionResponse(Integer pagina, Integer tamanio, Page<Remito> paginaRemito) {
        List<RemitoResponse> contenido = paginaRemito.getContent()
                .stream()
                .map(remitoMapper::toResponse)
                .collect(Collectors.toList());

        return PaginacionResponse.<RemitoResponse>builder()
                .contenido(contenido)
                .pagina(pagina)
                .tamanio(tamanio)
                .totalElementos(paginaRemito.getTotalElements())
                .totalPaginas(paginaRemito.getTotalPages())
                .esUltima(paginaRemito.isLast())
                .esPrimera(paginaRemito.isFirst())
                .build();
    }

    private Sort buildSort(String sortBy, String direction){
        Map<String,String> mapeoCampos = Map.of(
                "idRemito", "idRemito",
                "periodo", "periodo",
                "nombreUsuario", "usuario.nombreCompleto"
        );

        String campoReal = mapeoCampos.getOrDefault(sortBy, "idUsuario");
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir,campoReal);
    }


    public void generarRemito(Usuario usuario, InfoDeudaEmail infoDeudaEmail){
        Remito remito = new Remito();
        remito.setUsuario(usuario);
        remito.setPeriodo(emailService.getNombreMesActual());
        remito.setFechaEmision(LocalDate.now());
        remito.setImporteTotal(infoDeudaEmail.getTotalCalculado());

        String bauleras = "";
        List<String> listaBauleras = infoDeudaEmail.getNumerosBauleras();
        for(int i = 0; i < listaBauleras.size(); i++){
            bauleras = bauleras.concat(listaBauleras.get(i));  
            if(i != listaBauleras.size() - 1){
                bauleras = bauleras.concat(",");  
            }
        }

        remito.setBaulerasString(bauleras);
        remitoRepositorio.save(remito);
    }
}
