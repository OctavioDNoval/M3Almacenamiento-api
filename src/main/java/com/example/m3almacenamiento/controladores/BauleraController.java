package com.example.m3almacenamiento.controladores;

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
}
