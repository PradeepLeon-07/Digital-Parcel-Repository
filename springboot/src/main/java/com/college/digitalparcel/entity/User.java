package com.college.digitalparcel.entity;

import jakarta.persistence.*;

/*
 * WHAT IS AN ENTITY?
 * An entity is a Java class that maps to a database table.
 * Every field in this class becomes a column in the table.
 * Every object of this class becomes one row in the table.
 *
 * This User class maps to the "users" table in MySQL.
 * It stores login information for all users — admin, security staff, and students.
 *
 * TABLE: users
 * COLUMNS: id, username, password, role, enabled
 */

// @Entity tells Hibernate: "this class is a database table"
@Entity

// @Table(name = "users") sets the table name to "users"
// We specify this because "user" is a reserved keyword in MySQL
@Table(name = "users")
public class User {

    // @Id marks this field as the PRIMARY KEY of the table
    @Id

    // @GeneratedValue(strategy = GenerationType.IDENTITY) means:
    // MySQL will auto-increment this number (1, 2, 3, 4...)
    // We never set the id manually — the database assigns it automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column customizes the database column
    // nullable = false  → this column cannot be empty (NOT NULL in SQL)
    // unique = true     → no two users can have the same username
    // length = 50       → VARCHAR(50) in MySQL
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // The password is stored as a BCrypt hash, not plain text
    // BCrypt hashes are always 60 characters long, so length = 100 is safe
    @Column(nullable = false, length = 100)
    private String password;

    // @Enumerated(EnumType.STRING) stores the enum as text in the database
    // So the database will store "ADMIN", "SECURITY", or "STUDENT"
    // NOT numbers like 0, 1, 2 — because if we reorder the enum, numbers would be wrong
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // enabled = true means this account is active
    // enabled = false means the account is disabled (cannot login)
    @Column(nullable = false)
    private boolean enabled = true;

    // Default constructor — required by JPA/Hibernate to create objects
    public User() {}

    // --- GETTERS AND SETTERS ---
    // Getters let other classes READ the value of a private field
    // Setters let other classes WRITE/CHANGE the value of a private field
    // We keep fields private and use getters/setters — this is called Encapsulation

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
