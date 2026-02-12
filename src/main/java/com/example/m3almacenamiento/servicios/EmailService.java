package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.configuracion.ContactConfig;
import com.example.m3almacenamiento.modelo.DTO.InfoDeudaEmail;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ContactConfig contactConfig;
    private final BauleraRepositorio bauleraRepositorio;

    @Value("${spring.mail.username}")
    private String remiteteEmail;

    @PostConstruct
    public void logConfig() {
        log.info("✅ Configuración de email cargada:");
        log.info("   Username: {}", remiteteEmail);  // Esto debería mostrar tu email completo
    }

    /*
    * Envia una notificacion de Deuda actualizada
    * se llama desde pagos scheduler
    * */
    public void enviarNotificacionDeuda(Usuario usuario, InfoDeudaEmail infoEmail) {
        try{
            Map<String,Object> variables = new HashMap<>();
            variables.put("nombre", usuario.getNombreCompleto());

            String mesActual = getNombreMesActual();
            variables.put("mes", mesActual);
            variables.put("deudaTotal", infoEmail.getNuevaDeudaTotaL());

            BigDecimal totalCalculado = BigDecimal.ZERO;
            List<String> bauleras = new ArrayList<>();

            /*try{
                int cantidadBauleras = usuario.getBauleras().size();
                log.info("Usuario tiene {} bauleras", cantidadBauleras);

                for(Baulera b : usuario.getBauleras()){
                    bauleras.add(b.getNroBaulera());
                    totalCalculado = totalCalculado.add(BigDecimal.valueOf(b.getTipoBaulera().getPrecioMensual()));
                }
            }catch(Exception e){
                log.error("Error al acceder a las bauleras del usuario: {} | {}", usuario.getNombreCompleto(), e.getMessage());
                List<Baulera> baulerasUsuario = bauleraRepositorio.findByUsuarioAsignado_IdUsuario(usuario.getIdUsuario());
                for(Baulera b : baulerasUsuario){
                    bauleras.add(b.getNroBaulera());
                    totalCalculado = totalCalculado.add(BigDecimal.valueOf(b.getTipoBaulera().getPrecioMensual()));
                }
            }*/


            variables.put("nroBauleras", infoEmail.getNumerosBauleras());
            variables.put("totalCalculado", infoEmail.getTotalCalculado());
            variables.put("montoMensual", infoEmail.getMontoMensual());
            variables.put("deudaAnterior", infoEmail.getDeudaAnterior());
            try {
                String logoBase64 = convertirImagenABase64();
                variables.put("logoBase64", logoBase64);
            } catch (Exception e) {
                log.error("Error al convertir imagen a Base64 en notificación de deuda", e);
                variables.put("logoBase64", "");
            }

            //Info de contacto de la empresa

            if (contactConfig != null && contactConfig.getContacto() != null) {
                variables.put("telefonoContacto", contactConfig.getContacto().getTelefono());
                variables.put("emailContacto", contactConfig.getContacto().getEmail() );
                variables.put("direccion", contactConfig.getContacto().getDireccion() );
                variables.put("horarioAtencion", contactConfig.getContacto().getHorario());
            } else {
                log.error("Configuración de contacto no disponible");
                variables.put("telefonoContacto", "N/A");
                variables.put("emailContacto", "N/A" );
                variables.put("direccion", "N/A" );
                variables.put("horarioAtencion", "N/A");
            }

            enviarEmailTemplate(
                    usuario.getEmail(),
                    "Recordatorio Pago Bauleras " + mesActual,
                    "EmailDeudaTemplate",
                    variables);

        }catch(Exception e){
            log.error("❌ Error al enviar el mail: {} ", e.getMessage());
        }
    }

    /*
    * Con este metodo vamos a avisarle al usuario cuando se
    * le asigna una baulera nueva
    * */
    public void enviarNotificacionDeAsignacion(Usuario usuario, Baulera baulera){
        try{
            Map<String,Object> variables = new HashMap<>();

            // Datos de la Baulera y el Usuario
            variables.put("nombre", usuario.getNombreCompleto());
            variables.put("nroBaulera", baulera.getNroBaulera());
            variables.put("tipoBaulera", baulera.getTipoBaulera().getTipoBauleraNombre());
            variables.put("montoMensual", baulera.getTipoBaulera().getPrecioMensual());

            // Datos de la empresa
            if (contactConfig != null && contactConfig.getContacto() != null) {
                variables.put("telefonoContacto", contactConfig.getContacto().getTelefono());
                variables.put("emailContacto", contactConfig.getContacto().getEmail() );
                variables.put("direccion", contactConfig.getContacto().getDireccion() );
                variables.put("horarioAtencion", contactConfig.getContacto().getHorario());
            } else {
                log.error("Configuración de contacto no disponible");
                return;
            }

            // Logo para el mail en Base64
            try {
                String logoBase64 = convertirImagenABase64();
                variables.put("logoBase64", logoBase64);
            } catch (IOException e) {
                log.error("Error al convertir imagen a Base64", e);
                variables.put("logoBase64", ""); // valor por defecto
            }

            enviarEmailTemplate(
                    usuario.getEmail(),
                    "Asignacion de baulera " + baulera.getNroBaulera(),
                    "EmailAsignacionDeBauleraTemplate",
                    variables);

        }catch(Exception e){
            log.error("❌ Error al enviar el mail ");
            e.printStackTrace();
        }
    }

    public void enviarBienvenida(Usuario usuario){
        try{
        Map<String,Object> variables = new HashMap<>();
        variables.put("nombre", usuario.getNombreCompleto());
        variables.put("email", usuario.getEmail());
        variables.put("dni", usuario.getDni());

        if (contactConfig != null && contactConfig.getContacto() != null) {
            variables.put("telefonoContacto", contactConfig.getContacto().getTelefono());
            variables.put("emailContacto", contactConfig.getContacto().getEmail() );
            variables.put("direccion", contactConfig.getContacto().getDireccion() );
            variables.put("horarioAtencion", contactConfig.getContacto().getHorario());
        } else {
            log.error("Configuración de contacto no disponible");
            return;
        }

        try {
            String logoBase64 = convertirImagenABase64();
            variables.put("logoBase64", logoBase64);
        } catch (IOException e) {
            log.error("Error al convertir imagen a Base64", e);
            variables.put("logoBase64", ""); // valor por defecto
        }

        enviarEmailTemplate(
                usuario.getEmail(),
                "Bienvenido a M3 Almacenamiento",
                "EmailBienvenida",
                variables);

        }catch (Exception e){
            log.error("❌ Error al enviar el mail ");
            e.printStackTrace();
        }
    }

    /*
    * Este metodo se usa para enviar email con un template determinado pasado
    * por parametro
    * */
    public void enviarEmailTemplate(String destinatario, String asunto, String template, Map<String,Object> variables){
        try{
            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(template, context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            //Al poner true estamos diciendo que es un HTML lo que estamos mandando y que
            //Lo procese como tal
            helper.setText(htmlContent, true);
            helper.setFrom(remiteteEmail,"M3 Almacenamiento");

            log.info("Emviando mail...");
            mailSender.send(mimeMessage);

            log.info("Mail enviado: {} -> {}", asunto,destinatario);
        }catch(Exception e){
            log.error("❌ Error enviando email template a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }



    // ======== Metodos auxiliares =========

    private String getNombreMesActual(){
        String[] meses = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        int mes = Calendar.getInstance().get(Calendar.MONTH);
        int anio = Calendar.getInstance().get(Calendar.YEAR);

        //El get calendar month si es Enero devuelve 0, por lo cual no hay que restar 1 para obtener la
        //posicion correcta del arreglo
        return meses[mes] + " " + anio;
    }

    private String convertirImagenABase64() throws Exception{
        ClassPathResource resource = new ClassPathResource("/static/img/logo.png");

        byte[] imageBytes = Files.readAllBytes(Paths.get(resource.getURI()));
        String base64img = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64img;


    }

}
