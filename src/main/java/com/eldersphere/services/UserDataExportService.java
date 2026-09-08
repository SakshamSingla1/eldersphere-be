package com.eldersphere.services;

import com.eldersphere.dtos.User.UserDataExportResponse;
import com.eldersphere.exceptions.GenericException;

public interface UserDataExportService {
    /** Aggregates the caller's own profile, bookings, reviews, medical records and notifications. */
    UserDataExportResponse export(Long userId) throws GenericException;
}
