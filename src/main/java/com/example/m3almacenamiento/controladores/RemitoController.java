package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.RemitoResponse;

import com.example.m3almacenamiento.modelo.entidad.Remito;
import com.example.m3almacenamiento.repositorios.RemitoRepositorio;
import com.example.m3almacenamiento.servicios.PdfGeneratorService;
import com.example.m3almacenamiento.servicios.RemitoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/remitos")
@RequiredArgsConstructor
@Slf4j
public class RemitoController {
    private final RemitoService remitoService;
    private final RemitoRepositorio remitoRepositorio;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("/admin/obtenerPaginados")
    public ResponseEntity<PaginacionResponse<RemitoResponse>> obtenerPaginados(
            @RequestParam(defaultValue = "1") Integer pagina,
            @RequestParam(defaultValue = "15") Integer tamanio,
            @RequestParam(defaultValue = "idUsuario") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "") String filter){
        PaginacionResponse<RemitoResponse> paginaResponse;
        if(filter == null || filter.trim().isEmpty()){
            paginaResponse = remitoService.obtenerTodosPaginados(pagina, tamanio, sortBy,direction);
        }else{
            paginaResponse = remitoService.obtenerPaginadoConFiltro(pagina, tamanio, sortBy, filter,direction);
        }

        return ResponseEntity.ok(paginaResponse);
    }

    @GetMapping("/pdf/{idRemito}")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long idRemito) {
        try {
            Remito remito = remitoRepositorio.findById(idRemito)
                    .orElseThrow(() -> new RuntimeException("Remito no encontrado con ID: " + idRemito));



            byte[] pdfBytes = pdfGeneratorService.generarRemitoPdf(remito);

            // 3. Crear nombre del archivo
            String nombreArchivo = String.format("remito_%s_%s.pdf",
                    remito.getUsuario().getNombreCompleto()
                            .toLowerCase()
                            .replace(" ", "_")
                            .replace("ñ", "n"),
                    remito.getPeriodo()
                            .toLowerCase()
                            .replace(" ", "_")
                            .replace("ñ", "n")
            );

            // 4. Configurar headers para la descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(pdfBytes.length);



            // 5. Devolver el PDF como respuesta
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Error al generar el PDF para remito ID: {}", idRemito, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
