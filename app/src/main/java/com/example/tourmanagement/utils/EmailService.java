package com.example.tourmanagement.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.example.tourmanagement.model.Booking;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
 * Email service utility for sending password reset emails.
 * Uses JavaMail API to send emails through SMTP.
 *
 * Features:
 * - Generate random temporary passwords
 * - Send password reset emails
 * - Asynchronous email sending
 * - Professional email templates
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class EmailService {

    private static final String TAG = "EmailService";

    // Email configuration (use your own SMTP settings)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587"; // Use 587 for STARTTLS or 465 for SSL
    private static final String EMAIL_FROM = "maixuanhieu250123@gmail.com"; // Replace with your email
    private static final String EMAIL_PASSWORD = "zmfd aycm rzuu oarb"; // Replace with your app password

    /**
     * Interface for email sending callbacks
     */
    public interface EmailCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Generates a random temporary password
     *
     * @param length Length of the password
     * @return Random password string
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * Sends password reset email asynchronously
     *
     * @param recipientEmail Recipient's email address
     * @param recipientName Recipient's name
     * @param temporaryPassword Temporary password to send
     * @param callback Callback for success/failure handling
     */
    public static void sendPasswordResetEmail(String recipientEmail, String recipientName,
                                            String temporaryPassword, EmailCallback callback) {
        new SendEmailTask(recipientEmail, recipientName, temporaryPassword, callback).execute();
    }

    /**
     * AsyncTask for sending emails in background thread
     */
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
                // Create properties for SMTP configuration
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.ssl.trust", SMTP_HOST);

                // Alternative configuration for SSL (use this if STARTTLS doesn't work)
                // Uncomment these lines and comment out the STARTTLS lines above
                // props.put("mail.smtp.socketFactory.port", "465");
                // props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                // props.put("mail.smtp.socketFactory.fallback", "false");

                // Create authenticator
                Authenticator authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                    }
                };

                // Create session
                Session session = Session.getInstance(props, authenticator);

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

    /**
     * Creates HTML email content for password reset
     *
     * @param recipientName Recipient's name
     * @param temporaryPassword Temporary password
     * @return HTML email content
     */
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
                "            <p>To log in:</p>" +
                "            <ol>" +
                "                <li>Open the Tour Management app</li>" +
                "                <li>Enter your username and the temporary password above</li>" +
                "                <li>You'll be prompted to create a new password</li>" +
                "            </ol>" +
                "            <p>If you have any questions or concerns, please don't hesitate to contact our support team.</p>" +
                "            <p>Best regards,<br>Tour Management Team</p>" +
                "        </div>" +
                "        <div style='background-color: #333; color: white; padding: 10px; text-align: center; font-size: 12px;'>" +
                "            <p>This is an automated message. Please do not reply to this email.</p>" +
                "            <p>&copy; 2025 Tour Management System. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Sends booking confirmation email with ticket information
     *
     * @param user User who made the booking
     * @param tour Tour that was booked
     * @param booking Booking details
     * @param callback Callback for success/failure handling
     */
    public static void sendBookingConfirmationEmail(User user, Tour tour, Booking booking, EmailCallback callback) {
        new SendBookingEmailTask(user, tour, booking, callback).execute();
    }

    /**
     * AsyncTask for sending booking confirmation emails in background thread
     */
    private static class SendBookingEmailTask extends AsyncTask<Void, Void, Boolean> {
        private User user;
        private Tour tour;
        private Booking booking;
        private EmailCallback callback;
        private String errorMessage;

        public SendBookingEmailTask(User user, Tour tour, Booking booking, EmailCallback callback) {
            this.user = user;
            this.tour = tour;
            this.booking = booking;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Create properties for SMTP configuration
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.ssl.trust", SMTP_HOST);

                // Create authenticator
                Authenticator authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                    }
                };

                // Create session
                Session session = Session.getInstance(props, authenticator);

                // Create message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
                message.setSubject("Tour Management - Booking Confirmation & E-Ticket");

                // Create email content with ticket information
                String emailContent = createBookingConfirmationEmailContent(user, tour, booking);
                message.setContent(emailContent, "text/html; charset=utf-8");

                // Send email
                Transport.send(message);

                Log.d(TAG, "Booking confirmation email sent successfully to: " + user.getEmail());
                return true;

            } catch (MessagingException e) {
                Log.e(TAG, "Failed to send booking confirmation email", e);
                errorMessage = "Failed to send email: " + e.getMessage();
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while sending booking confirmation email", e);
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

    /**
     * Creates HTML email content for booking confirmation with ticket details
     *
     * @param user User who made the booking
     * @param tour Tour that was booked
     * @param booking Booking details
     * @return HTML email content with ticket information
     */
    private static String createBookingConfirmationEmailContent(User user, Tour tour, Booking booking) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        SimpleDateFormat tourDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        String bookingDate = dateFormat.format(new Date(booking.getBookingDate()));
        String tourDate = tourDateFormat.format(new Date(tour.getTourTime()));

        // Generate booking reference number
        String bookingReference = "TM" + String.format("%06d", booking.getId());

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <title>Booking Confirmation - Tour Management</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0;'>" +
                "    <div style='max-width: 650px; margin: 0 auto; background-color: #ffffff;'>" +
                "        <!-- Header -->" +
                "        <div style='background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); color: white; padding: 30px 20px; text-align: center;'>" +
                "            <h1 style='margin: 0; font-size: 28px; font-weight: bold;'>🎫 Tour Management</h1>" +
                "            <h2 style='margin: 10px 0 0 0; font-size: 20px; font-weight: normal;'>Booking Confirmation & E-Ticket</h2>" +
                "        </div>" +

                "        <!-- Booking Status -->" +
                "        <div style='background-color: #e8f5e8; border-left: 5px solid #4CAF50; padding: 20px; margin: 0;'>" +
                "            <div style='display: flex; align-items: center; justify-content: center;'>" +
                "                <span style='font-size: 24px; color: #4CAF50; margin-right: 10px;'>✅</span>" +
                "                <h3 style='margin: 0; color: #2e7d32; font-size: 18px;'>Booking Confirmed Successfully!</h3>" +
                "            </div>" +
                "        </div>" +

                "        <!-- Main Content -->" +
                "        <div style='padding: 30px 20px;'>" +
                "            <p style='font-size: 16px; margin-bottom: 20px;'>Dear " + user.getFullName() + ",</p>" +
                "            <p style='font-size: 14px; margin-bottom: 25px;'>Thank you for choosing Tour Management! Your booking has been confirmed. Below are your ticket details:</p>" +

                "            <!-- Ticket Information -->" +
                "            <div style='background-color: #f8f9fa; border: 2px dashed #4CAF50; border-radius: 10px; padding: 25px; margin: 25px 0;'>" +
                "                <div style='text-align: center; margin-bottom: 20px;'>" +
                "                    <h2 style='color: #4CAF50; margin: 0; font-size: 22px;'>🎫 E-TICKET</h2>" +
                "                    <p style='margin: 5px 0; font-size: 16px; font-weight: bold; color: #333;'>Booking Reference: " + bookingReference + "</p>" +
                "                </div>" +

                "                <!-- Tour Details -->" +
                "                <table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>" +
                "                    <tr>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; font-weight: bold; color: #555; width: 35%;'>Tour Name:</td>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; color: #333;'>" + tour.getTourName() + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; font-weight: bold; color: #555;'>Destination:</td>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; color: #333;'>" + tour.getTourLocation() + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; font-weight: bold; color: #555;'>Tour Date:</td>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; color: #333;'>" + tourDate + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; font-weight: bold; color: #555;'>Duration:</td>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; color: #333;'>" + tour.getDuration() + " days</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; font-weight: bold; color: #555;'>Number of People:</td>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; color: #333;'>" + booking.getNumberOfPeople() + " person(s)</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; font-weight: bold; color: #555;'>Total Amount:</td>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; color: #4CAF50; font-weight: bold; font-size: 16px;'>$" + String.format("%.2f", booking.getTotalAmount()) + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; font-weight: bold; color: #555;'>Booking Date:</td>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; color: #333;'>" + bookingDate + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0; font-weight: bold; color: #555;'>Status:</td>" +
                "                        <td style='padding: 10px 0; border-bottom: 1px solid #e0e0e0;'>" +
                "                            <span style='background-color: #4CAF50; color: white; padding: 5px 15px; border-radius: 20px; font-size: 12px; font-weight: bold;'>" + booking.getBookingStatus() + "</span>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +

                // Add notes if available
                (booking.getNotes() != null && !booking.getNotes().trim().isEmpty() ?
                "                <div style='margin-top: 20px; padding: 15px; background-color: #fff3cd; border-radius: 5px; border-left: 4px solid #ffc107;'>" +
                "                    <p style='margin: 0; font-weight: bold; color: #856404;'>Special Notes:</p>" +
                "                    <p style='margin: 5px 0 0 0; color: #856404;'>" + booking.getNotes() + "</p>" +
                "                </div>" : "") +
                "            </div>" +

                "            <!-- Important Information -->" +
                "            <div style='background-color: #e3f2fd; border-radius: 8px; padding: 20px; margin: 25px 0;'>" +
                "                <h3 style='color: #1565c0; margin: 0 0 15px 0; font-size: 18px;'>📋 Important Information</h3>" +
                "                <ul style='margin: 0; padding-left: 20px; color: #333;'>" +
                "                    <li style='margin-bottom: 8px;'>Please arrive at the meeting point 30 minutes before the tour starts</li>" +
                "                    <li style='margin-bottom: 8px;'>Bring a valid ID and this e-ticket (digital or printed)</li>" +
                "                    <li style='margin-bottom: 8px;'>Check weather conditions and dress appropriately</li>" +
                "                    <li style='margin-bottom: 8px;'>Contact us immediately if you need to make changes</li>" +
                "                    <li style='margin-bottom: 8px;'>Cancellations must be made at least 24 hours in advance</li>" +
                "                </ul>" +
                "            </div>" +

                "            <!-- Contact Information -->" +
                "            <div style='background-color: #f5f5f5; border-radius: 8px; padding: 20px; margin: 25px 0;'>" +
                "                <h3 style='color: #333; margin: 0 0 15px 0; font-size: 18px;'>📞 Need Help?</h3>" +
                "                <p style='margin: 0 0 10px 0; color: #555;'>If you have any questions or need assistance, feel free to contact us:</p>" +
                "                <p style='margin: 5px 0; color: #555;'><strong>Email:</strong> support@tourmanagement.com</p>" +
                "                <p style='margin: 5px 0; color: #555;'><strong>Phone:</strong> +1 (555) 123-4567</p>" +
                "                <p style='margin: 5px 0; color: #555;'><strong>Support Hours:</strong> 9:00 AM - 6:00 PM (Mon-Fri)</p>" +
                "            </div>" +

                "            <p style='font-size: 14px; color: #666; margin-top: 30px;'>We're excited to have you join us on this amazing journey! Have a wonderful trip!</p>" +
                "            <p style='font-size: 14px; color: #333; margin-top: 20px;'>Best regards,<br><strong>Tour Management Team</strong></p>" +
                "        </div>" +

                "        <!-- Footer -->" +
                "        <div style='background-color: #333; color: #ccc; padding: 20px; text-align: center; font-size: 12px;'>" +
                "            <p style='margin: 0 0 10px 0;'>This is an automated confirmation email. Please do not reply to this email.</p>" +
                "            <p style='margin: 0;'>&copy; 2025 Tour Management System. All rights reserved.</p>" +
                "            <p style='margin: 10px 0 0 0;'>Follow us on social media for updates and travel tips!</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
