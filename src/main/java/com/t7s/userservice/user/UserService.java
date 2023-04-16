package com.t7s.userservice.user;

import com.t7s.userservice.email.EmailSender;
import com.t7s.userservice.registration.EmailValidator;
import com.t7s.userservice.registration.RegistrationRequest;
import com.t7s.userservice.registration.token.ConfirmationToken;
import com.t7s.userservice.registration.token.ConfirmationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MESSAGE = "Users with username %s was not found";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final EmailValidator emailValidator;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ConfirmationTokenService confirmationTokenService, EmailSender emailSender, EmailValidator emailValidator) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.emailSender = emailSender;
        this.emailValidator = emailValidator;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsernameIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, username)));
    }

    public Optional<Users> getUserByUsername(String username) {
        return userRepository.findUserByUsernameIgnoreCase(username);
    }

    public String registerUser(Users user) {
        boolean usernameExists = userRepository.findUserByUsernameIgnoreCase(user.getUsername()).isPresent();
        boolean emailExists = userRepository.findUserByEmail(user.getEmail()).isPresent();

        //Throw an exception if the username is already present in the users database
        if(usernameExists) {
            throw new IllegalStateException("This username is already taken");
        }
        //Throw an exception if the email is already present in the users database
        if(emailExists) {
            throw new IllegalStateException("This email is already taken");
        }

        //Encrypt the password for database storage
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        //Save the user to the database
        userRepository.save(user);

        //Create an email confirmation token
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);

        //Save the confirmation token to the database
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        //Return the token to the POST request endpoint
        return token;
    }

    public String updatePassword(String input) {

        //Get rid of the quotes around the input
        String request = input.replaceAll("\"", "")
                              .replaceAll("%22=", "")
                              .replaceAll("%22", "");

        boolean emailExists = userRepository.findUserByEmail(request).isPresent();
        boolean usernameExists = userRepository.findUserByUsernameIgnoreCase(request).isPresent();
        String token = UUID.randomUUID().toString();

        //First, check if the user submitted an existing email address
        if(emailExists) {
            Users user = userRepository.getUserByEmailIgnoreCase(request);
            ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
            confirmationTokenService.saveConfirmationToken(confirmationToken);

            emailSender.send(request, buildPasswordResetEmail(user.getUsername(), "http://localhost:3000/new-password?token=" + token), "T7S account - reset password");

            return "Thank you, an email has been sent to " + request + " with a link to reset your password";
        }
        //If the submission wasn't an existing email address, let's check if they submitted a username instead
        if(usernameExists) {
            Users user = userRepository.getUserByUsernameIgnoreCase(request);
            ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
            confirmationTokenService.saveConfirmationToken(confirmationToken);

            emailSender.send(user.getEmail(), buildPasswordResetEmail(user.getUsername(), "http://localhost:3000/new-password?token=" + token), "T7S account - reset password");

            return "Thank you, an email has been sent to the email address associated with " + request + "'s account with a link to reset your password";
        }
        return "Sorry, we could not find your account";
    }

    @Transactional
    public String confirmPasswordResetToken(ResetPasswordRequest request) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(request.getToken()).orElseThrow(() -> new IllegalStateException("Token not found"));

        //Check if the email has already been confirmed
        if(confirmationToken.getConfirmedAt() != null) {
            return "This token has already been used for a password reset, please submit a new password reset request";
        }

        //Check if the token has expired yet
        if(confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "The token has expired, please submit a new password reset request";
        }

        //Confirm the token in the database
        confirmationTokenService.setConfirmedAt(request.getToken());

        //Encrypt the password for database storage
        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        //Update the password in the database
        userRepository.updatePasswordViaEmail(confirmationToken.getUser().getEmail(), encodedPassword);

        return "Your password has been successfully reset!";
    }

    public int enableUser(String email) {
        return userRepository.enableUser(email);
    }

    public String deleteUser(RegistrationRequest request) {
        boolean usernameExists = userRepository.findUserByUsernameIgnoreCase(request.getUsername()).isPresent();

        if(usernameExists) {
            String encodedPassword = userRepository.getUserByUsernameIgnoreCase(request.getUsername()).getPassword();

            if(bCryptPasswordEncoder.matches(request.getPassword(), encodedPassword)) {
                userRepository.deleteUser(request.getUsername());
                return "Success";
            }
            return "Incorrect password";
        }
        return "User doesn't exist";
    }

    public String updateUsername(UpdateUsernameRequest request) {
        boolean usernameExists = userRepository.findUserByUsernameIgnoreCase(request.getUsername()).isPresent();

        if(usernameExists) {
            String encodedPassword = userRepository.getUserByUsernameIgnoreCase(request.getUsername()).getPassword();

            if(bCryptPasswordEncoder.matches(request.getPassword(), encodedPassword)) {
                int i = userRepository.updateUsername(request.getUsername(), request.getNewUsername());

                if(i == 1)
                    return "Success";
                return "Failed";
            }
            return "Incorrect password";
        }
        return "User doesn't exist";
    }

    public String updateEmail(UpdateEmailRequest request) {
        boolean usernameExists = userRepository.findUserByUsernameIgnoreCase(request.getUsername()).isPresent();

        if(usernameExists) {
            //Check that the regex of the email is valid
            boolean isEmailValid = emailValidator.test(request.getNewEmail());
            if(!isEmailValid) {
                return "Invalid email";
            }

            String encodedPassword = userRepository.getUserByUsernameIgnoreCase(request.getUsername()).getPassword();

            if(bCryptPasswordEncoder.matches(request.getPassword(), encodedPassword)) {
                int i = userRepository.updateEmail(request.getUsername(), request.getNewEmail());

                if(i == 1)
                    return "Success";
                return "Failed";
            }
            return "Incorrect password";
        }
        return "User doesn't exist";
    }

    private String buildPasswordResetEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Reset your password</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Please click on the below link to reset your password: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Reset password</a> </p></blockquote>\n This link will expire in 15 minutes. <p>Thank you</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
