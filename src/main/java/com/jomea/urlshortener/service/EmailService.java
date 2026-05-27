package com.jomea.urlshortener.service;

import com.jomea.urlshortener.config.AesEncryption;
import com.jomea.urlshortener.entity.AppSettings;
import com.jomea.urlshortener.repository.AppSettingsRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final AppSettingsRepository appSettingsRepository;
    private final AesEncryption aesEncryption;

    public EmailService(AppSettingsRepository appSettingsRepository, AesEncryption aesEncryption) {
        this.appSettingsRepository = appSettingsRepository;
        this.aesEncryption = aesEncryption;
    }

    private JavaMailSender buildSender() {
        AppSettings s = appSettingsRepository.findById(1L).orElse(null);
        if (s == null || s.getSmtpHost() == null || s.getSmtpHost().isBlank()) return null;

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(s.getSmtpHost());
        sender.setPort(s.getSmtpPort() != null ? s.getSmtpPort() : 587);

        String username = s.getSmtpUsername();
        if (username != null && !username.isEmpty()) {
            sender.setUsername(aesEncryption.decrypt(username));
        }
        String password = s.getSmtpPassword();
        if (password != null && !password.isEmpty()) {
            sender.setPassword(aesEncryption.decrypt(password));
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(s.isSmtpUseTls()));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        return sender;
    }

    public boolean sendEmail(String to, String subject, String html) {
        try {
            JavaMailSender sender = buildSender();
            if (sender == null) {
                log.warn("SMTP not configured, skipping email to {}", to);
                return false;
            }
            AppSettings s = appSettingsRepository.findById(1L).orElse(null);
            String fromEmail = s != null && s.getSmtpFromEmail() != null ? s.getSmtpFromEmail() : "noreply@shrtly.com";
            String fromName = s != null && s.getSmtpFromName() != null ? s.getSmtpFromName() : "Shrtly";

            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            sender.send(msg);
            log.info("Email sent to {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            return false;
        }
    }

    public void sendWelcomeEmail(String to, String name) {
        String html = """
            <div style="font-family:sans-serif;max-width:480px;margin:0 auto">
              <h2 style="color:#3563e9">Welcome to Shrtly!</h2>
              <p>Hi %s,</p>
              <p>Your account has been created. Start shortening links and tracking clicks instantly.</p>
              <p style="margin-top:24px">
                <a href="%s" style="display:inline-block;padding:12px 24px;background:#3563e9;color:#fff;text-decoration:none;border-radius:8px">Get started</a>
              </p>
              <p style="color:#888;font-size:0.8125rem;margin-top:32px">— The Shrtly Team</p>
            </div>
            """.formatted(name, appSettingsRepository.findById(1L).map(a -> a.getSiteName()).orElse("Shrtly"));
        sendEmail(to, "Welcome to Shrtly!", html);
    }

    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        String html = """
            <div style="font-family:sans-serif;max-width:480px;margin:0 auto">
              <h2 style="color:#3563e9">Reset your password</h2>
              <p>Hi %s,</p>
              <p>Click the button below to reset your password. This link expires in 1 hour.</p>
              <p style="margin-top:24px">
                <a href="%s" style="display:inline-block;padding:12px 24px;background:#3563e9;color:#fff;text-decoration:none;border-radius:8px">Reset password</a>
              </p>
              <p style="color:#888;font-size:0.8125rem;margin-top:32px">If you didn't request this, you can ignore this email.</p>
            </div>
            """.formatted(name, resetLink);
        sendEmail(to, "Reset your Shrtly password", html);
    }

    public void sendPaymentReceipt(String to, String name, String planName, String amount, String currency) {
        String html = """
            <div style="font-family:sans-serif;max-width:480px;margin:0 auto">
              <h2 style="color:#3563e9">Payment confirmed!</h2>
              <p>Hi %s,</p>
              <p>Your <strong>%s</strong> subscription is now active.</p>
              <p>Amount: <strong>%s %s</strong></p>
              <p style="color:#888;font-size:0.8125rem;margin-top:32px">— The Shrtly Team</p>
            </div>
            """.formatted(name, planName, currency, amount);
        sendEmail(to, "Shrtly — Payment confirmed!", html);
    }
}
