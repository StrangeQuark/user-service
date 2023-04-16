package com.t7s.userservice.login;

public class LoginRequest {
    private final String username;
    private final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public String toString() {
        return "RegistrationRequest{" + "username='" + username + '\'' + ", password='" + password + '\'' + '}';
    }
}
