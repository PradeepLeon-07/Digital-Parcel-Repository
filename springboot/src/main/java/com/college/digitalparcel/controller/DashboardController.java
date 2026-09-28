package com.college.digitalparcel.controller;

import com.college.digitalparcel.dto.DashboardResponse;
import com.college.digitalparcel.service.ParcelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ParcelService parcelService;

    public DashboardController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(parcelService.getDashboard());
    }
}
