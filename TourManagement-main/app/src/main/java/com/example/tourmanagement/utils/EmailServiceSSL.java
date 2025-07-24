package com.example.tourmanagement.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Alternative EmailService with SSL configuration for Gmail SMTP.
 * Use this if the STARTTLS configuration doesn't work.
 */
public class EmailServiceSSL {

    private static final String TAG = "EmailServiceSSL";

    // Email configuration for SSL
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final String EMAIL_FROM = "maixuanhieu250123@gmail.com";
    private static final String EMAIL_PASSWORD = "zmfd aycm rzuu oarb";

    public interface EmailCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void sendPasswordResetEmail(String recipientEmail, String recipientName,
                                            String temporaryPassword, EmailCallback callback) {
        new SendEmailTask(recipientEmail, recipientName, temporaryPassword, callback).execute();
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private String recipientEmail;
        private String recipientName;
        private String temporaryPassword;
        private EmailCallback callback;
        private String errorMessage;

        public SendEmailTask(String recipientEmail, String recipientName,
                           String temporaryPassword, EmailCallback callback) {
            this.recipientEmail = recipientEmail;
            this.recipientName = recipientName;
            this.temporaryPassword = temporaryPassword;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // SSL configuration for Gmail
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                props.put("mail.smtp.socketFactory.port", SMTP_PORT);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.checkserveridentity", "true");

                // Create authenticator
                Authenticator authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                    }
                };

                // Create session
                Session session = Session.getInstance(props, authenticator);
                session.setDebug(true); // Enable debug for troubleshooting

                // Create message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Tour Management - Password Reset");

                // Create email content
                String emailContent = createPasswordResetEmailContent(recipientName, temporaryPassword);
                message.setContent(emailContent, "text/html; charset=utf-8");

                // Send email
                Transport.send(message);

                Log.d(TAG, "Password reset email sent successfully to: " + recipientEmail);
                return true;

            } catch (MessagingException e) {
                Log.e(TAG, "Failed to send email", e);
                errorMessage = "Failed to send email: " + e.getMessage();
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while sending email", e);
                errorMessage = "Unexpected error: " + e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(errorMessage != null ? errorMessage : "Unknown error occurred");
                }
            }
        }
    }

    private static String createPasswordResetEmailContent(String recipientName, String temporaryPassword) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <title>Password Reset - Tour Management</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "    <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "        <div style='background-color: #4CAF50; color: white; padding: 20px; text-align: center;'>" +
                "            <h1>Tour Management System</h1>" +
                "            <h2>Password Reset Request</h2>" +
                "        </div>" +
                "        <div style='padding: 20px; background-color: #f9f9f9;'>" +
                "            <p>Dear " + recipientName + ",</p>" +
                "            <p>We received a request to reset your password for your Tour Management account.</p>" +
                "            <p>Your temporary password is:</p>" +
                "            <div style='background-color: #e7f3ff; border: 1px solid #2196F3; padding: 15px; margin: 10px 0; text-align: center;'>" +
                "                <strong style='font-size: 18px; color: #1976D2;'>" + temporaryPassword + "</strong>" +
                "            </div>" +
                "            <p><strong>Important Security Notice:</strong></p>" +
                "            <ul>" +
                "                <li>This is a temporary password that expires in 24 hours</li>" +
                "                <li>You will be required to change this password upon your next login</li>" +
                "                <li>For security reasons, please change it to a strong, unique password</li>" +
                "                <li>If you didn't request this reset, please contact our support team immediately</li>" +
                "            </ul>" +
                "            <p>Best regards,<br>Tour Management Team</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
