package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.entidad.Log;
import com.example.m3almacenamiento.servicios.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class DashBoardController {
    private final LogService logService;

    @GetMapping("/public/obtenerTodo")
    public ResponseEntity<List<Log>> obtenerTodo() {
        return ResponseEntity.ok(logService.obtenerTodos());
    }
}
