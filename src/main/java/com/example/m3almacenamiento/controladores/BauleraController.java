package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.servicios.BauleraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/baulera")
@RequiredArgsConstructor
@Slf4j
public class BauleraController {
    private final BauleraService bauleraService;


    //========================GET=====================
    @GetMapping("/admin/obtenerTodos")
    public ResponseEntity<List<BauleraResponse>> obtenerTodos(){
        List<BauleraResponse> lista =  bauleraService.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/admin/obtenerDisponibles")
    public ResponseEntity<List<BauleraResponse>> obtenerDisponibles(){
        List<BauleraResponse> bauleras = bauleraService.obtenerTodosDisponibles();
        return ResponseEntity.ok(bauleras);
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
            @RequestParam(defaultValue = "15") Integer tamanio,
            @RequestParam(defaultValue = "idBaulera") String sortBy,
            @RequestParam(defaultValue = "")String filter){

        log.info("📥 ENDPOINT LLAMADO - Parámetros recibidos:");
        log.info("   pagina: {}", pagina);
        log.info("   tamanio: {}", tamanio);
        log.info("   sortBy: '{}'", sortBy);
        log.info("   search: '{}'", filter);

        PaginacionResponse paginaResponse = new PaginacionResponse();

        if(filter == null || filter.trim().isEmpty()){
            paginaResponse = bauleraService.obtenerTodosPaginados(pagina, tamanio, sortBy);
        }else{
            paginaResponse = bauleraService.obtenerPaginadoConFiltro(pagina, tamanio, sortBy, filter);
        }

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

    //=======================PATCH==========================
    @PatchMapping("/admin/desasignar/{idBaulera}")
    public ResponseEntity<BauleraResponse> desasignar(@PathVariable Long idBaulera){
        return ResponseEntity.ok(bauleraService.desasignarBaulera(idBaulera));
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
