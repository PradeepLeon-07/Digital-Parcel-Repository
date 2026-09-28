package com.college.digitalparcel.service;

import com.college.digitalparcel.dto.CreateStudentRequest;
import com.college.digitalparcel.dto.StudentResponse;
import com.college.digitalparcel.entity.Role;
import com.college.digitalparcel.entity.Student;
import com.college.digitalparcel.entity.User;
import com.college.digitalparcel.exception.BadRequestException;
import com.college.digitalparcel.exception.ResourceNotFoundException;
import com.college.digitalparcel.repository.StudentRepository;
import com.college.digitalparcel.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/*
 * WHAT IS A SERVICE?
 * The service layer contains the BUSINESS LOGIC of the application.
 * It sits between the Controller (which receives HTTP requests)
 * and the Repository (which talks to the database).
 *
 * WHY NOT PUT THIS LOGIC IN THE CONTROLLER?
 * Controllers should only handle HTTP — reading requests and sending responses.
 * Business rules (like "check if register number already exists") belong in the service.
 * This keeps each class focused on one job (Single Responsibility Principle).
 *
 * @Service tells Spring: "create one instance of this class and manage it"
 * Spring will automatically inject this service wherever it is needed.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring automatically provides these three objects through constructor injection
    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * Creates a new student AND a login account for that student.
     *
     * @Transactional means both saves happen in ONE database transaction.
     * If saving the User fails for any reason, the Student save is also undone.
     * This prevents a situation where a student exists but has no login account.
     */
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {

        // Check 1: Is the register number already used by another student?
        if (studentRepository.existsByRegisterNumber(request.getRegisterNumber())) {
            throw new BadRequestException("Register number already exists: " + request.getRegisterNumber());
        }

        // Check 2: Is the email already used by another student?
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        // Check 3: Does a login account already exist for this register number?
        if (userRepository.existsByUsername(request.getRegisterNumber())) {
            throw new BadRequestException("User account already exists for: " + request.getRegisterNumber());
        }

        // Create and save the student profile in the students table
        Student student = new Student();
        student.setRegisterNumber(request.getRegisterNumber());
        student.setName(request.getName());
        student.setDepartment(request.getDepartment());
        student.setYear(request.getYear());
        student.setPhone(request.getPhone());
        student.setEmail(request.getEmail());
        studentRepository.save(student); // INSERT INTO students ...

        // Create and save the login account in the users table
        // The student's login username = their register number
        User user = new User();
        user.setUsername(request.getRegisterNumber());
        // NEVER store plain text passwords — always hash them with BCrypt
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        userRepository.save(user); // INSERT INTO users ...

        // Convert the Student entity to a StudentResponse DTO and return it
        // We return a DTO (not the entity) to control exactly what data is sent back
        return StudentResponse.from(student);
    }

    // Get all students from the database and convert each to a StudentResponse DTO
    public List<StudentResponse> getAllStudents() {
        List<Student> allStudents = studentRepository.findAll();

        // .stream() lets us process the list
        // .map(StudentResponse::from) converts each Student to a StudentResponse
        // .toList() collects the results back into a List
        return allStudents.stream()
                .map(StudentResponse::from)
                .toList();
    }

    // Find one student by their register number
    public StudentResponse getStudentByRegisterNumber(String registerNumber) {
        // findByRegisterNumber returns Optional<Student>
        // .orElseThrow() means: if not found, throw this exception (which returns 404)
        Student student = studentRepository.findByRegisterNumber(registerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + registerNumber));

        return StudentResponse.from(student);
    }
}
