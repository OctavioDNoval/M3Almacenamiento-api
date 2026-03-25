package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Remito;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {

    private final BauleraRepositorio bauleraRepository;


    public byte[] generarRemitoPdf(Remito remito) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Crear documento PDF
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Configurar fuente
            PdfFont font = PdfFontFactory.createFont();

            // ========== ENCABEZADO ==========
            Paragraph header = new Paragraph("REMITO DE BAULERAS")
                    .setFont(font)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(header);

            // Línea separadora
            document.add(new Paragraph("______________________________________________")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));

            // ========== DATOS DEL REMITO ==========
            document.add(new Paragraph("DATOS DEL REMITO")
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));

            document.add(new Paragraph("N° Remito: " + remito.getIdRemito())
                    .setFont(font)
                    .setFontSize(11));
            document.add(new Paragraph("Período: " + remito.getPeriodo())
                    .setFont(font)
                    .setFontSize(11));
            document.add(new Paragraph("Fecha de Emisión: " +
                    remito.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(font)
                    .setFontSize(11));

            document.add(new Paragraph(" "));

            // ========== DATOS DEL CLIENTE ==========
            Usuario usuario = remito.getUsuario();
            document.add(new Paragraph("DATOS DEL CLIENTE")
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));

            document.add(new Paragraph("Nombre: " + usuario.getNombreCompleto())
                    .setFont(font)
                    .setFontSize(11));
            document.add(new Paragraph("DNI: " + usuario.getDni())
                    .setFont(font)
                    .setFontSize(11));
            document.add(new Paragraph("Email: " + usuario.getEmail())
                    .setFont(font)
                    .setFontSize(11));
            if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
                document.add(new Paragraph("Teléfono: " + usuario.getTelefono())
                        .setFont(font)
                        .setFontSize(11));
            }

            document.add(new Paragraph(" "));

            // ========== TABLA DE BAULERAS ==========
            document.add(new Paragraph("DETALLE DE BAULERAS")
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));

            // Obtener lista de números de bauleras desde baulerasString
            List<String> numerosBauleras = obtenerNumerosBauleras(remito.getBaulerasString());

            if (!numerosBauleras.isEmpty()) {
                // Crear tabla con 4 columnas
                Table table = new Table(UnitValue.createPercentArray(new float[]{30, 30, 20}))
                        .useAllAvailableWidth();

                // Encabezados de la tabla
                table.addCell(new Cell().add(new Paragraph("N° BAUELRA").setFont(font).setBold()));
                table.addCell(new Cell().add(new Paragraph("TIPO").setFont(font).setBold()));
                table.addCell(new Cell().add(new Paragraph("PRECIO").setFont(font).setBold()));

                BigDecimal totalDetallado = BigDecimal.ZERO;

                // Buscar cada baulera por su número y agregar a la tabla
                for (String nroBaulera : numerosBauleras) {
                    // Buscar la baulera por su número
                    Baulera baulera = bauleraRepository.findByNroBaulera(nroBaulera)
                            .orElse(null);

                    String tipo = "-";

                    BigDecimal precio = BigDecimal.ZERO;

                    if (baulera != null && baulera.getTipoBaulera() != null) {
                        tipo = baulera.getTipoBaulera().getTipoBauleraNombre();
                        precio = BigDecimal.valueOf(baulera.getTipoBaulera().getPrecioMensual());
                    }

                    table.addCell(new Cell().add(new Paragraph(nroBaulera).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph(tipo).setFont(font)));
                    table.addCell(new Cell().add(new Paragraph("$ " + precio.toString()).setFont(font)));

                    totalDetallado = totalDetallado.add(precio);
                }

                document.add(table);

                // Mostrar total detallado si difiere del importe total
                if (totalDetallado.compareTo(remito.getImporteTotal()) != 0) {
                    document.add(new Paragraph(" ")
                            .setMarginTop(5));
                    document.add(new Paragraph("Total parcial: $ " + totalDetallado.toString())
                            .setFont(font)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.RIGHT));
                }

            } else {
                document.add(new Paragraph("No se encontraron bauleras para este remito")
                        .setFont(font)
                        .setFontSize(11));
            }

            document.add(new Paragraph(" "));

            // ========== TOTAL ==========
            Paragraph total = new Paragraph("TOTAL: $ " + remito.getImporteTotal().toString())
                    .setFont(font)
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(20);
            document.add(total);

            document.add(new Paragraph(" "));

            // ========== RESUMEN DE DEUDA ==========
            if (usuario.getDeudaAcumulada() != null && usuario.getDeudaAcumulada().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("RESUMEN DE DEUDA")
                        .setFont(font)
                        .setFontSize(12)
                        .setBold()
                        .setMarginBottom(5));

                document.add(new Paragraph("Deuda acumulada al período anterior: $ " +
                        usuario.getDeudaAcumulada().subtract(remito.getImporteTotal()).toString())
                        .setFont(font)
                        .setFontSize(10));
                document.add(new Paragraph("Monto del presente período: $ " + remito.getImporteTotal().toString())
                        .setFont(font)
                        .setFontSize(10));
                document.add(new Paragraph("Deuda total actualizada: $ " + usuario.getDeudaAcumulada().toString())
                        .setFont(font)
                        .setFontSize(10)
                        .setBold());

                document.add(new Paragraph(" "));
            }

            // ========== OBSERVACIONES ==========
            document.add(new Paragraph("OBSERVACIONES")
                    .setFont(font)
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(5));

            document.add(new Paragraph("Este documento es un remito interno, no tiene validez fiscal.")
                    .setFont(font)
                    .setFontSize(10));

            document.add(new Paragraph("Ante cualquier duda, comuníquese con administración.")
                    .setFont(font)
                    .setFontSize(10));

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // ========== FIRMA ==========


            // Cerrar documento
            document.close();

            log.info("PDF generado exitosamente para remito ID: {}", remito.getIdRemito());

        } catch (Exception e) {
            log.error("Error generando PDF", e);
            throw new IOException("Error generando PDF", e);
        }

        return outputStream.toByteArray();
    }


    private List<String> obtenerNumerosBauleras(String baulerasString) {
        if (baulerasString == null || baulerasString.trim().isEmpty()) {
            return List.of();
        }

        // Separar por coma y limpiar espacios
        return Arrays.stream(baulerasString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}