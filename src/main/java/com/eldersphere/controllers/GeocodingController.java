package com.eldersphere.controllers;

import com.eldersphere.dtos.Common.GeocodingResultDTO;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.GeocodingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/geocoding")
@Tag(name = "Geocoding", description = "Free OpenStreetMap Nominatim-backed address search and reverse geocoding")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingService geocodingService;

    @Operation(summary = "Search addresses", description = "Free-text address autocomplete, e.g. for the elder profile address field.")
    @GetMapping("/search")
    public ResponseEntity<ResponseModel<List<GeocodingResultDTO>>> search(@RequestParam String query) {
        return ApiResponse.successResponse(geocodingService.search(query), "Addresses fetched successfully");
    }

    @Operation(summary = "Reverse geocode a coordinate", description = "Resolves a lat/long pair (e.g. from an emergency alert) into a readable address.")
    @GetMapping("/reverse")
    public ResponseEntity<ResponseModel<String>> reverse(@RequestParam Double lat, @RequestParam Double lon) {
        return ApiResponse.successResponse(geocodingService.reverseGeocode(lat, lon), "Address resolved successfully");
    }
}
