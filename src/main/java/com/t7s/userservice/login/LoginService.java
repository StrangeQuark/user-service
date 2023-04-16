package com.t7s.userservice.login;

import com.t7s.userservice.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public LoginService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public String login(LoginRequest loginRequest) {
        String username;
        try {
            username = userRepository.getUserByUsernameIgnoreCase(loginRequest.getUsername()).getUsername();
        }catch(Exception ex) { return "Login failed";}

        String encodedPassword = userRepository.getUserByUsernameIgnoreCase(loginRequest.getUsername()).getPassword();

        if(bCryptPasswordEncoder.matches(loginRequest.getPassword(), encodedPassword)) {
            if(userRepository.getUserByUsernameIgnoreCase(loginRequest.getUsername()).getEnabled())
                return username;
            else
                return "User not enabled";
        }

        return "Login failed";
    }
}
