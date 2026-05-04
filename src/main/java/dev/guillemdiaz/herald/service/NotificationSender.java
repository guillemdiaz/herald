package dev.guillemdiaz.herald.service;

public interface NotificationSender {

    /**
     * Sends a notification to a specific recipient.
     *
     * @param recipientNumber The phone number or endpoint to send to
     * @param message The content of the notification
     * @return boolean indicating if the delivery was successfully handed off
     */
    boolean send(String recipientNumber, String message);
}
