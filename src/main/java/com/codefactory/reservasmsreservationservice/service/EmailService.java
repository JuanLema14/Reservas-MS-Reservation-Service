package com.codefactory.reservasmsreservationservice.service;

/**
 * Service interface for email operations.
 * Handles sending notification emails related to reservations.
 */
public interface EmailService {

    /**
     * Sends a reservation confirmation email to a client.
     *
     * @param to Recipient email address
     * @param clienteNombre Client's name for personalization
     * @param servicioNombre Name of the reserved service
     * @param fechaHora Date and time of the reservation
     */
    void sendReservationConfirmationEmail(String to, String clienteNombre, String servicioNombre, String fechaHora);

    /**
     * Sends a reservation cancellation email to a client.
     *
     * @param to Recipient email address
     * @param clienteNombre Client's name for personalization
     * @param servicioNombre Name of the cancelled service
     * @param fechaHora Date and time of the cancelled reservation
     */
    void sendReservationCancellationEmail(String to, String clienteNombre, String servicioNombre, String fechaHora);

    /**
     * Sends a reservation reminder email to a client.
     *
     * @param to Recipient email address
     * @param clienteNombre Client's name for personalization
     * @param servicioNombre Name of the reserved service
     * @param fechaHora Date and time of the reservation
     */
    void sendReservationReminderEmail(String to, String clienteNombre, String servicioNombre, String fechaHora);
}