package com.t7s.userservice.user;

public class UpdateEmailRequest {
    private final String username;
    private final String newEmail;
    private final String password;

    public UpdateEmailRequest(String username, String newEmail, String password) {
        this.username = username;
        this.password = password;
        this.newEmail = newEmail;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNewEmail() {
        return newEmail;
    }

    @Override
    public String toString() {
        return "UpdateEmailRequest{" + "username='" + username + '\'' + ", password='" + password + '\'' + ", newEmail='" + newEmail + '\'' + '}';
    }
}
