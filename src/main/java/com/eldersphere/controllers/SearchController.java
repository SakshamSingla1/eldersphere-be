package com.eldersphere.controllers;

import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.dtos.User.UserLinkSearchResultDTO;
import com.eldersphere.entities.User;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.SearchService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Public caretaker discovery: filter by service category, minimum rating, verification status, hourly-rate range, and service area/location")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final Helper helper;

    @Operation(summary = "Search users to link", description = "Authenticated search for an existing ELDER or FAMILY_MEMBER account by name, email, or phone — used by the family-member \"link an existing account\" flow.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users")
    public ResponseEntity<ResponseModel<List<UserLinkSearchResultDTO>>> searchUsers(
            @RequestParam String query,
            @RequestParam(required = false) UserTypeEnum userType,
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        return ApiResponse.successResponse(searchService.searchLinkableUsers(query, userType, caller), "Users fetched successfully");
    }

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
