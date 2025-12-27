package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
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

    //=======================POST======================
    @PostMapping("/admin/nueva-baulera")
    public ResponseEntity<BauleraResponse> guardar (@RequestBody BauleraRequest bauleraRequest){
        BauleraResponse nuevaBaulera =  bauleraService.crear(bauleraRequest);
        return ResponseEntity.ok(nuevaBaulera);
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
