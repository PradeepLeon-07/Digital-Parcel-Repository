package com.college.digitalparcel.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/*
 * This Parcel class maps to the "parcels" table in MySQL.
 * Each row in this table is one parcel that arrived at the college.
 *
 * RELATIONSHIP WITH STUDENT:
 * Many parcels can belong to one student.
 * This is called a Many-To-One relationship (from the parcel's point of view).
 *
 * The "student_id" column in this table is the FOREIGN KEY
 * that links each parcel to its owner (student).
 *
 * TABLE: parcels
 * COLUMNS: id, parcel_id, student_id, courier_name, sender_name,
 *          received_date, storage_location, status, collected_date
 */
@Entity
@Table(name = "parcels")
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Human-readable parcel identifier given by security staff
    // Example: "PCL-2024-001"
    @Column(nullable = false, unique = true, length = 50)
    private String parcelId;

    /*
     * @ManyToOne means: many parcels belong to one student
     *
     * @JoinColumn(name = "student_id") means:
     *   Create a column called "student_id" in the parcels table
     *   This column stores the id of the student who owns this parcel
     *   This is the FOREIGN KEY that connects parcels to students
     *
     * nullable = false means every parcel must have a student — it cannot be null
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // The name of the courier company (e.g., Amazon, FedEx, DTDC)
    @Column(nullable = false, length = 100)
    private String courierName;

    // Who sent the parcel — optional field, can be null
    @Column(length = 100)
    private String senderName;

    // The date the parcel arrived at the college
    // LocalDate stores only the date (no time) — perfect for this use case
    @Column(nullable = false)
    private LocalDate receivedDate;

    // Where the parcel is physically stored — example: "Rack-A-3"
    @Column(length = 50)
    private String storageLocation;

    // The current status of the parcel
    // Default is RECEIVED when a new parcel is created
    // @Enumerated(EnumType.STRING) stores "RECEIVED", "READY_FOR_COLLECTION", or "COLLECTED"
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ParcelStatus status = ParcelStatus.RECEIVED;

    // The date the student collected the parcel
    // This is null until the parcel is actually collected
    private LocalDate collectedDate;

    // Default constructor — required by JPA
    public Parcel() {}

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getParcelId() { return parcelId; }
    public void setParcelId(String parcelId) { this.parcelId = parcelId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public ParcelStatus getStatus() { return status; }
    public void setStatus(ParcelStatus status) { this.status = status; }

    public LocalDate getCollectedDate() { return collectedDate; }
    public void setCollectedDate(LocalDate collectedDate) { this.collectedDate = collectedDate; }
}
