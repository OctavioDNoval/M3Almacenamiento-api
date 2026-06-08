package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.configuracion.ContactConfig;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Remito;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {

    private final BauleraRepositorio bauleraRepository;
    private final TemplateEngine templateEngine;
    private final ContactConfig contactConfig;

    public byte[] generarRemitoPdfConTemplate(Remito remito) throws Exception {
        Context context = new Context();

        context.setVariable("remito", remito);
        Usuario usuario = remito.getUsuario();
        context.setVariable("usuario", usuario);

        List<String> numerosBauleras = obtenerNumerosBauleras(remito.getBaulerasString());
        context.setVariable("numerosBauleras", numerosBauleras);

        // Detalle de bauleras
        List<Map<String, Object>> baulerasDetalle = new ArrayList<>();
        for (String nro : numerosBauleras) {
            Optional<Baulera> opt = bauleraRepository.findByNroBaulera(nro);
            String tipo = "-";
            BigDecimal precio = BigDecimal.ZERO;
            if (opt.isPresent()) {
                Baulera b = opt.get();
                if (b.getTipoBaulera() != null) {
                    tipo = b.getTipoBaulera().getTipoBauleraNombre();
                    precio = BigDecimal.valueOf(b.getTipoBaulera().getPrecioMensual());
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("numero", nro);
            item.put("tipo", tipo);
            item.put("precio", precio);
            baulerasDetalle.add(item);
        }
        BigDecimal precioXMes = (BigDecimal) baulerasDetalle.get(0).get("precio");
        context.setVariable("baulerasDetalle", baulerasDetalle);
        context.setVariable("precioBauleraMes", precioXMes);
        // Deuda
        BigDecimal deudaAcumulada = usuario.getDeudaAcumulada();
        BigDecimal deudaAnterior = BigDecimal.ZERO;
        if (deudaAcumulada != null && remito.getImporteTotal() != null) {
            deudaAnterior = deudaAcumulada.subtract(remito.getImporteTotal());
            if (deudaAnterior.compareTo(BigDecimal.ZERO) < 0) deudaAnterior = BigDecimal.ZERO;
        }
        context.setVariable("deudaAcumulada", deudaAcumulada);
        context.setVariable("deudaAnterior", deudaAnterior);

        // Contacto
        if (contactConfig != null && contactConfig.getContacto() != null) {
            context.setVariable("contacto", contactConfig.getContacto());
        } else {
            Map<String, String> fallback = new HashMap<>();
            fallback.put("telefono", "N/A");
            fallback.put("email", "N/A");
            context.setVariable("contacto", fallback);
        }

        // Logo
        try {
            String logoBase64 = convertirImagenABase64();
            context.setVariable("logoBase64", logoBase64);
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo: {}", e.getMessage());
            context.setVariable("logoBase64", "");
        }

        // 🔥 NUEVO: Importe en letras
        String importeEnLetras = convertirNumeroALetras(precioXMes);
        context.setVariable("importeEnLetras", importeEnLetras);

        BigDecimal deuda = remito.getDeudaAnterior();
        context.setVariable("deudaAnterior", deuda);

        String fechaFormateada = remito.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        context.setVariable("fechaEmisionStr", fechaFormateada);

        String html = templateEngine.process("RemitoTemplate", context);
        return convertirHtmlAPdf(html);
    }

    // -------------------------------------------------------------
    // Conversión de número a letras (español, hasta millones)
    // -------------------------------------------------------------
    private String convertirNumeroALetras(BigDecimal monto) {
        if (monto == null) return "CERO";
        long parteEntera = monto.longValue();
        int centavos = monto.remainder(BigDecimal.ONE).movePointRight(2).intValue();
        String letras = convertirEntero(parteEntera);
        letras = letras + " CON " + String.format("%02d", centavos) + "/100";
        return letras;
    }

    private String convertirEntero(long numero) {
        if (numero == 0) return "CERO";
        if (numero == 1000000) return "UN MILLON";
        if (numero > 999999) return "NO SOPORTADO";

        String[] unidades = {"", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"};
        String[] decenas = {"", "DIEZ", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
        String[] especiales = {"ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE"};

        StringBuilder resultado = new StringBuilder();

        int miles = (int) (numero / 1000);
        int resto = (int) (numero % 1000);

        if (miles > 0) {
            if (miles == 1) resultado.append("MIL ");
            else resultado.append(convertirEntero(miles)).append(" MIL ");
        }

        int centenas = resto / 100;
        int decenaUnidad = resto % 100;

        if (centenas > 0) {
            switch (centenas) {
                case 1: resultado.append("CIEN "); break;
                case 2: resultado.append("DOSCIENTOS "); break;
                case 3: resultado.append("TRESCIENTOS "); break;
                case 4: resultado.append("CUATROCIENTOS "); break;
                case 5: resultado.append("QUINIENTOS "); break;
                case 6: resultado.append("SEISCIENTOS "); break;
                case 7: resultado.append("SETECIENTOS "); break;
                case 8: resultado.append("OCHOCIENTOS "); break;
                case 9: resultado.append("NOVECIENTOS "); break;
            }
        }

        if (decenaUnidad > 0) {
            if (decenaUnidad < 10) {
                resultado.append(unidades[decenaUnidad]);
            } else if (decenaUnidad <= 19) {
                resultado.append(especiales[decenaUnidad - 11]);
            } else {
                int dec = decenaUnidad / 10;
                int uni = decenaUnidad % 10;
                if (uni == 0) resultado.append(decenas[dec]);
                else resultado.append(decenas[dec]).append(" Y ").append(unidades[uni]);
            }
        }

        return resultado.toString().trim();
    }

    // -------------------------------------------------------------
    // Métodos auxiliares (sin cambios)
    // -------------------------------------------------------------
    private byte[] convertirHtmlAPdf(String html) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        }
    }

    private String convertirImagenABase64() throws Exception {
        ClassPathResource resource = new ClassPathResource("/static/img/logo.png");
        byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(resource.getURI()));
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64;
    }

    private List<String> obtenerNumerosBauleras(String baulerasString) {
        if (baulerasString == null || baulerasString.trim().isEmpty()) return List.of();
        return Arrays.stream(baulerasString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}