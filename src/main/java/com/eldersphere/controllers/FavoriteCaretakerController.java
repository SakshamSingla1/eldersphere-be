package com.eldersphere.controllers;

import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.FavoriteCaretakerService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Favorite Caretakers", description = "Family members can save/bookmark caretakers for quick re-booking")
@RequiredArgsConstructor
public class FavoriteCaretakerController {

    private final FavoriteCaretakerService favoriteCaretakerService;
    private final Helper helper;

    @Operation(summary = "Add a caretaker to my favorites")
    @PostMapping("/{caretakerId}")
    public ResponseEntity<ResponseModel<CaretakerSearchResultDTO>> addFavorite(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long caretakerId) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.createSuccess(favoriteCaretakerService.addFavorite(userId, caretakerId), "Caretaker added to favorites");
    }

    @Operation(summary = "Remove a caretaker from my favorites")
    @DeleteMapping("/{caretakerId}")
    public ResponseEntity<ResponseModel<String>> removeFavorite(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long caretakerId) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        favoriteCaretakerService.removeFavorite(userId, caretakerId);
        return ApiResponse.successResponse("Caretaker removed from favorites");
    }

    @Operation(summary = "List my favorite caretakers")
    @GetMapping
    public ResponseEntity<ResponseModel<List<CaretakerSearchResultDTO>>> listFavorites(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(favoriteCaretakerService.listFavorites(userId), "Favorite caretakers fetched successfully");
    }
}
