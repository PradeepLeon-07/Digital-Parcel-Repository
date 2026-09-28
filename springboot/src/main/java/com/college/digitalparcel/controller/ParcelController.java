package com.college.digitalparcel.controller;

import com.college.digitalparcel.dto.CreateParcelRequest;
import com.college.digitalparcel.dto.ParcelResponse;
import com.college.digitalparcel.dto.UpdateParcelRequest;
import com.college.digitalparcel.entity.ParcelStatus;
import com.college.digitalparcel.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    public ResponseEntity<ParcelResponse> createParcel(@Valid @RequestBody CreateParcelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parcelService.createParcel(request));
    }

    @GetMapping
    public ResponseEntity<List<ParcelResponse>> getAllParcels() {
        return ResponseEntity.ok(parcelService.getAllParcels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> getParcelById(@PathVariable Long id) {
        return ResponseEntity.ok(parcelService.getParcelById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<ParcelResponse> searchByParcelId(@RequestParam String parcelId) {
        return ResponseEntity.ok(parcelService.getParcelByParcelId(parcelId));
    }

    @GetMapping("/student/{registerNumber}")
    public ResponseEntity<List<ParcelResponse>> getParcelsByStudent(
            @PathVariable String registerNumber,
            @AuthenticationPrincipal UserDetails currentUser) {
        if (!currentUser.getUsername().equals(registerNumber)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(parcelService.getParcelsByStudent(registerNumber));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParcelResponse>> getParcelsByStatus(@PathVariable ParcelStatus status) {
        return ResponseEntity.ok(parcelService.getParcelsByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParcelResponse> updateParcel(@PathVariable Long id,
                                                        @RequestBody UpdateParcelRequest request) {
        return ResponseEntity.ok(parcelService.updateParcel(id, request));
    }

    @PostMapping("/{id}/collect")
    public ResponseEntity<ParcelResponse> collectParcel(@PathVariable Long id) {
        return ResponseEntity.ok(parcelService.collectParcel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParcel(@PathVariable Long id) {
        parcelService.deleteParcel(id);
        return ResponseEntity.noContent().build();
    }
}
