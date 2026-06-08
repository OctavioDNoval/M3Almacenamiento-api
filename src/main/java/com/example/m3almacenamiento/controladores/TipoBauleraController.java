package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.request.TipoBauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.TipoBauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
import com.example.m3almacenamiento.servicios.TipoBauleraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/user/obtener-id/{idTipoBaulera}")
    public ResponseEntity<TipoBauleraResponse> obtenerPorId(@PathVariable UUID idTipoBaulera){
        return ResponseEntity.ok(tipoBauleraService.obtenerPorId(idTipoBaulera));
    }

    //=================POST=================
    @PostMapping("/admin/new-tipo")
    public ResponseEntity<TipoBauleraResponse> newTipo(@RequestBody TipoBauleraRequest tipoBauleraRequest){
        return ResponseEntity.ok(tipoBauleraService.crear(tipoBauleraRequest));
    }

    //======================PATCH=====================
    @PatchMapping("/admin/newPrecio/{idTipoBaulera}")
    public ResponseEntity<TipoBauleraResponse> newPrecio(@PathVariable UUID idTipoBaulera, @RequestParam Double precio){
        return ResponseEntity.ok(tipoBauleraService.actualizarPrecio(precio,idTipoBaulera));
    }

    //================DELETE=====================
    @DeleteMapping("/admin/delete/{idTipoBaulera}/cascade")
    public ResponseEntity<Void> deleteTipoBauleraCascade(@PathVariable UUID idTipoBaulera){
        boolean isDeleted = tipoBauleraService.eliminarTipoBauleraOnCascade(idTipoBaulera);
        if(isDeleted){
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.notFound().build();
        }
    }

}
