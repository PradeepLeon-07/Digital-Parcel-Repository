package com.college.digitalparcel.dto;

import com.college.digitalparcel.entity.ParcelStatus;

public class UpdateParcelRequest {

    private String courierName;
    private String senderName;
    private String storageLocation;
    private ParcelStatus status;

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public ParcelStatus getStatus() { return status; }
    public void setStatus(ParcelStatus status) { this.status = status; }
}
