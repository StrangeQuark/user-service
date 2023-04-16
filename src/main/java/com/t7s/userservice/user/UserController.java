package com.t7s.userservice.user;

import com.t7s.userservice.registration.RegistrationRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(path = "api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "{username}")
    public Optional<Users> getUser(@PathVariable("username") String username) {
        return userService.getUserByUsername(username);
    }

    @PostMapping(path = "details/{username}")
    public UserDetails getUserDetails(@PathVariable("username") String username) {
        return userService.loadUserByUsername(username);
    }

    @PostMapping(path = "deleteUser")
    public String deleteUser(@RequestBody RegistrationRequest request) {
        return userService.deleteUser(request);
    }

    @PostMapping(path = "updateUsername")
    public String updateUsername(@RequestBody UpdateUsernameRequest request) {
        return userService.updateUsername(request);
    }

    @PostMapping(path = "updateEmail")
    public String updateEmail(@RequestBody UpdateEmailRequest request) {
        return userService.updateEmail(request);
    }
}