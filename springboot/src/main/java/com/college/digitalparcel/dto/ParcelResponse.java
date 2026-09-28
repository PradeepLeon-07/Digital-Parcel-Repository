package com.college.digitalparcel.dto;

import com.college.digitalparcel.entity.Parcel;
import com.college.digitalparcel.entity.ParcelStatus;

import java.time.LocalDate;

public class ParcelResponse {

    private Long id;
    private String parcelId;
    private String studentName;
    private String studentRegisterNumber;
    private String courierName;
    private String senderName;
    private LocalDate receivedDate;
    private String storageLocation;
    private ParcelStatus status;
    private LocalDate collectedDate;

    public static ParcelResponse from(Parcel parcel) {
        ParcelResponse response = new ParcelResponse();
        response.id = parcel.getId();
        response.parcelId = parcel.getParcelId();
        response.studentName = parcel.getStudent().getName();
        response.studentRegisterNumber = parcel.getStudent().getRegisterNumber();
        response.courierName = parcel.getCourierName();
        response.senderName = parcel.getSenderName();
        response.receivedDate = parcel.getReceivedDate();
        response.storageLocation = parcel.getStorageLocation();
        response.status = parcel.getStatus();
        response.collectedDate = parcel.getCollectedDate();
        return response;
    }

    public Long getId() { return id; }
    public String getParcelId() { return parcelId; }
    public String getStudentName() { return studentName; }
    public String getStudentRegisterNumber() { return studentRegisterNumber; }
    public String getCourierName() { return courierName; }
    public String getSenderName() { return senderName; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public String getStorageLocation() { return storageLocation; }
    public ParcelStatus getStatus() { return status; }
    public LocalDate getCollectedDate() { return collectedDate; }
}
