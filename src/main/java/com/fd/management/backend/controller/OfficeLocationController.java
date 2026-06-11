package com.fd.management.backend.controller;

import com.fd.management.backend.entity.OfficeLocation;
import com.fd.management.backend.repository.OfficeLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/office")
@RequiredArgsConstructor
public class OfficeLocationController {

    private final OfficeLocationRepository officeRepository;

    @PostMapping("/set-location")
    public ResponseEntity<?> setOfficeLocation(@RequestBody OfficeLocation location) {
        officeRepository.deleteAll();

        if(location.getRadiusMeters() == null) {
            location.setRadiusMeters(50.0); // Default 50m
        }

        OfficeLocation savedLocation = officeRepository.save(location);
        return ResponseEntity.ok(savedLocation);
    }

    @GetMapping("/get-location")
    public ResponseEntity<?> getOfficeLocation() {
        return officeRepository.findAll().stream().findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}