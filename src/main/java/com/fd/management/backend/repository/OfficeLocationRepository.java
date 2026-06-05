package com.fd.management.backend.repository;

import com.fd.management.backend.entity.OfficeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficeLocationRepository extends JpaRepository<OfficeLocation, Long> {
}