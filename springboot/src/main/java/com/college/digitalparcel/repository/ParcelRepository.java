package com.college.digitalparcel.repository;

import com.college.digitalparcel.entity.Parcel;
import com.college.digitalparcel.entity.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
 * This repository handles all database operations for the Parcel table.
 */
@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    // Spring generates: SELECT * FROM parcels WHERE parcel_id = ?
    Optional<Parcel> findByParcelId(String parcelId);

    // Spring generates: SELECT * FROM parcels WHERE status = ?
    List<Parcel> findByStatus(ParcelStatus status);

    /*
     * This method crosses two tables (parcels and students).
     * The underscore in "Student_RegisterNumber" tells Spring:
     *   "Go to the student field in Parcel, then look at registerNumber inside Student"
     *
     * Spring generates this SQL automatically:
     *   SELECT * FROM parcels p
     *   JOIN students s ON p.student_id = s.id
     *   WHERE s.register_number = ?
     */
    List<Parcel> findByStudent_RegisterNumber(String registerNumber);

    // Spring generates: SELECT COUNT(*) > 0 FROM parcels WHERE parcel_id = ?
    boolean existsByParcelId(String parcelId);

    // Spring generates: SELECT COUNT(*) FROM parcels WHERE status = ?
    // Used for the dashboard statistics
    long countByStatus(ParcelStatus status);

    /*
     * @Query lets us write our own JPQL query when the method name approach is not enough.
     * JPQL looks like SQL but uses Java class names and field names instead of table/column names.
     *
     * "Parcel" = the Java class name (not the table name "parcels")
     * "p.receivedDate" = the Java field name (not the column name "received_date")
     * :today = a parameter we pass in when calling this method
     */
    @Query("SELECT COUNT(p) FROM Parcel p WHERE p.receivedDate = :today")
    long countReceivedToday(LocalDate today);

    // Counts how many parcels were collected today — used for dashboard
    @Query("SELECT COUNT(p) FROM Parcel p WHERE p.collectedDate = :today")
    long countCollectedToday(LocalDate today);
}
