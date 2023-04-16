package com.t7s.userservice.registration;

public class RegistrationRequest {
    private final String username;
    private final String password;
    private final String email;

    public RegistrationRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "RegistrationRequest{" + "username='" + username + '\'' + ", password='" + password + '\'' + ", email='" + email + '\'' + '}';
    }
}
