package com.college.digitalparcel.repository;

import com.college.digitalparcel.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/*
 * This repository handles all database operations for the Student table.
 * We get basic CRUD operations for free from JpaRepository.
 * Below we add extra query methods that we need for our project.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /*
     * Spring generates: SELECT * FROM students WHERE register_number = ?
     * We use this when security staff assigns a parcel to a student —
     * they type the register number and we look up the student.
     */
    Optional<Student> findByRegisterNumber(String registerNumber);

    /*
     * Spring generates: SELECT COUNT(*) > 0 FROM students WHERE register_number = ?
     * We use this before creating a new student to check if the register number is already taken.
     */
    boolean existsByRegisterNumber(String registerNumber);

    /*
     * Spring generates: SELECT COUNT(*) > 0 FROM students WHERE email = ?
     * We use this before creating a new student to check if the email is already taken.
     */
    boolean existsByEmail(String email);
}
