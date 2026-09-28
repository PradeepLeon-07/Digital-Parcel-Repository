package com.college.digitalparcel.dto;

public class DashboardResponse {

    private long totalParcels;
    private long received;
    private long readyForCollection;
    private long collected;
    private long receivedToday;
    private long collectedToday;

    public DashboardResponse(long totalParcels, long received, long readyForCollection,
                              long collected, long receivedToday, long collectedToday) {
        this.totalParcels = totalParcels;
        this.received = received;
        this.readyForCollection = readyForCollection;
        this.collected = collected;
        this.receivedToday = receivedToday;
        this.collectedToday = collectedToday;
    }

    public long getTotalParcels() { return totalParcels; }
    public long getReceived() { return received; }
    public long getReadyForCollection() { return readyForCollection; }
    public long getCollected() { return collected; }
    public long getReceivedToday() { return receivedToday; }
    public long getCollectedToday() { return collectedToday; }
}
