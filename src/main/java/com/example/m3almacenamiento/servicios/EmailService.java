package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.configuracion.ContactConfig;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

    /*
    * Envia una notificacion de Deuda actualizada
    * se llama desde pagos scheduler
    * */
    public void enviarNotificacionDeuda(Usuario usuario, BigDecimal montoMensual, BigDecimal nuevaDeudaTotal) {
        try{
            Map<String,Object> variables = new HashMap<>();
            variables.put("nombre", usuario.getNombreCompleto());

            String mesActual = getNombreMesActual();
            variables.put("mes", mesActual);
            variables.put("deudaTotal", nuevaDeudaTotal);

            BigDecimal totalCalculado = BigDecimal.ZERO;
            List<String> bauleras = new ArrayList<>();

            for(Baulera b : usuario.getBauleras()){
                bauleras.add(b.getNroBaulera());
                totalCalculado = totalCalculado.add(BigDecimal.valueOf(b.getTipoBaulera().getPrecioMensual()));
            }

            String baulerasNombres = String.join(", ", bauleras);

            variables.put("nroBauleras", baulerasNombres);
            variables.put("totalCalculado", totalCalculado);

            String logoBase64= convertirImagenABase64();
            variables.put("logoBase64", logoBase64);

            //Info de contacto de la empresa

            variables.put("telefonoContacto", contactConfig.getContacto().getTelefono() );
            variables.put("emailContacto", contactConfig.getContacto().getEmail() );
            variables.put("direccion", contactConfig.getContacto().getDireccion() );
            variables.put("horarioAtencion", contactConfig.getContacto().getHorario());

            enviarEmailTemplate(
                    usuario.getEmail(),
                    "Recordatorio Pago Bauleras" + mesActual,
                    "EmailDeudaTemplate",
                    variables);

        }catch(Exception e){
            log.error("❌ Error al enviar el mail ");
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
            variables.put("telefonoContacto", contactConfig.getContacto().getTelefono() );
            variables.put("emailContacto", contactConfig.getContacto().getEmail() );
            variables.put("direccion", contactConfig.getContacto().getDireccion() );
            variables.put("horarioAtencion", contactConfig.getContacto().getHorario());

            // Logo para el mail en Base64
            String logoBase64= convertirImagenABase64();
            variables.put("logoBase64", logoBase64);

            enviarEmailTemplate(
                    usuario.getEmail(),
                    "Asignacion de baulera" + baulera.getNroBaulera(),
                    "EmailAsignacionDeBauleraTemplate",
                    variables);

        }catch(Exception e){
            log.error("❌ Error al enviar el mail ");
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
            helper.setFrom("M3 Almacenamiento");

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
        ClassPathResource resource = new ClassPathResource("src/main/resources/static/img/logo.png");

        byte[] imageBytes = Files.readAllBytes(Paths.get(resource.getURI()));
        String base64img = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64img;


    }

}
