package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.excepciones.ResourceNotFoundException;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.PagoResponse;
import com.example.m3almacenamiento.modelo.entidad.Pago;
import com.example.m3almacenamiento.repositorios.PagoRepositorio;
import com.example.m3almacenamiento.servicios.PagoService;
import com.example.m3almacenamiento.servicios.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {
    private final PagoService pagoService;
    private final PdfGeneratorService pdfGeneratorService;
    private final PagoRepositorio pagoRepositorio;

    @GetMapping("/admin/obtenerPaginados")
    public ResponseEntity<PaginacionResponse<PagoResponse>> obtenerPaginados(
            @RequestParam(defaultValue = "1") Integer pagina,
            @RequestParam(defaultValue = "15") Integer tamanio,
            @RequestParam(defaultValue = "idPago") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "") String filter) {
        PaginacionResponse<PagoResponse> paginaResponse;
        if (filter == null || filter.trim().isEmpty()) {
            paginaResponse = pagoService.obtenerTodosPaginados(pagina, tamanio, sortBy, direction);
        } else {
            paginaResponse = pagoService.obtenerPaginadoConFiltro(pagina, tamanio, sortBy, filter, direction);
        }
        return ResponseEntity.ok(paginaResponse);
    }

    @GetMapping("/pdf/descargar/{idUsuario}")
    public ResponseEntity<byte[]> descargarPdfPagos(@PathVariable UUID idUsuario) throws Exception {
        List<PagoResponse> pagos = pagoService.obtenerPorUsuario(idUsuario);
        if (pagos.isEmpty()) {
            throw new ResourceNotFoundException("No hay pagos registrados");
        }

        byte[] pdf = pdfGeneratorService.generarPagosPdf(pagos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "listado_pagos.pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
