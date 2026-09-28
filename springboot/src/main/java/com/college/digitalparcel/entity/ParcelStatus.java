package com.college.digitalparcel.entity;

/*
 * This enum represents the lifecycle of a parcel.
 * A parcel always moves in this order:
 *
 *   RECEIVED  →  READY_FOR_COLLECTION  →  COLLECTED
 *
 * RECEIVED            : Security staff just recorded the parcel in the system
 * READY_FOR_COLLECTION: The parcel has been placed in the rack and student can collect it
 * COLLECTED           : The student has picked up the parcel
 *
 * WHY ENUM INSTEAD OF STRING?
 * If we stored "RECEIVED" as a plain String in the database, someone could accidentally
 * save "received" or "Received" and our comparisons would fail.
 * With an enum, only these three exact values are allowed — nothing else.
 */
public enum ParcelStatus {
    RECEIVED,
    READY_FOR_COLLECTION,
    COLLECTED
}
