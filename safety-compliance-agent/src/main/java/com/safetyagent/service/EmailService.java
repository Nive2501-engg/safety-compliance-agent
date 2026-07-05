

    // private static final String FROM_EMAIL = "complainceagent@gmail.com";  // <- puதிya email podunga
    // private static final String APP_PASSWORD = "mbujovgvupkumwxz";                  // <- App Password (spaces illama)
package com.safetyagent.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service
public class EmailService {

    private static final String FROM_EMAIL = "complainceagent@gmail.com";
    private static final String APP_PASSWORD = "mbujovgvupkumwxz";

    public void sendAlertEmail(String toEmail, String subject, String messageBody) {
        if (toEmail == null || toEmail.isBlank()) {
            System.out.println("No recipient email, skipping.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(messageBody);

            Transport.send(message);
            System.out.println("Email sent to " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}