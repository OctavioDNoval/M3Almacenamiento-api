package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.request.UsuarioRequest;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.UsuarioResponse;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import com.example.m3almacenamiento.servicios.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {
    private  final UsuarioService usuarioService;

    @GetMapping("/admin/getAll")
    public ResponseEntity<List<UsuarioResponse>> getAll(){
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/admin/getPagina")
    public ResponseEntity<PaginacionResponse<UsuarioResponse>> obtenerTodosPaginado(
            @RequestParam(defaultValue = "1") Integer pagina,
            @RequestParam(defaultValue = "15") Integer tamanio,
            @RequestParam(defaultValue = "idUsuario") String sortBy,
            @RequestParam(defaultValue = "") String filter
    ){
        log.info("📥 ENDPOINT LLAMADO - Parámetros recibidos:");
        log.info("   pagina: {}", pagina);
        log.info("   tamanio: {}", tamanio);
        log.info("   sortBy: '{}'", sortBy);
        log.info("   search: '{}'", filter);

        PaginacionResponse paginaResponse = new PaginacionResponse();

        if(filter == null || filter.trim().isEmpty()){
            paginaResponse = usuarioService.obtenerTodosPaginados(pagina, tamanio, sortBy);
        }else{
            paginaResponse = usuarioService.obtenerPaginadoConFiltro(pagina, tamanio, sortBy, filter);
        }

        return ResponseEntity.ok(paginaResponse);
    }

    @PostMapping("/admin/alta/usuario")
    public ResponseEntity<UsuarioResponse> darDeAltaUsuario (@RequestBody UsuarioRequest usuarioRequest){
        return ResponseEntity.ok(usuarioService.crear(usuarioRequest));
    }

    @PatchMapping("/admin/alta/usuarioCreado/{idUsuario}")
    public ResponseEntity<UsuarioResponse> darDeAltaCreadoUsuario (@PathVariable Long idUsuario){
        return ResponseEntity.ok(usuarioService.darDeAlataUsuarioCreado(idUsuario));
    }

    @PatchMapping("/admin/baja/usuario/{idUsuario}")
    public ResponseEntity<UsuarioResponse> darDeBajaUsuario (@PathVariable Long idUsuario){
        return ResponseEntity.ok(usuarioService.darDeBaja(idUsuario));
    }

    @PatchMapping("/admin/asignarBauleras/{idUsuario}")
    public ResponseEntity<UsuarioResponse> asginarBauleras (@PathVariable Long idUsuario,
                                                            @RequestBody List<Long> idBauleras){
        return ResponseEntity.ok(usuarioService.asignarBauleras(idUsuario, idBauleras));
    }

}
