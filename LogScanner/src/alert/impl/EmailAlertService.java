package alert.impl;

import alert.AlertService;
import model.LogEvent;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class EmailAlertService implements AlertService {

    private final String username;
    private final String password;
    private final String to;
    private final String host;
    private final String port;

    public EmailAlertService(String host,
                             String port,
                             String username,
                             String password,
                             String to) {

        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.to = to;
    }

    @Override
    public void process(LogEvent event) {

        if (event == null) return;

        if (!isCritical(event.getLevel())) return;

        sendEmail(event);
    }

    private boolean isCritical(String level) {
        return level != null &&
                (level.equalsIgnoreCase("ERROR") ||
                        level.equalsIgnoreCase("CRITICAL"));
    }

    private void sendEmail(LogEvent event) {

        try {
            Properties props = new Properties();

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );

            message.setSubject("🚨 LOG ALERT");

            message.setText(format(event));

            Transport.send(message);

        } catch (Exception e) {
            System.err.println("Ошибка отправки email: " + e.getMessage());
        }
    }

    private String format(LogEvent e) {
        return "Time: " + e.getTimestamp() +
                "\nSource: " + e.getSource() +
                "\nLevel: " + e.getLevel() +
                "\nMessage: " + e.getMessage();
    }
}