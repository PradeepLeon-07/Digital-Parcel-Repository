package com.college.digitalparcel.entity;

/*
 * WHAT IS AN ENUM?
 * An enum is a special type in Java that holds a fixed list of constant values.
 * Think of it like a dropdown menu — the user can only pick from these exact options.
 *
 * WHY USE AN ENUM FOR ROLES INSTEAD OF A PLAIN STRING?
 * If we used a String, someone could accidentally write "ADMINN" or "admin" (lowercase)
 * and the code would break silently. With an enum, Java will give a compile error
 * if you try to use a value that doesn't exist in this list.
 *
 * These are the three types of users in our system:
 * - ADMIN    : full access (add students, add parcels, delete parcels)
 * - SECURITY : can add and manage parcels, view students
 * - STUDENT  : can only view their own parcels
 */
public enum Role {
    ADMIN,
    SECURITY,
    STUDENT
}
