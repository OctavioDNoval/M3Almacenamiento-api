package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.response.DashBoardResponse;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.UserDashBoardResponse;
import com.example.m3almacenamiento.modelo.entidad.Log;
import com.example.m3almacenamiento.servicios.DashBoardService;
import com.example.m3almacenamiento.servicios.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class DashBoardController {
    private final LogService logService;
    private final DashBoardService dashBoardService;

    @GetMapping("/admin/obtenerLogsPaginados")
    public ResponseEntity<PaginacionResponse<Log>> obtenerLogsPaginados(Integer pagina, Integer tamanio) {
        return ResponseEntity.ok(logService.obtenerLogsPaginados(pagina,tamanio));

    }

    @GetMapping("/user/obtenerDashBoard/{idUsuario}")
    public ResponseEntity<UserDashBoardResponse> obtenerUserDashBoard(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(dashBoardService.obtenerUserDashBoard(idUsuario));
    }

    @GetMapping("/admin/obtenerDashBoard")
    public ResponseEntity<DashBoardResponse> obtenerDashBoard() {
        return ResponseEntity.ok(dashBoardService.obtenerDashBoard());
    }
}
