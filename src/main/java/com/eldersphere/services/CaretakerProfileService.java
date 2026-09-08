package com.eldersphere.services;

import com.eldersphere.dtos.Caretaker.CaretakerProfileRequest;
import com.eldersphere.dtos.Caretaker.CaretakerProfileResponse;
import com.eldersphere.dtos.Caretaker.CaretakerVerificationDocumentResponse;
import com.eldersphere.dtos.Caretaker.CaretakerVerificationUpdateRequest;
import com.eldersphere.exceptions.GenericException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CaretakerProfileService {
    CaretakerProfileResponse createOrUpdateForUser(Long userId, CaretakerProfileRequest request) throws GenericException;
    CaretakerProfileResponse getByUserId(Long userId) throws GenericException;
    CaretakerProfileResponse getById(Long id) throws GenericException;
    CaretakerProfileResponse updateVerification(Long id, CaretakerVerificationUpdateRequest request) throws GenericException;

    /** Called by a logged-in CARETAKER to attach a verification document (ID, certification, etc.) to their own profile. */
    CaretakerVerificationDocumentResponse uploadVerificationDocument(Long userId, MultipartFile file) throws GenericException;

    /** Called by an admin reviewing a caretaker's verification status, to see what evidence was submitted. */
    List<CaretakerVerificationDocumentResponse> listVerificationDocuments(Long caretakerProfileId) throws GenericException;
}
