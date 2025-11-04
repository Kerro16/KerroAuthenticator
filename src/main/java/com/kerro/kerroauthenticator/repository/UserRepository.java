package com.kerro.kerroauthenticator.repository;

import com.kerro.kerroauthenticator.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User,Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String name);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByRole(String role);
    List<User> findNombreByUsernameContainingIgnoreCase(String username);
}
