package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.request.UsuarioRequest;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.UsuarioResponse;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import com.example.m3almacenamiento.servicios.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsuarioController {
    private  final UsuarioService usuarioService;

    @GetMapping("/admin/getAll")
    public ResponseEntity<List<UsuarioResponse>> getAll(){
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/admin/getPagina")
    public ResponseEntity<PaginacionResponse<UsuarioResponse>> obtenerTodosPaginado(
            @RequestParam(defaultValue = "1") Integer pagina,
            @RequestParam(defaultValue = "15") Integer tamanio
    ){
        PaginacionResponse<UsuarioResponse> paginaResponse = usuarioService
                .obtenerTodosPaginados(pagina, tamanio);

        return ResponseEntity.ok(paginaResponse);
    }

    @PostMapping("/admin/alta/usuario")
    public ResponseEntity<UsuarioResponse> darDeAltaUsuario (@RequestBody UsuarioRequest usuarioRequest){
        return ResponseEntity.ok(usuarioService.crear(usuarioRequest));
    }

    @PatchMapping("/admin/baja/usuario/{idUsuario}")
    public ResponseEntity<UsuarioResponse> darDeBajaUsuario (@PathVariable Long idUsuario){
        return ResponseEntity.ok(usuarioService.darDeBaja(idUsuario));
    }

}
