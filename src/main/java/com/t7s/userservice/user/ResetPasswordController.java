package com.t7s.userservice.user;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(path = "api/v1/")
public class ResetPasswordController {
    private final UserService userService;

    public ResetPasswordController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "password-reset")
    public String resetPassword(@RequestBody String request) {
        return userService.updatePassword(request);
    }

    @PostMapping(path = "password-reset/confirm")
    public String confirmPasswordReset(@RequestBody ResetPasswordRequest request) {
        return userService.confirmPasswordResetToken(request);
    }
}
