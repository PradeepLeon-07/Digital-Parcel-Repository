package com.college.digitalparcel.repository;

import com.college.digitalparcel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/*
 * WHAT IS A REPOSITORY?
 * A repository is the layer that talks directly to the database.
 * All database queries (SELECT, INSERT, UPDATE, DELETE) happen here.
 *
 * WHAT IS JpaRepository?
 * JpaRepository is an interface provided by Spring Data JPA.
 * By extending it, we automatically get these database methods for FREE:
 *
 *   save(user)         → INSERT or UPDATE a user in the database
 *   findById(id)       → SELECT * FROM users WHERE id = ?
 *   findAll()          → SELECT * FROM users
 *   deleteById(id)     → DELETE FROM users WHERE id = ?
 *   existsById(id)     → returns true if a user with that id exists
 *   count()            → returns total number of users
 *
 * We don't write any SQL. Spring generates it automatically.
 *
 * JpaRepository<User, Long> means:
 *   User → this repository manages the User entity
 *   Long → the data type of the primary key (id)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * DERIVED QUERY METHOD:
     * Spring reads the method name "findByUsername" and automatically generates this SQL:
     *   SELECT * FROM users WHERE username = ?
     *
     * Optional<User> means the result might be empty (user not found).
     * Using Optional is safer than returning null — it forces us to handle the "not found" case.
     * In the service layer we call .orElseThrow() on it.
     */
    Optional<User> findByUsername(String username);

    /*
     * Spring generates: SELECT COUNT(*) > 0 FROM users WHERE username = ?
     * Returns true if a user with this username already exists.
     * We use this to prevent duplicate usernames during registration.
     */
    boolean existsByUsername(String username);
}
