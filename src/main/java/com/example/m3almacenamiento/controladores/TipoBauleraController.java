package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.response.TipoBauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
import com.example.m3almacenamiento.servicios.TipoBauleraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tipo-baulera")
@RequiredArgsConstructor
public class TipoBauleraController {
    private final TipoBauleraService tipoBauleraService;

    //================GET====================
    @GetMapping("/admin/obtener-todos")
    public ResponseEntity<List<TipoBauleraResponse>> obtenerTodos(){
        List<TipoBauleraResponse> lista =  tipoBauleraService.obtenerTodos();
        return ResponseEntity.ok(lista);
    }


}
