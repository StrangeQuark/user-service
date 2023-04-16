package com.t7s.userservice.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findUserByUsernameIgnoreCase(String username);

    @Query("SELECT u FROM Users u WHERE u.email = ?1")
    Optional<Users> findUserByEmail(String email);

    Users getUserByUsernameIgnoreCase(String username);

    Users getUserByEmailIgnoreCase(String email);

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.isEnabled = TRUE WHERE u.email = ?1")
    int enableUser(String email);

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.password = ?2 WHERE u.email = ?1")
    int updatePasswordViaEmail(String email, String password);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM confirmation_token WHERE confirmation_token.user_id = (SELECT id FROM users WHERE users.username = :username);" +
            "DELETE FROM users WHERE users.username = :username", nativeQuery = true)
    int deleteUser(String username);

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.username = ?2 WHERE u.username = ?1")
    int updateUsername(String username, String newUsername);

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.email = ?2 WHERE u.username = ?1")
    int updateEmail(String username, String newEmail);
}
