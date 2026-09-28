package com.college.digitalparcel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateParcelRequest {

    @NotBlank(message = "Parcel ID is required")
    private String parcelId;

    @NotBlank(message = "Student register number is required")
    private String studentRegisterNumber;

    @NotBlank(message = "Courier name is required")
    private String courierName;

    private String senderName;

    @NotNull(message = "Received date is required")
    private LocalDate receivedDate;

    private String storageLocation;

    public String getParcelId() { return parcelId; }
    public void setParcelId(String parcelId) { this.parcelId = parcelId; }

    public String getStudentRegisterNumber() { return studentRegisterNumber; }
    public void setStudentRegisterNumber(String studentRegisterNumber) { this.studentRegisterNumber = studentRegisterNumber; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
}
