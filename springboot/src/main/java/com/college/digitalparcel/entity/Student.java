package com.college.digitalparcel.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/*
 * This Student class maps to the "students" table in MySQL.
 * It stores the profile information of each student.
 *
 * RELATIONSHIP WITH PARCEL:
 * One student can have MANY parcels.
 * This is called a One-To-Many relationship.
 *
 * In the database, the parcels table has a column called "student_id"
 * which points back to the student who owns that parcel.
 *
 * TABLE: students
 * COLUMNS: id, register_number, name, department, year, phone, email
 */
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Register number is unique for every student — like a student ID
    // We use this to search for students and link parcels to them
    @Column(nullable = false, unique = true, length = 20)
    private String registerNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /*
     * @OneToMany means: one student has many parcels
     *
     * mappedBy = "student" means:
     *   "The Parcel class has a field called 'student' — that field owns this relationship"
     *   "The foreign key column (student_id) lives in the parcels table, not here"
     *
     * cascade = CascadeType.ALL means:
     *   If we delete a student, all their parcels are also deleted automatically
     *
     * orphanRemoval = true means:
     *   If a parcel is removed from this list, delete it from the database too
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Parcel> parcels = new ArrayList<>();

    // Default constructor — required by JPA
    public Student() {}

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Parcel> getParcels() { return parcels; }
    public void setParcels(List<Parcel> parcels) { this.parcels = parcels; }
}
