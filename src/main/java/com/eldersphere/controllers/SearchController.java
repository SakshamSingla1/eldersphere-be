package com.eldersphere.controllers;

import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Public caretaker discovery: filter by service category, minimum rating, verification status, hourly-rate range, and service area/location")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Search caretakers")
    @GetMapping("/caretakers")
    public ResponseEntity<ResponseModel<Page<CaretakerSearchResultDTO>>> searchCaretakers(
            @RequestParam(required = false) ServiceCategoryEnum category,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) CaretakerVerificationStatusEnum verificationStatus,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) String location,
            Pageable pageable) {
        Page<CaretakerSearchResultDTO> response = searchService.searchCaretakers(category, minRating, verificationStatus,
                minRate, maxRate, location, pageable);
        return ApiResponse.successResponse(response, "Caretakers fetched successfully");
    }
}
