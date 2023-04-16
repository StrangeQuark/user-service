package com.t7s.userservice.user;

public class UpdateUsernameRequest {
    private final String username;
    private final String newUsername;
    private final String password;

    public UpdateUsernameRequest(String username, String newUsername, String password) {
        this.username = username;
        this.password = password;
        this.newUsername = newUsername;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNewUsername() {
        return newUsername;
    }

    @Override
    public String toString() {
        return "UpdateUsernameRequest{" + "username='" + username + '\'' + ", password='" + password + '\'' + ", newUsername='" + newUsername + '\'' + '}';
    }
}
