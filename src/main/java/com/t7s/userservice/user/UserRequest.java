package com.t7s.userservice.user;

public class UserRequest {
    private final String username;

    public UserRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "UserRequest{" + "username='" + username + '}';
    }
}
