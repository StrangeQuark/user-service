package com.t7s.userservice.user;

public class ResetPasswordRequest {
    private final String token;
    private final String password;

    public ResetPasswordRequest(String token, String password) {
        this.token = token;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public String toString() {
        return "RegistrationRequest{" + "token='" + token + '\'' + ", password='" + password + '\'' + '}';
    }
}
