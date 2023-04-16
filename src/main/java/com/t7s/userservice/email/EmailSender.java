package com.t7s.userservice.email;

public interface EmailSender {
    void send(String recipient, String email, String subject);
}
