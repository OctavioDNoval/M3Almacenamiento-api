package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.servicios.BauleraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/baulera")
@RequiredArgsConstructor
public class BauleraController {
    private final BauleraService bauleraService;


    //========================GET=====================
    @GetMapping("/admin/obtenerTodos")
    public ResponseEntity<List<BauleraResponse>> obtenerTodos(){
        List<BauleraResponse> lista =  bauleraService.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/user/obtener-x-id/{idBaulera}")
    public ResponseEntity<BauleraResponse> obtenerPorId(@PathVariable Long idBaulera){
        BauleraResponse bauleraPorId = bauleraService.obtenerPorId(idBaulera);
        return ResponseEntity.ok(bauleraPorId);
    }

    @GetMapping("/user/obtenerTodo/{idUsuario}")
    public ResponseEntity<List<BauleraResponse>> obtenerTodoPorUsuario(@PathVariable Long idUsuario){
        List<BauleraResponse> listaBauleras = bauleraService.obtenerPorIdUsuario(idUsuario);
        return ResponseEntity.ok(listaBauleras);
    }

    @GetMapping("/admin/obtenerTodo/pagina")
    public ResponseEntity<PaginacionResponse<BauleraResponse>> obtenerTodosPaginado(
            @RequestParam(defaultValue = "1") Integer pagina,
            @RequestParam(defaultValue = "15") Integer tamanio
    ){
        PaginacionResponse<BauleraResponse> paginaResponse = bauleraService
                .obtenerTodosPaginados(pagina, tamanio);
        System.out.println(paginaResponse);

        return ResponseEntity.ok(paginaResponse);
    }

    //=======================POST======================
    @PostMapping("/admin/new-baulera")
    public ResponseEntity<BauleraResponse> guardar (@RequestBody BauleraRequest bauleraRequest){
        BauleraResponse nuevaBaulera =  bauleraService.crear(bauleraRequest);
        return ResponseEntity.ok(nuevaBaulera);
    }

    @PostMapping("/admin/crear-lote")
    public ResponseEntity<List<BauleraResponse>> guardarEnLote (
            @RequestParam Integer cantidad,
            @RequestParam Long tipoBauleraId
    ){
        return ResponseEntity.ok(bauleraService.crearDesdeNroBaulera(cantidad, tipoBauleraId));
    }

    //====================DELETE==========================
    @DeleteMapping("/admin/eliminar/{idBaulera}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idBaulera){
        boolean isDeleted= bauleraService.eliminar(idBaulera);
        if(isDeleted){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }
}
