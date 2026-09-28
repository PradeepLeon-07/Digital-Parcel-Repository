package com.college.digitalparcel.service;

import com.college.digitalparcel.dto.CreateParcelRequest;
import com.college.digitalparcel.dto.DashboardResponse;
import com.college.digitalparcel.dto.ParcelResponse;
import com.college.digitalparcel.dto.UpdateParcelRequest;
import com.college.digitalparcel.entity.Parcel;
import com.college.digitalparcel.entity.ParcelStatus;
import com.college.digitalparcel.entity.Student;
import com.college.digitalparcel.exception.BadRequestException;
import com.college.digitalparcel.exception.ResourceNotFoundException;
import com.college.digitalparcel.repository.ParcelRepository;
import com.college.digitalparcel.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/*
 * This service contains all the business logic for parcel operations.
 * The controller calls methods in this service.
 * This service calls the repository to talk to the database.
 *
 * Flow: Controller → ParcelService → ParcelRepository → MySQL
 */
@Service
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final StudentRepository studentRepository;

    public ParcelService(ParcelRepository parcelRepository, StudentRepository studentRepository) {
        this.parcelRepository = parcelRepository;
        this.studentRepository = studentRepository;
    }

    // ─── CREATE A NEW PARCEL ─────────────────────────────────────────────────

    @Transactional
    public ParcelResponse createParcel(CreateParcelRequest request) {

        // Check: Is this parcel ID already in the system?
        if (parcelRepository.existsByParcelId(request.getParcelId())) {
            throw new BadRequestException("Parcel ID already exists: " + request.getParcelId());
        }

        // Find the student this parcel belongs to using their register number
        // If the student doesn't exist, throw a 404 error
        Student student = studentRepository.findByRegisterNumber(request.getStudentRegisterNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: " + request.getStudentRegisterNumber()));

        // Create a new Parcel object and fill in all the details
        Parcel parcel = new Parcel();
        parcel.setParcelId(request.getParcelId());
        parcel.setStudent(student);               // link this parcel to the student
        parcel.setCourierName(request.getCourierName());
        parcel.setSenderName(request.getSenderName());
        parcel.setReceivedDate(request.getReceivedDate());
        parcel.setStorageLocation(request.getStorageLocation());
        parcel.setStatus(ParcelStatus.RECEIVED);  // new parcels always start as RECEIVED

        // Save to database and return as a DTO
        Parcel savedParcel = parcelRepository.save(parcel);
        return ParcelResponse.from(savedParcel);
    }

    // ─── GET ALL PARCELS ─────────────────────────────────────────────────────

    public List<ParcelResponse> getAllParcels() {
        List<Parcel> allParcels = parcelRepository.findAll();
        return allParcels.stream().map(ParcelResponse::from).toList();
    }

    // ─── GET ONE PARCEL BY DATABASE ID ───────────────────────────────────────

    public ParcelResponse getParcelById(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
        return ParcelResponse.from(parcel);
    }

    // ─── SEARCH BY PARCEL ID (e.g. "PCL-2024-001") ───────────────────────────

    public ParcelResponse getParcelByParcelId(String parcelId) {
        Parcel parcel = parcelRepository.findByParcelId(parcelId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found: " + parcelId));
        return ParcelResponse.from(parcel);
    }

    // ─── GET ALL PARCELS FOR A SPECIFIC STUDENT ───────────────────────────────

    public List<ParcelResponse> getParcelsByStudent(String registerNumber) {
        // First verify the student exists
        if (!studentRepository.existsByRegisterNumber(registerNumber)) {
            throw new ResourceNotFoundException("Student not found: " + registerNumber);
        }
        List<Parcel> parcels = parcelRepository.findByStudent_RegisterNumber(registerNumber);
        return parcels.stream().map(ParcelResponse::from).toList();
    }

    // ─── GET ALL PARCELS WITH A SPECIFIC STATUS ───────────────────────────────

    public List<ParcelResponse> getParcelsByStatus(ParcelStatus status) {
        List<Parcel> parcels = parcelRepository.findByStatus(status);
        return parcels.stream().map(ParcelResponse::from).toList();
    }

    // ─── UPDATE PARCEL DETAILS ────────────────────────────────────────────────

    @Transactional
    public ParcelResponse updateParcel(Long id, UpdateParcelRequest request) {
        // Find the parcel — throw 404 if not found
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));

        // Only update fields that were actually sent in the request (not null)
        // This way, if you only want to update the status, you don't need to send all other fields
        if (request.getCourierName() != null) {
            parcel.setCourierName(request.getCourierName());
        }
        if (request.getSenderName() != null) {
            parcel.setSenderName(request.getSenderName());
        }
        if (request.getStorageLocation() != null) {
            parcel.setStorageLocation(request.getStorageLocation());
        }
        if (request.getStatus() != null) {
            parcel.setStatus(request.getStatus());
        }

        // Save the updated parcel back to the database
        Parcel updatedParcel = parcelRepository.save(parcel);
        return ParcelResponse.from(updatedParcel);
    }

    // ─── MARK PARCEL AS COLLECTED ─────────────────────────────────────────────

    @Transactional
    public ParcelResponse collectParcel(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));

        // Prevent collecting a parcel that was already collected
        if (parcel.getStatus() == ParcelStatus.COLLECTED) {
            throw new BadRequestException("Parcel has already been collected");
        }

        // Update the status and record today's date as the collection date
        parcel.setStatus(ParcelStatus.COLLECTED);
        parcel.setCollectedDate(LocalDate.now());

        Parcel updatedParcel = parcelRepository.save(parcel);
        return ParcelResponse.from(updatedParcel);
    }

    // ─── DELETE A PARCEL ──────────────────────────────────────────────────────

    @Transactional
    public void deleteParcel(Long id) {
        // Check if the parcel exists before trying to delete
        if (!parcelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parcel not found with id: " + id);
        }
        parcelRepository.deleteById(id);
    }

    // ─── DASHBOARD STATISTICS ─────────────────────────────────────────────────

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();

        // Collect all the counts and return them in one DashboardResponse object
        return new DashboardResponse(
                parcelRepository.count(),                                        // total parcels
                parcelRepository.countByStatus(ParcelStatus.RECEIVED),           // received
                parcelRepository.countByStatus(ParcelStatus.READY_FOR_COLLECTION), // ready
                parcelRepository.countByStatus(ParcelStatus.COLLECTED),          // collected
                parcelRepository.countReceivedToday(today),                      // received today
                parcelRepository.countCollectedToday(today)                      // collected today
        );
    }
}
