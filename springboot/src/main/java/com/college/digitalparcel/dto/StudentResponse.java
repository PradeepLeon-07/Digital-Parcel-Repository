package com.college.digitalparcel.dto;

import com.college.digitalparcel.entity.Student;

public class StudentResponse {

    private Long id;
    private String registerNumber;
    private String name;
    private String department;
    private Integer year;
    private String phone;
    private String email;

    public static StudentResponse from(Student student) {
        StudentResponse response = new StudentResponse();
        response.id = student.getId();
        response.registerNumber = student.getRegisterNumber();
        response.name = student.getName();
        response.department = student.getDepartment();
        response.year = student.getYear();
        response.phone = student.getPhone();
        response.email = student.getEmail();
        return response;
    }

    public Long getId() { return id; }
    public String getRegisterNumber() { return registerNumber; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public Integer getYear() { return year; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
