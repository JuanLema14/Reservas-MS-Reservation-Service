package com.codefactory.reservasmsreservationservice.service.impl;

import com.codefactory.reservasmsreservationservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of EmailService.
 * Sends reservation-related emails using JavaMailSender and Thymeleaf templates.
 * Email sending is ASYNC to not block the main request thread.
 */
@Service
@ConditionalOnBean(name = "javaMailSender")
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${platform.name:Plataforma de Reservas}")
    private String appName;

    @Value("${email.host:smtp.gmail.com}")
    private String emailHost;

    @Value("${email.port:587}")
    private int emailPort;

    @Value("${email.username:}")
    private String emailUsername;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Async("emailExecutor")
    public void sendReservationConfirmationEmail(String to, String clienteNombre, String servicioNombre, String fechaHora) {
        log.info("[ASYNC] Iniciando envio de email de confirmacion a: {}", to);
        log.debug("[ASYNC] Email config - host: {}, port: {}, username: {}",
                emailHost, emailPort, emailUsername);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Confirmación de tu reserva - " + appName);
            helper.setFrom(emailUsername);

            Context context = new Context();
            context.setVariable("clienteNombre", clienteNombre);
            context.setVariable("servicioNombre", servicioNombre);
            context.setVariable("fechaHora", fechaHora);
            context.setVariable("appName", appName);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("reservation-confirmation", context);
            helper.setText(htmlContent, true);

            log.info("[ASYNC] Enviando email de confirmacion...");
            javaMailSender.send(message);
            log.info("[ASYNC] Email de confirmacion enviado exitosamente a: {}", to);

        } catch (Exception e) {
            log.error("[ASYNC] Error al enviar email de confirmacion a: {}", to, e);
            // Don't throw - just log the error to not fail the reservation
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendReservationCancellationEmail(String to, String clienteNombre, String servicioNombre, String fechaHora) {
        log.info("[ASYNC] Iniciando envio de email de cancelacion a: {}", to);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Reserva cancelada - " + appName);
            helper.setFrom(emailUsername);

            Context context = new Context();
            context.setVariable("clienteNombre", clienteNombre);
            context.setVariable("servicioNombre", servicioNombre);
            context.setVariable("fechaHora", fechaHora);
            context.setVariable("appName", appName);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("reservation-cancellation", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("[ASYNC] Email de cancelacion enviado exitosamente a: {}", to);

        } catch (Exception e) {
            log.error("[ASYNC] Error al enviar email de cancelacion a: {}", to, e);
            // Don't throw - just log the error
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendReservationReminderEmail(String to, String clienteNombre, String servicioNombre, String fechaHora) {
        log.info("[ASYNC] Iniciando envio de email de recordatorio a: {}", to);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Recordatorio de tu reserva - " + appName);
            helper.setFrom(emailUsername);

            Context context = new Context();
            context.setVariable("clienteNombre", clienteNombre);
            context.setVariable("servicioNombre", servicioNombre);
            context.setVariable("fechaHora", fechaHora);
            context.setVariable("appName", appName);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("reservation-reminder", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("[ASYNC] Email de recordatorio enviado exitosamente a: {}", to);

        } catch (Exception e) {
            log.error("[ASYNC] Error al enviar email de recordatorio a: {}", to, e);
            // Don't throw - just log the error
        }
    }
}